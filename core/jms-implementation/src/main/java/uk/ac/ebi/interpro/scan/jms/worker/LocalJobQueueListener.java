package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.ac.ebi.interpro.scan.jms.activemq.StepExecutionTransaction;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This implementation receives responses on the destinationResponseQueue
 * and then propagates them to the super worker or master.
 *
 * @author nuka, scheremetjew
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class LocalJobQueueListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(LocalJobQueueListener.class.getName());
    private JmsTemplate localJmsTemplate;

    private Destination jobResponseQueue;

    private StepExecutionTransaction stepExecutor;

    boolean verboseLog = false;

    boolean testFailOnce = false;

    private int verboseLogLevel;

    /**
     * The distributed worker controller is in charge of calling 'stop' on the
     * Spring MonitorListenerContainer when the conditions are correct.
     *
     * @see uk.ac.ebi.interpro.scan.jms.worker.WorkerImpl
     */
    private WorkerImpl controller;

    private AtomicInteger jobCount = new AtomicInteger(0);

    private int inVmworkerNumber = 0;

    private StatsUtil statsUtil;

    public AtomicInteger getJobCount() {
        return jobCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = new AtomicInteger (jobCount);
    }

    public void setLocalJmsTemplate(JmsTemplate localJmsTemplate) {
        this.localJmsTemplate = localJmsTemplate;
    }

    public void setController(WorkerImpl controller) {
        this.controller = controller;
    }

    @Required
    public void setStepExecutor(StepExecutionTransaction stepExecutor) {
        this.stepExecutor = stepExecutor;
    }

    /**
     * Sets the JMS queue to which the Worker returns the results of a job.
     *
     * @param jobResponseQueue the name of the JMS queue to which the Worker returns the results of a job.
     */
    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    public void setInVmworkerNumber(int inVmworkerNumber) {
        this.inVmworkerNumber = inVmworkerNumber;
    }

    public boolean isVerboseLog() {
        return verboseLog;
    }

    public void setVerboseLog(boolean verboseLog) {
        this.verboseLog = verboseLog;
    }

    public int getVerboseLogLevel() {
        return verboseLogLevel;
    }

    public void setVerboseLogLevel(int verboseLogLevel) {
        this.verboseLogLevel = verboseLogLevel;
    }

    public StatsUtil getStatsUtil() {
        return statsUtil;
    }

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    @Override
    public void onMessage(final Message message) {
        jobCount.incrementAndGet();
        int localCount = jobCount.get();
        String timeNow = Utilities.getTimeNow();
	    long threadId = Thread.currentThread().getId();
        if(localCount == 1){
            Utilities.verboseLog("first transaction ... ");
        }
        if (inVmworkerNumber == 0){
            if (controller != null) {
                setInVmworkerNumber(controller.getInVmWorkeNumber());
            }else{
                try {
                    setInVmworkerNumber(message.getJMSMessageID().hashCode());
                } catch (JMSException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    setInVmworkerNumber(timeNow.hashCode());
                }
            }
        }
        final String debugToken = " DEBUG ";
//        Utilities.verboseLog(timeNow + debugToken + "worker-" + inVmworkerNumber + " job " + localCount);

        final String messageId;
        String stepName = "";

        try {
            messageId = message.getJMSMessageID();
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown in MessageListener when attempting to get the JMS Message ID.", e);
            return;
        }

        try {
            if (controller != null) {
                controller.jobStarted(messageId);
            }
            LOGGER.debug("Message received from queue.  JMS Message ID: " + message.getJMSMessageID() + " cmd:  " + message.toString());
            LOGGER.info("Message received from queue.  JMS Message ID: " + message.getJMSMessageID());

            if (!(message instanceof ObjectMessage)) {
                LOGGER.error("Received a message of an unknown type (non-ObjectMessage)");
                return;
            }
            final ObjectMessage stepExecutionMessage = (ObjectMessage) message;
            final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();



            if (stepExecution == null) {
                LOGGER.error("An ObjectMessage was received but had no contents.");
                return;
            }
            LOGGER.debug("Message received of queue - attempting to executeInTransaction");


            // TODO - Need to add a dead-letter queue, so if this worker never gets as far as
            // acknowledging the message, the Master will know to re-run the StepInstance.
            try {

                stepName =  stepExecution.getStepInstance().getStepId();
                Long stepId = stepExecution.getStepInstance().getId();
                statsUtil.jobStarted(stepName);
                final long now = System.currentTimeMillis();
                final String timeNow1 = Utilities.getTimeNow();
//                Utilities.verboseLog("verboseLogLevel :" + Utilities.verboseLogLevel);
                if (Utilities.verboseLogLevel > 2) {
                    Utilities.verboseLog("thread#: " + threadId + " Processing " + stepName + " JobCount #: " + localCount
                            + " - stepInstanceId = " + stepId
                            + "\n stepInstance: " + stepExecution.getStepInstance().toString());
                }
                //the following code was used to test high memory worker creation, might still be useful later
//                if (controller != null && ! testFailOnce){
//                    testFailOnce = true;
//                    throw new IllegalStateException("Exception for testing ....");
//                }
                stepExecutor.executeInTransaction(stepExecution, message);
                final long executionTime =   System.currentTimeMillis() - now;
                if(Utilities.verboseLogLevel > 2){
                    Utilities.verboseLog("thread#: " + threadId + " Finished Processing " + stepName + " JobCount #: " + localCount + " - stepInstanceId = " + stepId);
                    Utilities.verboseLog("Execution Time (ms) for job started " + timeNow1 + " JobCount #: " + localCount + " stepId: " + stepName + "  time: " + executionTime);
                }
                statsUtil.jobFinished(stepName);
            } catch (Exception e) {
                //todo: reinstate self termination for remote workers. Disabled to make process more robust for local workers.
                //            running = false;
                LOGGER.error("Execution thrown when attempting to executeInTransaction the StepExecution.  All database activity rolled back.", e);

                LOGGER.error("StepExecution with errors - stepName: " + stepName);

                // Something went wrong in the execution - try to send back failure
                // message to the broker.  This in turn may fail if it is the JMS connection
                // that failed during the execution.
                stepExecution.fail(e);

                localJmsTemplate.send(jobResponseQueue, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(stepExecution);
                    }
                });
                message.acknowledge(); // Acknowledge message following failure.
                LOGGER.debug("Message returned to the broker to indicate that the StepExecution has failed: " + stepExecution.getId());
            }

        } catch (JMSException e) {
            LOGGER.error("JMSException thrown in MessageListener.", e);
            if (controller != null) {
                controller.handleFailure(LocalJobQueueListener.class.getName());
            }
        } catch (Exception e) {
            LOGGER.error("JMSException thrown in MessageListener.", e);
            if (controller != null) {
                controller.handleFailure(LocalJobQueueListener.class.getName());
            }
        } finally {
            if (controller != null) {
                controller.jobFinished(messageId);
//                controller.workerState.addLocallyCompletedJob(message);
            }
        }
        if(localCount == 1){
            Utilities.verboseLog("first transaction ... done");
            Utilities.verboseLog("InterProScan analyses continue ....");
        }

    }

    public void getCurrentInVmWorkers(){

    }

}
