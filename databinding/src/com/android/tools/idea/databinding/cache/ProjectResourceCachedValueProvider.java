/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.databinding.cache;

import com.android.tools.idea.databinding.LayoutBindingProjectComponent;
import com.google.common.collect.Maps;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility cache that can be used if results from multiple resource repositories are being merged into one.
 * @param <T> The type of {@linkplain CachedValueProvider}
 * @param <V> The type of each individual {@linkplain ResourceCacheValueProvider}
 */
abstract public class ProjectResourceCachedValueProvider<T, V> implements CachedValueProvider<T>, ModificationTracker {
  private Map<AndroidFacet, CachedValue<V>> myCachedValues = Maps.newHashMap();
  private final ModificationTracker[] myAdditionalTrackers;
  private List<ModificationTracker> myDependencies = new ArrayList<>();
  private LayoutBindingProjectComponent myComponent;
  private long myDependencyModificationCountOnCompute;
  private long myModificationCount = 0;

  @Override
  public long getModificationCount() {
    long newCount = calculateModificationCountFrom(myDependencies);
    if (newCount != myDependencyModificationCountOnCompute) {
      myModificationCount ++;
    }
    return myModificationCount;
  }

  public ProjectResourceCachedValueProvider(LayoutBindingProjectComponent component, ModificationTracker... additionalTrackers) {
    myComponent = component;
    myAdditionalTrackers = additionalTrackers;
  }

  @Nullable
  @Override
  public final Result<T> compute() {
    List<AndroidFacet> facets = getFacets();
    List<V> values = new ArrayList<>();

    List<ModificationTracker> newDependencies = new ArrayList<>();
    newDependencies.add(myComponent);
    Collections.addAll(newDependencies, myAdditionalTrackers);
    for (AndroidFacet facet : facets) {
      CachedValue<V> cachedValue = getCachedValue(facet);
      // we know this for sure since it is created from createCacheProvider
      if (cachedValue.getValueProvider() instanceof ModificationTracker) {
        newDependencies.add((ModificationTracker)cachedValue.getValueProvider());
      }
      V result = cachedValue.getValue();
      if (result != null) {
        values.add(result);
      }
    }
    myDependencies = Collections.unmodifiableList(newDependencies);
    myDependencyModificationCountOnCompute = calculateModificationCountFrom(myDependencies);
    return Result.create(merge(values), this);
  }

  @NotNull
  private List<AndroidFacet> getFacets() {
    List<AndroidFacet> viewBindingEnabledFacets = myComponent.getViewBindingEnabledFacets();
    if (viewBindingEnabledFacets.isEmpty()) {
      return myComponent.getDataBindingEnabledFacets();
    }
    return viewBindingEnabledFacets;
  }

  private static long calculateModificationCountFrom(List<ModificationTracker> dependencies) {
    long total = 0;
    for (ModificationTracker tracker : dependencies) {
      total += tracker.getModificationCount();
    }
    return total;
  }

  @NotNull
  abstract protected T merge(List<V> results);

  private CachedValue<V> getCachedValue(AndroidFacet facet) {
    CachedValue<V> cachedValue = myCachedValues.get(facet);
    if (cachedValue == null) {
      ResourceCacheValueProvider<V> cacheProvider = createCacheProvider(facet);
      cachedValue = CachedValuesManager.getManager(facet.getModule().getProject()).createCachedValue(cacheProvider, false);
      myCachedValues.put(facet, cachedValue);
    }
    return cachedValue;
  }

  abstract protected ResourceCacheValueProvider<V> createCacheProvider(AndroidFacet facet);

  abstract public static class MergedMapValueProvider<A, B> extends ProjectResourceCachedValueProvider<Map<A, List<B>>, Map<A, List<B>>> {

    public MergedMapValueProvider(LayoutBindingProjectComponent component, ModificationTracker... additionalTrackers) {
      super(component, additionalTrackers);
    }

    @NotNull
    @Override
    protected Map<A, List<B>> merge(List<Map<A, List<B>>> results) {
      Map<A, List<B>> merged = Maps.newHashMap();
      for (Map<A, List<B>> result : results) {
        for (Map.Entry<A, List<B>> entry : result.entrySet()) {
          List<B> bList = merged.get(entry.getKey());
          if (bList == null) {
            bList = new ArrayList<>();
            merged.put(entry.getKey(), bList);
          }
          bList.addAll(entry.getValue());
        }
      }
      return merged;
    }
  }
}
