package uk.ac.ebi.interpro.scan.jms.monitor;

import uk.ac.ebi.interpro.scan.jms.worker.WorkerState;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;

import javax.swing.table.TableModel;
import javax.swing.event.TableModelListener;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Table Model that defines the contents of the display table.
 *
 * @author Phil Jones
 * @version $Id: WorkerStateTableModel.java,v 1.2 2009/10/29 17:58:00 pjones Exp $
 * @since 1.0
 */
public class WorkerStateTableModel implements TableModel {

    List<WorkerState> workerStates;

    public WorkerStateTableModel(List<WorkerState> workerStates) {
        this.workerStates = workerStates;
    }

    /**
     * Alter the List backing the model - will this change the display?
     * @param workerStates
     */
    public void setWorkerStates(List<WorkerState> workerStates) {
        this.workerStates = workerStates;
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    @Override
    public int getRowCount() {
        return workerStates.size();
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>.  This is used
     * to initialize the table's column header name.  Note: this name does
     * not need to be unique; two columns in a table can have the same name.
     *
     * @return the name of the column
     * @param    columnIndex    the index of the column
     */
    @Override
    public String getColumnName(int columnIndex) {
        return Column.getColumnByColumnNumber(columnIndex).getColumnName();
    }

    /**
     * Returns the most specific superclass for all the cell values
     * in the column.  This is used by the <code>JTable</code> to set up a
     * default renderer and editor for the column.
     *
     * @param columnIndex the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Column.getColumnByColumnNumber(columnIndex).getColumnClass();
    }

    /**
     * Returns true if the cell at <code>rowIndex</code> and
     * <code>columnIndex</code>
     * is editable.  Otherwise, <code>setValueAt</code> on the cell will not
     * change the value of that cell.
     *
     * @param    rowIndex    the row whose value to be queried
     * @param    columnIndex    the column whose value to be queried
     * @return true if the cell is editable
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param    rowIndex    the row whose value is to be queried
     * @param    columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // Get the right worker state for the row.
        WorkerState state = workerStates.get(rowIndex);

        if (state == null){
            throw new IllegalStateException ("The List of WorkerStates includes a null value at row "+ rowIndex + " which should not be possible - coding error.");
        }
        // Now set the values for the columns.
        switch (columnIndex){
            case 0:
                return state.getHostName();
            case 1:
                return state.getWorkerIdentification();
            case 2:
                return state.getTimeAliveMillis();
            case 3:
                return state.getJobId();
            case 4:
                return state.getJobDescription();
            case 5:
                return state.getStepExecutionStatus().toString();
            case 6:
                return state.getProportionComplete();
            case 7:
                return state.getWorkerStatus();
            case 8:
                return (state.isSingleUseOnly()) ? "Single" : "Multiple";
            case 9:
                String exceptionMessage = "No Problems";
                if (state.getExceptionThrown() != null){
                    exceptionMessage = state.getExceptionThrown().getMessage();
                }
                return exceptionMessage;
            case 10:
                return "Shutdown";
            case 11:
                return "Kill";
            default:
                throw new IllegalArgumentException ("There is no column number "+ columnIndex + " defined. (Programming error");
        }
    }

    /**
     * Sets the value in the cell at <code>columnIndex</code> and
     * <code>rowIndex</code> to <code>aValue</code>.
     *
     * @param    aValue         the new value
     * @param    rowIndex     the row whose value is to be changed
     * @param    columnIndex the column whose value is to be changed
     * @see #getValueAt
     * @see #isCellEditable
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // Nothing yet.
    }

    /**
     * Adds a listener to the list that is notified each time a change
     * to the data model occurs.
     *
     * @param    l        the TableModelListener
     */
    @Override
    public void addTableModelListener(TableModelListener l) {
        // Nothing yet.
    }

    /**
     * Removes a listener from the list that is notified each time a
     * change to the data model occurs.
     *
     * @param    l        the TableModelListener
     */
    @Override
    public void removeTableModelListener(TableModelListener l) {
        // Nothing yet.
    }

    enum Column{

        HOST(0, "Host", String.class),
        JVM_ID(1, "JVM ID", String.class),
        TIME_ALIVE(2, "Time Alive (ms)", Long.class),
        JOB_ID(3, "Step ID", String.class),
        JOB_DESCRIPTION(4, "Step Description", String.class),
        JOB_STATUS(5, "Step Status", String.class),
        PROGRESS(6, "Progress", Double.class),
        STATUS(7, "Worker Status", String.class),
        SINGLE_USE_ONLY(8, "Single Use Only?", String.class),
        EXCEPTIONS(9, "Exceptions", String.class),
        SHUTDOWN(10, "", String.class),
        KILL(11, "", String.class)
        ;

        private static final Map <Integer, Column> columnNumberToColumnMap = new HashMap<Integer, Column> (Column.values().length);

        static {
            for (Column column : values()){
                columnNumberToColumnMap.put (column.columnNumber, column);
            }
        }

        private int columnNumber;

        private String columnName;

        private Class clazz;

        static Column getColumnByColumnNumber (int columnNumber){
            return columnNumberToColumnMap.get(columnNumber);
        }

        private Column (int columnNumber, String columnName, Class clazz){
            this.columnNumber = columnNumber;
            this.columnName = columnName;
            this.clazz = clazz;
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        public String getColumnName() {
            return columnName;
        }

        public Class getColumnClass() {
            return clazz;
        }
    }
}
