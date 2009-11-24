package uk.ac.ebi.interpro.scan.jms.monitor;

import uk.ac.ebi.interpro.scan.jms.worker.WorkerState;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

import org.springframework.beans.factory.annotation.Required;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * Controller class for the Worker Monitor Swing Application
 * Controller for WorkerMonitorGUI.java
 *
 * @author Phil Jones
 * @version $Id: WorkerMonitorController.java,v 1.2 2009/10/29 17:58:00 pjones Exp $
 * @since 1.0
 */
public class WorkerMonitorController {

    private JPanel mainLayoutManager;
    private JTable workerDisplayTable;
    private JToolBar mainToolBar;
    private JSpinner refreshRateSpinner;
    private JButton shutdownAllButton;
    private JButton killAllButton;

    private long refreshInterval;

    private MonitorSwingWorker swingWorker;

    private WorkerStateTableModel stateTableModel;

    @Required
    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    @Required
    public void setSwingWorker(MonitorSwingWorker swingWorker) {
        this.swingWorker = swingWorker;
    }

    /**
     * This UUID is used to identifiy this monitor application
     * instance, so several users could run monitors without
     * clashing. (The textualised version of the UUID will
     * be placed in the JMS header to allow filtering
     * of messages).
     */
    private UUID monitorId = UUID.randomUUID();

    public UUID getMonitorId() {
        return monitorId;
    }

//    public static void main(String[] args) {
//        WorkerMonitorController wmc = new WorkerMonitorController();
//        wmc.run();
//    }

    public void run() {

        // Start the SwingWorker that will monitor the worker status
        swingWorker.setController(this);
        swingWorker.execute();

        // Create and display the GUI
        JFrame mainFrame = new JFrame("InterProScan 5 Worker Monitor");
        mainLayoutManager.setDoubleBuffered(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.add(mainLayoutManager);

        refreshRateSpinner.setModel(new SpinnerNumberModel(refreshInterval, 500, 20000, 100));
        refreshRateSpinner.addChangeListener(new RefreshSpinnerListener());
        stateTableModel = new WorkerStateTableModel(new ArrayList<WorkerState>());
        workerDisplayTable.setModel(stateTableModel);
        // Inject suitable renderers for the non-text columns.
        setUpProgressColumn (workerDisplayTable.getColumnModel().getColumn(WorkerStateTableModel.Column.PROGRESS.getColumnNumber()));
        setUpButtonColumn(workerDisplayTable.getColumnModel().getColumn(WorkerStateTableModel.Column.SHUTDOWN.getColumnNumber()));
        setUpButtonColumn(workerDisplayTable.getColumnModel().getColumn(WorkerStateTableModel.Column.KILL.getColumnNumber()));
        try {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
            System.exit(3);
        }
        SwingUtilities.updateComponentTreeUI(mainFrame);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void setUpProgressColumn(TableColumn progressColumn) {
        TableCellRenderer renderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column) {
                JProgressBar progressBar = new JProgressBar(0,100);
                if (value != null){
                    double valAsPercentage = ((Double) value) * 100d;
                    progressBar.setValue((int)valAsPercentage);
                }
                return progressBar;
            }
        };
        progressColumn.setCellRenderer(renderer);
    }

    private void setUpButtonColumn(TableColumn buttonColumn) {
        TableCellRenderer renderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column) {
                return new JButton((String)value);
            }
        };
        buttonColumn.setCellRenderer(renderer);
    }

    public void setStatus(List<WorkerState> workerStates) {
        stateTableModel.setWorkerStates(workerStates);
        workerDisplayTable.repaint();
//        TableModel tableModel = new WorkerStateTableModel (workerStates);
//        workerDisplayTable.setModel(tableModel);
//        // Inject suitable renderers for the non-text columns.
//        setUpProgressColumn (
//                workerDisplayTable.getColumnModel().getColumn(WorkerStateTableModel.Column.PROGRESS.getColumnNumber())
//                );
//
//        setUpButtonColumn(
//                workerDisplayTable.getColumnModel().getColumn(WorkerStateTableModel.Column.SHUTDOWN.getColumnNumber())
//        );
//        setUpButtonColumn(
//                workerDisplayTable.getColumnModel().getColumn(WorkerStateTableModel.Column.KILL.getColumnNumber())
//        );
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    private class RefreshSpinnerListener implements ChangeListener {
        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner refreshSpinner = (JSpinner)e.getSource();
            Double newRateAsDouble = (Double)refreshSpinner.getModel().getValue();
            // TODO - why am I storing this here?
            refreshInterval = newRateAsDouble.longValue();
            swingWorker.setRefreshInterval(refreshInterval);
        }
    }
}
