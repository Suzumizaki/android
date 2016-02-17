/*
 * Copyright (C) 2015 The Android Open Source Project
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
 *
 * THIS FILE WAS GENERATED BY codergen. EDIT WITH CARE.
 */
package com.android.tools.idea.editors.gfxtrace.service.path;

import org.jetbrains.annotations.NotNull;

import com.android.tools.rpclib.binary.*;
import com.android.tools.rpclib.schema.*;

import java.io.IOException;

public final class MeshPath extends Path {
  @Override
  public StringBuilder stringPath(StringBuilder builder) {
    return myObject.stringPath(builder).append("<mesh>");
  }

  @Override
  public Path getParent() {
    return myObject;
  }

  //<<<Start:Java.ClassBody:1>>>
  private Path myObject;
  private MeshPathOptions myOptions;

  // Constructs a default-initialized {@link MeshPath}.
  public MeshPath() {}


  public Path getObject() {
    return myObject;
  }

  public MeshPath setObject(Path v) {
    myObject = v;
    return this;
  }

  public MeshPathOptions getOptions() {
    return myOptions;
  }

  public MeshPath setOptions(MeshPathOptions v) {
    myOptions = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }


  private static final Entity ENTITY = new Entity("path", "Mesh", "", "");

  static {
    ENTITY.setFields(new Field[]{
      new Field("Object", new Interface("Path")),
      new Field("Options", new Pointer(new Struct(MeshPathOptions.Klass.INSTANCE.entity()))),
    });
    Namespace.register(Klass.INSTANCE);
  }
  public static void register() {}
  //<<<End:Java.ClassBody:1>>>
  public enum Klass implements BinaryClass {
    //<<<Start:Java.KlassBody:2>>>
    INSTANCE;

    @Override @NotNull
    public Entity entity() { return ENTITY; }

    @Override @NotNull
    public BinaryObject create() { return new MeshPath(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      MeshPath o = (MeshPath)obj;
      e.object(o.myObject.unwrap());
      e.object(o.myOptions);
    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      MeshPath o = (MeshPath)obj;
      o.myObject = Path.wrap(d.object());
      o.myOptions = (MeshPathOptions)d.object();
    }
    //<<<End:Java.KlassBody:2>>>
  }
}
