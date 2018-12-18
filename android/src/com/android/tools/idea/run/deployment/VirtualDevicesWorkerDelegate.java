/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea.run.deployment;

import com.android.tools.idea.avdmanager.AvdManagerConnection;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.swing.SwingWorker;
import org.jetbrains.annotations.NotNull;

final class VirtualDevicesWorkerDelegate extends SwingWorker<Collection<VirtualDevice>, Void> {
  @NotNull
  @Override
  protected Collection<VirtualDevice> doInBackground() {
    return AvdManagerConnection.getDefaultAvdManagerConnection().getAvds(true).stream()
      .map(VirtualDevice::newDisconnectedVirtualDevice)
      .collect(Collectors.toList());
  }
}