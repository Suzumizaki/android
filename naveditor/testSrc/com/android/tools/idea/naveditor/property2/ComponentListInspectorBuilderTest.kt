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
package com.android.tools.idea.naveditor.property2

import com.android.tools.idea.common.model.NlComponent
import com.android.tools.idea.naveditor.NavModelBuilderUtil.navigation
import com.android.tools.idea.naveditor.NavTestCase
import com.android.tools.idea.naveditor.model.argumentName
import com.android.tools.idea.naveditor.property2.inspector.ActionListInspectorBuilder
import com.android.tools.idea.naveditor.property2.inspector.ArgumentInspectorBuilder
import com.android.tools.idea.naveditor.property2.inspector.DeepLinkInspectorBuilder
import com.android.tools.idea.naveditor.property2.ui.ComponentList
import com.android.tools.idea.naveditor.scene.decorator.HIGHLIGHTED_CLIENT_PROPERTY
import com.android.tools.idea.uibuilder.property2.NelePropertiesModel
import com.android.tools.idea.uibuilder.property2.NelePropertiesProvider
import com.android.tools.idea.uibuilder.property2.NelePropertyItem
import com.android.tools.property.panel.api.InspectorBuilder
import com.android.tools.property.panel.impl.model.util.FakeInspectorPanel
import com.android.tools.property.panel.impl.model.util.FakeLineType
import java.awt.event.FocusEvent
import javax.swing.ListModel

class ComponentListInspectorBuilderTest : NavTestCase() {
  fun testActionListInspectorBuilder() {
    val model = model("nav.xml") {
      navigation("root", startDestination = "fragment1") {
        fragment("fragment1") {
          action("action1", destination = "fragment2")
          action("action2", destination = "activity1")
          action("action3", destination = "fragment1")
        }
        fragment("fragment2")
        activity("activity1")
      }
    }

    val fragment1 = model.find("fragment1")!!
    val expected = arrayOf("action1", "action2", "action3").mapNotNull(model::find)
    val propertiesModel = NelePropertiesModel(myRootDisposable, myFacet)
    verifyPanel(fragment1, propertiesModel, ActionListInspectorBuilder(propertiesModel), expected)
  }

  fun testArgumentInspectorBuilder() {
    val model = model("nav.xml") {
      navigation("root", startDestination = "fragment1") {
        fragment("fragment1") {
          argument("argument1")
          argument("argument2")
          argument("argument3")
        }
      }
    }

    val fragment1 = model.find("fragment1")!!
    val expected = arrayOf("argument1", "argument2", "argument3").map { name -> fragment1.children.first { it.argumentName == name } }
    val propertiesModel = NelePropertiesModel(myRootDisposable, myFacet)
    verifyPanel(fragment1, propertiesModel, ArgumentInspectorBuilder(), expected)
  }

  fun testDeepLinkInspectorBuilder() {
    val model = model("nav.xml") {
      navigation("root", startDestination = "fragment1") {
        fragment("fragment1") {
          deeplink("deepLink1", "www.foo.com", true)
          deeplink("deepLink2", "www.bar.com", true)
          deeplink("deepLink3", "www.baz.com", true)
        }
      }
    }

    val fragment1 = model.find("fragment1")!!
    val expected = arrayOf("deepLink1", "deepLink2", "deepLink3").mapNotNull(model::find)
    val propertiesModel = NelePropertiesModel(myRootDisposable, myFacet)
    verifyPanel(fragment1, propertiesModel, DeepLinkInspectorBuilder(), expected)
  }

  private fun verifyPanel(component: NlComponent, propertiesModel: NelePropertiesModel,
                          builder: InspectorBuilder<NelePropertyItem>, expected: List<NlComponent>) {
    val provider = NelePropertiesProvider(myFacet)
    val propertiesTable = provider.getProperties(propertiesModel, null, listOf(component))
    val panel = FakeInspectorPanel()

    builder.attachToInspector(panel, propertiesTable)
    assertEquals(2, panel.lines.size)
    assertEquals(FakeLineType.TITLE, panel.lines[0].type)
    assertEquals(FakeLineType.PANEL, panel.lines[1].type)
    assertInstanceOf(panel.lines[1].component, ComponentList::class.java)
    val componentList = panel.lines[1].component as ComponentList

    val tableModel = componentList.list.model
    assertEquals(tableModel.size, expected.size)

    for ((i, expectedElement) in expected.withIndex()) {
      assertEquals(expectedElement, tableModel.getElementAt(i))
    }
  }

  fun testSelectionHighlighted() {
    val model = model("nav.xml") {
      navigation("root", startDestination = "fragment1") {
        fragment("fragment1") {
          action("action1", destination = "fragment2")
          action("action2", destination = "activity1")
          action("action3", destination = "fragment1")
        }
      }
    }

    val fragment1 = model.find("fragment1")!!

    val propertiesModel = NelePropertiesModel(myRootDisposable, myFacet)
    val provider = NelePropertiesProvider(myFacet)
    val propertiesTable = provider.getProperties(propertiesModel, null, listOf(fragment1))
    val panel = FakeInspectorPanel()
    val builder = ActionListInspectorBuilder(propertiesModel)
    builder.attachToInspector(panel, propertiesTable)

    val componentList = panel.lines[1].component as ComponentList
    val list = componentList.list
    val listModel = list.model

    list.selectedIndex = 0
    verifyClientProperties(listModel, true, false, false)

    list.selectedIndices = intArrayOf(1, 2)
    verifyClientProperties(listModel, false, true, true)

    list.focusListeners.forEach { it.focusLost(FocusEvent(list, FocusEvent.FOCUS_LOST)) }
    verifyClientProperties(listModel, false, false, false)
  }

  fun testUpdates() {
    val model = model("nav.xml") {
      navigation("root", startDestination = "fragment1") {
        fragment("fragment1") {
          action("action1", destination = "fragment1")
          action("action2", destination = "fragment1")
          action("action3", destination = "fragment1")
        }
      }
    }

    val fragment1 = model.find("fragment1")!!

    val propertiesModel = NelePropertiesModel(myRootDisposable, myFacet)
    val provider = NelePropertiesProvider(myFacet)
    val propertiesTable = provider.getProperties(propertiesModel, null, listOf(fragment1))
    val panel = FakeInspectorPanel()
    val builder = ActionListInspectorBuilder(propertiesModel)
    builder.attachToInspector(panel, propertiesTable)

    val lineModel = panel.lines[1]
    val componentList = lineModel.component as ComponentList
    val listModel = componentList.list.model
    assertEquals(3, listModel.size)

    fragment1.model.delete(listOf(fragment1.children[0]))
    lineModel.refresh()
    assertEquals(2, listModel.size)
  }

  private fun verifyClientProperties(model: ListModel<NlComponent>, vararg expectedValues: Boolean) {
    assertEquals(model.size, expectedValues.size)

    expectedValues.forEachIndexed { i, expected ->
      val property = model.getElementAt(i).getClientProperty(HIGHLIGHTED_CLIENT_PROPERTY)

      if (expected) {
        assertEquals(property, true)
      }
      else {
        assertNull(property)
      }
    }
  }
}

