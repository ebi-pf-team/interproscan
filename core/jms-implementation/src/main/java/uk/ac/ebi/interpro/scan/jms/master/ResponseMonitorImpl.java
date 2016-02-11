package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.List;

/**
 * This implementation receives responses on the destinationResponseQueue
 * and then delegates to the injected Handler to respond to them.
 *
 * @author Phil Jones
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class ResponseMonitorImpl implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(ResponseMonitorImpl.class.getName());

    StepExecutionDAO stepExecutionDAO;

    public static final String CAN_RUN_REMOTELY_PROPERTY = "remote";

    public int remoteJobs = 0;

    private StatsUtil statsUtil;

    public ResponseMonitorImpl(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }

    public int getRemoteJobs() {
        return remoteJobs;
    }

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    @Override
    public void onMessage(Message message) {
        LOGGER.info("Master: received a message on the  responseQueue");
        try {
            boolean canHandle = false;
            boolean canRunRemotely = false;
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object messageContents = objectMessage.getObject();
                if (messageContents instanceof StepExecution) {
                    canHandle = true;
                    StepExecution freshStepExecution = (StepExecution) messageContents;
                    stepExecutionDAO.refreshStepExecution(freshStepExecution);
                    Utilities.verboseLog(4, "ResponseMonitorImpl: " + freshStepExecution.getStepInstance().toString());
                    statsUtil.updateSubmittedStepInstances(freshStepExecution.getStepInstance());

                    canRunRemotely = message.getBooleanProperty(CAN_RUN_REMOTELY_PROPERTY);
                    if(canRunRemotely){
                        statsUtil.incRemoteJobsCompleted();

                        remoteJobs++;
//                        System.out.println("Remote Job completed, remoteJobs:" + remoteJobs + " statsutil.remotejobs: " + StatsUtil.getRemoteJobsCompleted() );
                    }else{
                        statsUtil.incLocalJobsCompleted();
                    }


                } else if (messageContents instanceof List){
                    canHandle = true;
                    List<Message> failedJobs =(List<Message>)    messageContents;
                    LOGGER.debug("Received FAILED_JOB");
                }
                statsUtil.updateLastMessageReceivedTime();
            }
            if (! canHandle){
                LOGGER.error("Master: received a message that I don't know how to handle.");
                throw new IllegalArgumentException("Don't know how to handle the message: " + message.toString());
            }
        }
        catch (JMSException jmse) {
            LOGGER.error("JMSException thrown by Response Monitor on the Master", jmse);
        }
    }
}
