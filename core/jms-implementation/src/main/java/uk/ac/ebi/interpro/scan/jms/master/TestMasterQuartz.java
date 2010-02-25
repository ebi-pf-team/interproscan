package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.QueueJumper;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;

import javax.jms.JMSException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: 06-Nov-2009
 * Time: 16:25:45
 */

public class TestMasterQuartz implements Master  {

    private static final Logger LOGGER = Logger.getLogger(TestMasterQuartz.class);

    private String jmsBrokerHostName;

    private int jmsBrokerPort;

    private String jobSubmissionQueueName;

    private ResponseMonitor responseMonitor;

    private String managementRequestTopicName;

    //Quartz SchedulerFactory
    private SchedulerFactory sf = new StdSchedulerFactory();

    /**
     * Sets the task submission queue name.  This is the queue that new
     * jobs are placed on to, prior to be pushed on to the requestQueue
     * from where they are taken by a worker node.
     * @param jobSubmissionQueueName
     */
    @Required
    public void setJobSubmissionQueueName(String jobSubmissionQueueName) {
        this.jobSubmissionQueueName = jobSubmissionQueueName;
    }

    @Required
    public void setJmsBrokerHostName(String jmsBrokerHostName) {
        this.jmsBrokerHostName = jmsBrokerHostName;
    }

    @Required
    public void setJmsBrokerPort(int jmsBrokerPort) {
        this.jmsBrokerPort = jmsBrokerPort;
    }

    /**
     * Sets the name of the topic to which Worker management requests
     * should be sent, for multicast to all of the Worker clients.
     *
     * @param managementRequestTopicName the name of the topic to which Worker management requests
     *                                   should be sent, for multicast to all of the Worker clients.
     */
    @Override
    public void setManagementRequestTopicName(String managementRequestTopicName) {
        this.managementRequestTopicName = managementRequestTopicName;
    }

    /**
     * Sets the ResponseMonitor which will handle any responses from
     * the Worker nodes.
     * @param responseMonitor which will handle any responses from
     * the Worker nodes.
     */
    @Required
    public void setResponseMonitor(ResponseMonitor responseMonitor){
        this.responseMonitor = responseMonitor;
    }

    /**
     * Run the Master Application.
     */
    public void start(){
        SessionHandler sessionHandler = null;
        try {
            // Start the response monitor thread
            Thread responseMonitorThread = new Thread(responseMonitor);
            responseMonitorThread.start();

            Log log = LogFactory.getLog(InterProScanMaster.class);

            // Initialise the sessionHandler for the master thread
            sessionHandler = new SessionHandler(jmsBrokerHostName, jmsBrokerPort);

            //Initialise the Scheduler with SchedulerFactory
            Scheduler sched = sf.getScheduler();

            sched.start();

            // This sets the trigger to fire every 10 seconds - the trigger is created with a time to fire, then the trigger is loaded with a job which gets arated at the assigned time
            Date runTime = TriggerUtils.getNextGivenSecondDate(new Date(), 10);

            // define the job and tie it to our MessageJob class
            for (int i = 1; i <= 10; i++) {
                JobDetail job = new JobDetail("job" + i, "group" + i, MessageJob.class);
                job.getJobDataMap().put(MessageJob.DESTINATION, jobSubmissionQueueName);
                job.getJobDataMap().put(MessageJob.MESSAGE_STRING, "Message number "+i);
                job.getJobDataMap().put(MessageJob.SESSION_HANDLER, sessionHandler);
                //Create trigger with time to fire
                SimpleTrigger trigger = new SimpleTrigger("trigger" + i, "group" + i, runTime);
                Thread.sleep(1000);
                // Tell quartz to schedule the job using our trigger
                sched.scheduleJob(job, trigger);
                log.info(job.getFullName() + " will run at: " + runTime);
                runTime = TriggerUtils.getNextGivenSecondDate(new Date(), 10);
            }
            //Only proceed to shutdown once all jobs have been started
            while (sched.getJobGroupNames().length>0) {
                Thread.sleep(100);
            }
            sched.shutdown(true);
        } catch (JMSException e) {
            LOGGER.error ("JMSException thrown by TestMasterQuartz", e);
        } catch (InterruptedException e) {
            LOGGER.error ("InterruptedException thrown by TestMasterQuartz", e);
        } catch (SchedulerException e) {
            LOGGER.error ("SchedulerException thrown by TestMasterQuartz", e);
        }

        finally {
            if (sessionHandler != null){
                try {
                    sessionHandler.close();
                } catch (JMSException e) {
                    LOGGER.error ("JMSException thrown by TestMasterQuartz when attempting to close SessionHandler.", e);
                }
            }
        }
    }

    @Override
    public void setQueueJumper(QueueJumper queueJumper) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSerialWorkerRunner(WorkerRunner serialWorkerRunner) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
