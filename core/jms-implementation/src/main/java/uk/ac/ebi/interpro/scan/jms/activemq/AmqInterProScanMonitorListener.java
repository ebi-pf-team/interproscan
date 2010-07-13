package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import uk.ac.ebi.interpro.scan.management.model.Jobs;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AmqInterProScanMonitorListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(AmqInterProScanMonitorListener.class.getName());

    private Destination workerManagerResponseQueue;

    private AmqInterProScanWorker worker;

    private JmsTemplate jmsTemplate;

    private Jobs jobs;

    @Required
    public void setWorker(AmqInterProScanWorker worker) {
        this.worker = worker;
    }

    @Required
    public void setWorkerManagerResponseQueue(Destination workerManagerResponseQueue) {
        this.workerManagerResponseQueue = workerManagerResponseQueue;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }


    /**
     * TODO - needs to be re-written as the AmqInterProScanWorker may be used in multiple threads.
     *
     * @param message
     */
    public void onMessage(Message message) {
        /*try{
            if (message instanceof TextMessage){
                final TextMessage managementRequest = (TextMessage) message;
                managementRequest.acknowledge();     // TODO - when should message receipt be acknowledged?
                // Just echo back the managementRequest with the name of the worker host to uk.ac.ebi.interpro.scan.jms the multicast topic.
                final WorkerState workerState = new WorkerState(
                        System.currentTimeMillis() - worker.getStartTime(),
                        java.net.InetAddress.getLocalHost().getHostName(),
                        worker.getUniqueWorkerIdentification(),
                        false
                );
                workerState.setJobId("Unique Job ID as passed from the broker in the JMS header. (TODO)");
                workerState.setProportionComplete(worker.getProportionOfWorkDone());
                workerState.setWorkerStatus("Running");
                StepExecution stepExecution = worker.getCurrentStepExecution();
                if (stepExecution == null){
                    workerState.setStepExecutionState(null);
                    workerState.setJobId("-");
                    workerState.setJobDescription("-");
                }
                else {
                    workerState.setStepExecutionState(stepExecution.getState());
                    workerState.setJobId(stepExecution.getId().toString());
                    workerState.setJobDescription(stepExecution.getStepInstance().getStep(jobs).getStepDescription());
                }


                jmsTemplate.convertAndSend(workerManagerResponseQueue,
                        workerState, new MessagePostProcessor(){

                            @Override
                            public Message postProcessMessage(Message message) throws JMSException {
                                if (managementRequest.propertyExists(WorkerMonitor.REQUESTEE_PROPERTY)){
                                    message.setStringProperty(WorkerMonitor.REQUESTEE_PROPERTY,
                                            managementRequest.getStringProperty(WorkerMonitor.REQUESTEE_PROPERTY));
                                }
                                return message;
                            }
                        });
            }

        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by InterProScanMonitorListener", e);
        } catch (UnknownHostException e) {
            LOGGER.error("UnknownHostException thrown by InterProScanMonitorListener", e);
        }*/
    }
}
