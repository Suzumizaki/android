/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.naveditor.property.editors;

import com.android.tools.idea.common.property.NlProperty;
import com.android.tools.idea.common.property.editors.BaseComponentEditor;
import com.android.tools.idea.uibuilder.property.EmptyProperty;
import com.android.tools.idea.uibuilder.property.editors.NlEditingListener;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class TextEditor extends BaseComponentEditor {
  private static final int HORIZONTAL_SPACE_AFTER_LABEL = 4;

  private final JPanel myPanel;
  private final JLabel myLabel;
  private final ComponentManager myProject;
  private final EditorTextField myTextEditor;

  private NlProperty myProperty;
  private String myLastReadValue;
  private Object myLastWriteValue;

  public TextEditor(@NotNull Project project,
                    @NotNull NlEditingListener listener) {
    super(listener);
    myPanel = new JPanel(new BorderLayout());

    myLabel = new JBLabel();
    myPanel.add(myLabel, BorderLayout.LINE_START);
    myPanel.setFocusable(false);
    myLabel.setBorder(JBUI.Borders.emptyRight(HORIZONTAL_SPACE_AFTER_LABEL));

    myProject = project;

    //noinspection UseDPIAwareInsets
    myTextEditor = new EditorTextField();

    myPanel.add(myTextEditor, BorderLayout.CENTER);
    myTextEditor.registerKeyboardAction(event -> stopEditing(getText()),
                                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    myTextEditor.registerKeyboardAction(event -> cancel(),
                                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    myTextEditor.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(@NotNull FocusEvent event) {
        myTextEditor.selectAll();
      }

      @Override
      public void focusLost(@NotNull FocusEvent event) {
        stopEditing(getText());
        // Remove the selection after we lose focus for feedback on which editor is the active editor
        myTextEditor.removeSelection();
      }
    });
    myProperty = EmptyProperty.INSTANCE;
  }

  protected EditorTextField getTextEditor() {
    return myTextEditor;
  }

  @NotNull
  private String getText() {
    return myTextEditor.getDocument().getText();
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (myProject.isDisposed()) {
      return;
    }

    myTextEditor.setEnabled(enabled);
    if (!enabled) {
      myLastReadValue = "";
      myLastWriteValue = "";
      myTextEditor.setText("");
    }
  }

  @NotNull
  @Override
  public NlProperty getProperty() {
    return myProperty;
  }

  @Override
  public void setProperty(@NotNull NlProperty property) {
    if (myProperty != property) {
      myProperty = property;
      myLastReadValue = null;
    }

    myPanel.add(myLabel, BorderLayout.LINE_START);

    String propValue = StringUtil.notNullize(myProperty.getValue());
    if (!propValue.equals(myLastReadValue)) {
      myLastReadValue = propValue;
      myLastWriteValue = propValue;
      myTextEditor.setText(propValue);
    }
    Color color = myProperty.isDefaultValue(myLastReadValue) ? DEFAULT_VALUE_TEXT_COLOR : CHANGED_VALUE_TEXT_COLOR;
    myTextEditor.setForeground(color);
  }

  @Override
  public void requestFocus() {
    myTextEditor.requestFocus();
    myTextEditor.selectAll();
    myTextEditor.scrollRectToVisible(myTextEditor.getBounds());
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @NotNull
  @Override
  public Object getValue() {
    return getText();
  }

  @Override
  public void stopEditing(@Nullable Object newValue) {
    // Update the selected value for immediate feedback from resource editor.
    myTextEditor.setText((String)newValue);
    // Select all the text to give visual confirmation that the value has been applied.
    if (myTextEditor.hasFocus()) {
      myTextEditor.selectAll();
    }

    if (!Objects.equals(newValue, myLastWriteValue)) {
      myLastWriteValue = newValue;
      myLastReadValue = null;
      super.stopEditing(newValue);
    }
  }

  protected void cancel() {
    // Update the selected value for immediate feedback from resource editor.
    myTextEditor.setText(myProperty.getValue());
    myTextEditor.selectAll();
    cancelEditing();
  }
}
