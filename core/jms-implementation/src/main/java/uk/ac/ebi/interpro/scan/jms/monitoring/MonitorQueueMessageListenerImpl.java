package uk.ac.ebi.interpro.scan.jms.monitoring;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerState;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Listener implementation for the monitor queue (activeMQ identifier: monitorQueue). See file activemq-queue-config-context.xml.
 *
 * @author Maxim Scheremetjew
 * @author Gift Nuka
 *
 * @since 1.0
 */
public class MonitorQueueMessageListenerImpl implements MessageListener {


    private static final Logger LOGGER = LogManager.getLogger(MonitorQueueMessageListenerImpl.class.getName());

    private StatsUtil statsUtil;

    private long lastStatsDisplayTime = System.currentTimeMillis();

    private long timeSinceLastStepsDisplay = 0;

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    @Override
    public void onMessage(Message message) {
        LOGGER.info("Master received a message on the monitor queue...");
        if (message instanceof TextMessage) {
            try {
                TextMessage textMessage = (TextMessage) message;
                System.out.println("messageText = " + textMessage.getText());
            } catch (JMSException e) {
                LOGGER.warn("Cannot get text message!", e);
            }
        }else if (message instanceof ObjectMessage){
            ObjectMessage objectMessage = (ObjectMessage) message;

            Object messageContents = null;
            try {
                messageContents = objectMessage.getObject();

                if (messageContents instanceof WorkerState) {
                    WorkerState workerState = (WorkerState) messageContents;
                    statsUtil.updateWorkerStateMap(workerState);
                    LOGGER.debug("worker state received: \n"
                            + workerState.toString());
                }
            } catch (JMSException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        Long timeSinceLastDisplay = System.currentTimeMillis() - lastStatsDisplayTime;

        if(timeSinceLastDisplay > 30 * 60 * 1000) {
            timeSinceLastStepsDisplay += timeSinceLastDisplay;
            LOGGER.debug("MonitorQueueMessageListenerImpl: total workers: " + statsUtil.getWorkerStateMap().size() );
            int unfinishedStepsCount = 0;
            int totalSteps = 0;
            List<StepExecution> unfinishedSteps = new ArrayList<StepExecution>();
            Collection<WorkerState> workerStateCollection = statsUtil.getWorkerStateMap().values();
            for(WorkerState worker:workerStateCollection){
                unfinishedStepsCount += worker.getUnfinishedStepCount();
                totalSteps += worker.getTotalStepCount();
                if(timeSinceLastStepsDisplay > 40 * 60 * 1000){
                    unfinishedSteps.addAll(worker.getNonFinishedJobs().values());
                }
            }
            Utilities.verboseLog(1100, "MonitorQueueMessageListenerImpl: total remote steps submitted: " + totalSteps
                    + " remote steps left: " + unfinishedStepsCount );
            if(timeSinceLastStepsDisplay > 120 * 60 * 1000){
                LOGGER.debug("MonitorQueueMessageListenerImpl: steps still running ");
                for(StepExecution stepExecution:unfinishedSteps){
                    LOGGER.debug("MQMLI:" + stepExecution.getStepInstance().toString());
                }
                timeSinceLastStepsDisplay = 0;
            }
        }
    }
}