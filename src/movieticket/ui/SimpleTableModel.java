package movieticket.ui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/** The MODEL of MVC for every JTable (the JTable is the view, the listeners are the controller). */
public class SimpleTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private final String[] columnNames;
    private Vector<Object[]> rows = new Vector<Object[]>();

    public SimpleTableModel(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public void setRows(Vector<Object[]> newRows) {
        this.rows = newRows;
        fireTableDataChanged();               // tells the view to redraw
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        return rows.get(row)[column];
    }
}
