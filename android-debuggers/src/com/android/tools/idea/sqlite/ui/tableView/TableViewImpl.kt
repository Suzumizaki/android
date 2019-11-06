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
package com.android.tools.idea.sqlite.ui.tableView

import com.android.tools.adtui.stdui.CommonButton
import com.android.tools.idea.sqlite.model.SqliteColumn
import com.android.tools.idea.sqlite.model.SqliteColumnValue
import com.android.tools.idea.sqlite.model.SqliteRow
import com.android.tools.idea.sqlite.ui.notifyError
import com.android.tools.idea.sqlite.ui.setResultSetTableColumns
import com.android.tools.idea.sqlite.ui.setupResultSetTable
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.Vector
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

/**
 * Abstraction on the UI component used to display tables.
 */
class TableViewImpl : TableView {
  private val columnClass = SqliteColumnValue::class.java
  private val tableModel: DefaultTableModel by lazy {
    object : DefaultTableModel() {
      override fun getColumnClass(columnIndex: Int): Class<*> = columnClass
    }
  }

  private val listeners = mutableListOf<TableViewListener>()
  private val pageSizeDefaultValues = listOf(5, 10, 20, 25, 50)
  private var isLoading = false

  private lateinit var columns: List<SqliteColumn>

  private val panel = TablePanel()
  override val component: JComponent = panel.root

  private val firstRowsPageButton = CommonButton("First", AllIcons.Actions.Play_first)
  private val lastRowsPageButton = CommonButton("Last", AllIcons.Actions.Play_last)

  private val previousRowsPageButton = CommonButton("Previous", AllIcons.Actions.Play_back)
  private val nextRowsPageButton = CommonButton("Next", AllIcons.Actions.Play_forward)

  private val pageSizeComboBox = ComboBox<Int>()

  init {
    firstRowsPageButton.toolTipText = "First"
    panel.controlsPanel.add(firstRowsPageButton)
    firstRowsPageButton.addActionListener { listeners.forEach { it.loadFirstRowsInvoked() }}

    previousRowsPageButton.toolTipText = "Previous"
    panel.controlsPanel.add(previousRowsPageButton)
    previousRowsPageButton.addActionListener { listeners.forEach { it.loadPreviousRowsInvoked() }}

    pageSizeComboBox.isEditable = true
    pageSizeDefaultValues.forEach { pageSizeComboBox.addItem(it) }
    panel.controlsPanel.add(pageSizeComboBox)
    pageSizeComboBox.addActionListener { listeners.forEach { it.rowCountChanged((pageSizeComboBox.selectedItem as Int)) } }

    nextRowsPageButton.toolTipText = "Next"
    panel.controlsPanel.add(nextRowsPageButton)
    nextRowsPageButton.addActionListener { listeners.forEach { it.loadNextRowsInvoked() }}

    lastRowsPageButton.toolTipText = "Last"
    panel.controlsPanel.add(lastRowsPageButton)
    lastRowsPageButton.addActionListener { listeners.forEach { it.loadLastRowsInvoked() }}

    panel.table.tableHeader.defaultRenderer = HeaderRenderer()

    panel.table.tableHeader.addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(e: MouseEvent) {
        if (isLoading) return

        val point = e.point
        val columnIndex = panel.table.columnAtPoint(point)
        listeners.forEach { it.toggleOrderByColumnInvoked(columns[columnIndex]) }
      }
    })
  }

  override fun showPageSizeValue(maxRowCount: Int) {
    pageSizeComboBox.selectedItem = maxRowCount
  }

  override fun resetView() {
    tableModel.dataVector.clear()
    tableModel.columnCount = 0
    removeRows()
  }

  override fun removeRows() {
    tableModel.rowCount = 0
  }

  override fun startTableLoading() {
    panel.table.setupResultSetTable(tableModel, columnClass)
    tableModel.rowCount = 0
    tableModel.columnCount = 0
    panel.table.setPaintBusy(true)

    isLoading = true
  }

  override fun showTableColumns(columns: List<SqliteColumn>) {
    this.columns = columns
    columns.forEach { tableModel.addColumn(it.name) }
    panel.table.setResultSetTableColumns()
  }

  override fun showTableRowBatch(rows: List<SqliteRow>) {
    rows.forEach { row ->
      val values = Vector<SqliteColumnValue>()
      row.values.forEach { values.addElement(it) }
      tableModel.addRow(values)
    }
  }

  override fun stopTableLoading() {
    panel.table.setPaintBusy(false)

    isLoading = false
  }

  override fun reportError(message: String, t: Throwable?) {
    notifyError(message, t)
  }

  override fun setFetchPreviousRowsButtonState(enable: Boolean) {
    previousRowsPageButton.isEnabled = enable
    firstRowsPageButton.isEnabled = enable
  }

  override fun setFetchNextRowsButtonState(enable: Boolean) {
    nextRowsPageButton.isEnabled = enable
    lastRowsPageButton.isEnabled = enable
  }

  override fun addListener(listener: TableViewListener) {
    listeners.add(listener)
  }

  override fun removeListener(listener: TableViewListener) {
    listeners.remove(listener)
  }

  private class HeaderRenderer : TableCellRenderer {
    private val columnNameLabel = DefaultTableCellRenderer()
    private val panel = JPanel(BorderLayout())

    init {
      val sortIcon = DefaultTableCellRenderer()
      sortIcon.icon = AllIcons.General.ArrowSplitCenterV
      columnNameLabel.icon = AllIcons.Nodes.DataColumn
      columnNameLabel.iconTextGap = 8

      panel.background = Color(0, 0, 0, 0)
      panel.add(columnNameLabel, BorderLayout.CENTER)
      panel.add(sortIcon, BorderLayout.EAST)
    }

    override fun getTableCellRendererComponent(
      table: JTable,
      value: Any,
      selected: Boolean,
      focused: Boolean,
      viewRowIndex: Int,
      viewColumnIndex: Int
    ): Component {
      columnNameLabel.text = value as String
      return panel
    }
  }
}