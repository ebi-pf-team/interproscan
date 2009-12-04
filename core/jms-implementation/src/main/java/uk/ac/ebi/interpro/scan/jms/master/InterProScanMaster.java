package uk.ac.ebi.interpro.scan.jms.master;

import org.springframework.beans.factory.annotation.Required;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.LoadFastaFile;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.*;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.RunHmmer3Step;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.RunHmmer3StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.ParseHMMER3OutputStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.ParseHMMER3OutputStepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA.Pfam_A_PostProcessingStepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA.Pfam_A_PostProcessingStep;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.persistence.SignatureDAO;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;
import uk.ac.ebi.interpro.scan.io.model.Hmmer3ModelLoader;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import java.util.*;
import java.io.IOException;

/**
 * Pretending to be the InterProScan master application.
 *
 * @author Phil Jones
 * @version $Id: TestMaster.java,v 1.4 2009/10/28 15:04:00 pjones Exp $
 * @since 1.0
 */
public class InterProScanMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(InterProScanMaster.class);

    private SessionHandler sessionHandler;

    private String jobSubmissionQueueName;

    private ResponseMonitor responseMonitor;

    private List<Job> jobs;

    private volatile Map<String, StepInstance> stepInstances = new HashMap<String, StepInstance>();

    private volatile Map<String, StepExecution> stepExecutions = new HashMap<String, StepExecution>();

    private String managementRequestTopicName;

    private LoadFastaFile loader;

    private DAOManager daoManager;

    private String pfamHMMfilePath;

    private MessageProducer producer;

    @Required
    public void setLoader(LoadFastaFile loader) {
        this.loader = loader;
    }

    /**
     * Sets the SessionHandler.  This looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     * @param sessionHandler  looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     */
    @Required
    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

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

    public List<Job> getJobs() {
        return jobs;
    }

    @Required
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setDaoManager(DAOManager daoManager) {
        this.daoManager = daoManager;
    }

    @Required
    public void setPfamHMMfilePath(String pfamHMMfilePath) {
        this.pfamHMMfilePath = pfamHMMfilePath;
    }

    /**
     * Run the Master Application.
     */
    public void start(){
        try {
            // Start the response monitor thread
            Thread responseMonitorThread = new Thread(responseMonitor);
            responseMonitor.setStepExecutionMap(stepExecutions);
            responseMonitorThread.start();
            // Initialise the sessionHandler for the master thread
            sessionHandler.init();

            producer = sessionHandler.getMessageProducer(jobSubmissionQueueName);
            buildStepInstancesTheStupidWay();
            System.out.println("Returned from building step instances method.");
            while(true){
//                sendMessage(jobSubmissionQueueName, "Message number " + i);  // Send a message every second or so.
                for (StepInstance stepInstance : stepInstances.values()){
                    if (stepInstance.canBeSubmitted()){
                        StepExecution stepExecution = stepInstance.createStepExecution();
                        stepExecutions.put(stepExecution.getId(), stepExecution);
                        // TODO - for the moment, just sending to the default job submission queue.
                        System.out.println("Submitting "+ stepExecution.getStepInstance().getStep().getStepDescription());
                        sendMessage(stepExecution);
                    }
                }
                Thread.sleep(2000);
            }

        } catch (JMSException e) {
            LOGGER.error ("JMSException", e);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error ("InterruptedException", e);
            System.exit(1);
        }
        finally {
            if (sessionHandler != null){
                try {
                    sessionHandler.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void buildStepInstancesTheStupidWay() {
        // TODO - Note that this method is just a HACK to create a structure of steps that
        // TODO - can be run for the demonstration - the mechanism to build a real set of
        // TODO - steps, following the addition of new proteins has yet to be written.

        Job job = jobs.iterator().next();
        // Load some proteins into the database.
        loader.loadSequences();

        // Load the models into the database.

        // Build a SignatureLibrary object.
        SignatureLibrary sigLibrary = new SignatureLibrary("Pfam", "Pfam database signatures.");
        LOGGER.debug("Storing SignatureLibrary...");
        // Store it.
        daoManager.getSignatureLibraryDAO().insert(sigLibrary);
        LOGGER.debug("Storing SignatureLibrary...DONE");
        // Now parse and retrieve the signatures.
        Hmmer3ModelLoader modelLoader = new Hmmer3ModelLoader(sigLibrary, "24.0");
        SignatureLibraryRelease release = null;
        try{
            release = modelLoader.parse(pfamHMMfilePath);
        } catch (IOException e) {
            LOGGER.debug("IOException thrown when parsing HMM file.");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        }

        // And store the Models / Signatures to the database.
        LOGGER.debug("Storing SignatureLibraryRelease...");
        daoManager.getSignatureLibraryReleaseDAO().insert(release);
        LOGGER.debug("Storing SignatureLibraryRelease...DONE");


        final long sliceSize = 500l;

        // Then retrieve the first 99.
        for (long bottomProteinId = 1; bottomProteinId <= 10000l; bottomProteinId += sliceSize){
            final long topProteinId = bottomProteinId + sliceSize;
            WriteFastaFileStepInstance fastaStepInstance = null;
            RunHmmer3StepInstance hmmer3StepInstance = null;
            ParseHMMER3OutputStepInstance hmmer3ParserStepInstance = null;
            Pfam_A_PostProcessingStepInstance ppStepInstance = null;

            // Create the fastafilestep
            for (Step step : job.getSteps()){
                if (step instanceof WriteFastaFileStep){
                    List<Protein> proteins = daoManager.getProteinDAO().getProteinsBetweenIds(bottomProteinId, topProteinId);

                    System.out.println("Creating WriteFastaFileStepInstance for range "+ bottomProteinId + " - " + topProteinId);
                    fastaStepInstance = new WriteFastaFileStepInstance(
                            UUID.randomUUID(),
                            (WriteFastaFileStep)step,
                            proteins,
                            bottomProteinId,
                            topProteinId
                    );
                    stepInstances.put(fastaStepInstance.getId(), fastaStepInstance);
                }
            }
            // Create the RunHmmer3Step
            for (Step step : job.getSteps()){
                if (step instanceof RunHmmer3Step){
                    System.out.println("Creating RunHmmer3StepInstance for range "+ bottomProteinId + " - " + topProteinId);
                    hmmer3StepInstance = new RunHmmer3StepInstance(
                            UUID.randomUUID(),
                            (RunHmmer3Step)step,
                            bottomProteinId,
                            topProteinId
                    );
                    hmmer3StepInstance.addDependentStepInstance(fastaStepInstance);
                    stepInstances.put(hmmer3StepInstance.getId(), hmmer3StepInstance);
                }
            }

            // Create the ParseHmmer3Output step
            for (Step step : job.getSteps()){
                if (step instanceof ParseHMMER3OutputStep){
                    System.out.println("Creating ParseHMMER3OutputStepInstance for range "+ bottomProteinId + " - " + topProteinId);
                    hmmer3ParserStepInstance = new ParseHMMER3OutputStepInstance(
                            UUID.randomUUID(),
                            (ParseHMMER3OutputStep)step,
                            bottomProteinId,
                            topProteinId
                    );
                    hmmer3ParserStepInstance.addDependentStepInstance(hmmer3StepInstance);
                    stepInstances.put(hmmer3ParserStepInstance.getId(), hmmer3ParserStepInstance);
                }
            }

            // Create the Pfam_A_Post_Processing step
            for (Step step : job.getSteps()){
                if (step instanceof Pfam_A_PostProcessingStep){
                    System.out.println("Creating Pfam_A_PostProcessingStepInstance for range "+ bottomProteinId + " - " + topProteinId);
                    ppStepInstance = new Pfam_A_PostProcessingStepInstance(
                            UUID.randomUUID(),
                            (Pfam_A_PostProcessingStep)step,
                            bottomProteinId,
                            topProteinId
                    );
                    ppStepInstance.addDependentStepInstance(hmmer3ParserStepInstance);
                    stepInstances.put(ppStepInstance.getId(), ppStepInstance);
                }
            }
        }
        System.out.println("Built Collection of stepInstances");
    }

    /**
     * Just creates simple text messages to be sent to Worker nodes.
     * @param stepExecution being the StepExecution to send as a message
     * @throws JMSException in the event of a failure sending the message to the JMS Broker.
     */
    private void sendMessage(StepExecution stepExecution) throws JMSException {
        stepExecution.submit();
        ObjectMessage message = sessionHandler.createObjectMessage(stepExecution);
        message.setBooleanProperty("parallel", stepExecution.getStepInstance().getStep().isParallel());
        if (message.getObject() == null){
            System.out.println("message.getObject() is null.  Throwing IllegalStateException.");
            throw new IllegalStateException ("The object message is empty:" + message.toString());
        }
        StepExecution retrievedStepExec = (StepExecution) message.getObject();
        if (! retrievedStepExec.equals(stepExecution)){
            System.out.println("message.getObject not equals stepExecution.  Throwing IllegalStateException.");
            throw new IllegalStateException("The StepExecution object in the message is not equal to the StepExecution object placed into the message.");
        }
        producer.send(message);
    }
}
