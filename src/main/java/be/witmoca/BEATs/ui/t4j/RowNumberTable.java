package be.witmoca.BEATs.ui.t4j;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.*;
import java.util.List;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.*;
import javax.swing.table.*;

/*
 *	Use a JTable as a renderer for row numbers of a given main table.
 *  This table must be added to the row header of the scrollpane that
 *  contains the main table.
 *  
 *  Contains custom additions by Witmoca
 *  
 *  Note: is capable of tracking a rowsorter (selection change), but only if the rowsorter exists beforehand
 */
public class RowNumberTable extends JTable implements ChangeListener, PropertyChangeListener, TableModelListener {
	private static final long serialVersionUID = 1L;
	
	private final List<? extends SortKey> defaultKeys;
	
	private JTable main;

	/**
	 * 
	 * @param table The table to extend the row numbers to
	 * @param defaultKeys Pressing the left header sets the sortingkeys to this parameter (NULL = turn off sort when this cell is pressed)
	 */
	public RowNumberTable(JTable table, List<? extends SortKey> defaultKeys) {
		main = table;
		this.defaultKeys = defaultKeys;
		main.addPropertyChangeListener(this);
		main.getModel().addTableModelListener(this);

		setFocusable(false);
		setAutoCreateColumnsFromModel(false);
		setSelectionModel(main.getSelectionModel());

		TableColumn column = new TableColumn();
		column.setHeaderValue("# (No sort)");
		addColumn(column);
		column.setCellRenderer(new RowNumberRenderer());

		getColumnModel().getColumn(0).setPreferredWidth(50);
		setPreferredScrollableViewportSize(getPreferredSize());
		tableHeader.setReorderingAllowed(false);

		// If the table has a rowsorter => track it for changes (and set the defaultsortkeys)
		if (table.getRowSorter() != null) {
			table.getRowSorter().addRowSorterListener(new RowSorterPainter());
			table.getRowSorter().setSortKeys(defaultKeys);
		}

		this.getTableHeader().addMouseListener(new HeaderClickListener(table));
	}

	@Override
	public void addNotify() {
		super.addNotify();

		Component c = getParent();

		// Keep scrolling of the row table in sync with the main table.

		if (c instanceof JViewport) {
			JViewport viewport = (JViewport) c;
			viewport.addChangeListener(this);
		}
	}

	/*
	 * Delegate method to main table
	 */
	@Override
	public int getRowCount() {
		return main.getRowCount();
	}

	@Override
	public int getRowHeight(int row) {
		int rowHeight = main.getRowHeight(row);

		if (rowHeight != super.getRowHeight(row)) {
			super.setRowHeight(row, rowHeight);
		}

		return rowHeight;
	}

	/*
	 * No model is being used for this table so just use the row number as the value
	 * of the cell.
	 */
	@Override
	public Object getValueAt(int row, int column) {
		return Integer.toString(row + 1);
	}

	/*
	 * Don't edit data in the main TableModel by mistake
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/*
	 * Do nothing since the table ignores the model
	 */
	@Override
	public void setValueAt(Object value, int row, int column) {
	}

	//
	// Implement the ChangeListener
	//
	public void stateChanged(ChangeEvent e) {
		// Keep the scrolling of the row table in sync with main table

		JViewport viewport = (JViewport) e.getSource();
		JScrollPane scrollPane = (JScrollPane) viewport.getParent();
		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
	}

	//
	// Implement the PropertyChangeListener
	//
	public void propertyChange(PropertyChangeEvent e) {
		// Keep the row table in sync with the main table

		if ("selectionModel".equals(e.getPropertyName())) {
			setSelectionModel(main.getSelectionModel());
		}

		if ("rowHeight".equals(e.getPropertyName())) {
			repaint();
		}

		if ("model".equals(e.getPropertyName())) {
			main.getModel().addTableModelListener(this);
			revalidate();
		}
	}

	//
	// Implement the TableModelListener
	//
	@Override
	public void tableChanged(TableModelEvent e) {
		revalidate();
	}

	/*
	 * Attempt to mimic the table header renderer
	 */
	private static class RowNumberRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RowNumberRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (table != null) {
				JTableHeader header = table.getTableHeader();

				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			if (isSelected) {
				setFont(getFont().deriveFont(Font.BOLD));
			}

			setText((value == null) ? "" : value.toString());
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));

			return this;
		}
	}

	/**
	 * When the sorter gets changed, the table updates
	 * The rownumbers could possible be dirty (or the amount changed, etc)
	 * => revalidate and repaint
	 */
	private class RowSorterPainter implements RowSorterListener {
		@Override
		public void sorterChanged(RowSorterEvent e) {
			revalidate();
			repaint();
		}
	}

	/**
	 * Clears the sorting keys of the accompanying table if a sorter exists
	 */
	private class HeaderClickListener implements MouseListener {
		private final JTable rowTable;

		public HeaderClickListener(JTable rowTable) {
			this.rowTable = rowTable;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (rowTable.getRowSorter() != null) {
				rowTable.getRowSorter().setSortKeys(defaultKeys);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

	}
}
