package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.model.Hmmer3ModelLoader;
import uk.ac.ebi.interpro.scan.io.gene3d.Model2SfReader;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.*;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.Model;

import javax.jms.*;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The InterProScan master application.
 *
 * @author Phil Jones
 * @version $Id: TestMaster.java,v 1.4 2009/10/28 15:04:00 pjones Exp $
 * @since 1.0
 */
public class InterProScanMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(InterProScanMaster.class);

    private Jobs jobs;

    private StepInstanceDAO stepInstanceDAO;

    private StepExecutionDAO stepExecutionDAO;

    private GenericDAO<SignatureLibraryRelease, Long> signatureLibraryReleaseDAO;

    private String pfamHMMfilePath;
    private Resource gene3dModel2SfFile;    

    private ConnectionFactory connectionFactory;

    private String workerJobResponseQueueName;

    private String workerJobRequestQueueName;

    private WorkerRunner parallelWorkerRunner;

    @Required
    public void setParallelWorkerRunner(WorkerRunner workerRunner) {
        this.parallelWorkerRunner = workerRunner;
    }

    @Required
    public void setWorkerJobRequestQueueName(String workerJobRequestQueueName) {
        this.workerJobRequestQueueName = workerJobRequestQueueName;
    }

    /**
     * Sets the name of the destinationResponseQueue.
     * @param workerJobResponseQueueName the name of the destinationResponseQueue.
     */
    @Required
    public void setWorkerJobResponseQueueName(String workerJobResponseQueueName) {
        this.workerJobResponseQueueName = workerJobResponseQueueName;
    }

    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setStepExecutionDAO(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }

    @Required
    public void setSignatureLibraryReleaseDAO(GenericDAO<SignatureLibraryRelease, Long> signatureLibraryReleaseDAO) {
        this.signatureLibraryReleaseDAO = signatureLibraryReleaseDAO;
    }

    public Jobs getJobs() {
        return jobs;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setPfamHMMfilePath(String pfamHMMfilePath) {
        this.pfamHMMfilePath = pfamHMMfilePath;
    }

    @Required
    public void setGene3dModel2SfFile(Resource gene3dModel2SfFile) {
        this.gene3dModel2SfFile = gene3dModel2SfFile;
    }

    /**
     * Run the Master Application.
     */
    public void start(){
        SessionHandler sessionHandler = null;
        try {
            // Initialise the sessionHandler for the master thread
            sessionHandler = new SessionHandler(connectionFactory);

            MessageConsumer jobResponseMessageConsumer = sessionHandler.getMessageConsumer(workerJobResponseQueueName);
            MessageProducer jobRequestMessageProducer = sessionHandler.getMessageProducer(workerJobRequestQueueName);

            ResponseMonitorImpl responseMonitor = new ResponseMonitorImpl(stepExecutionDAO);
            jobResponseMessageConsumer.setMessageListener(responseMonitor);

            sessionHandler.start();
//            loadPfamModels();
            while(true){       // TODO should be while(running) to allow shutdown.
                for (Job job : jobs.getJobList()){
                    for (Step step : job.getSteps()){
                        for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances(step)){
                            if (stepInstance.canBeSubmitted(jobs) && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)){
                                sendMessage(stepInstance, sessionHandler, jobRequestMessageProducer);
                            }
                        }
                    }
                }
                Thread.sleep (5000);  // Every 5 seconds, checks for any runnable StepInstances and runs them.
            }
        } catch (JMSException e) {
            LOGGER.error ("JMSException thrown by Master", e);
        } catch (Exception e){
            LOGGER.error ("Exception thrown by Master", e);
        }

        finally {
            if (sessionHandler != null){
                try {
                    sessionHandler.close();
                } catch (JMSException e) {
                    LOGGER.error ("JMSException thrown when attempting to close the SessionHandler.", e);
                }
            }
        }
    }

    /**
     * Called by quartz to load some more proteins.
     */
    public void createProteinLoadJob(){
        Job insertProteinJob = jobs.getJobById("jobLoadFromUniParc");
        for (Step step : insertProteinJob.getSteps()){
            StepInstance stepInstance = new StepInstance(
                    step,
                    null,
                    null,
                    null,
                    null
            );
            stepInstanceDAO.insert(stepInstance);
        }
    }

    private void loadGene3dModels() throws IOException {
        // Read models
        Model2SfReader reader = new Model2SfReader();
        final Map<String, String> modelMap = reader.read(gene3dModel2SfFile);
        // Create signatures
        final Map<String, Signature> signatureMap = new HashMap<String, Signature>();
        for (String modelAc : modelMap.keySet())  {
            String signatureAc = modelMap.get(modelAc);
            Signature signature;
            if (signatureMap.containsKey(signatureAc))  {
                signature = signatureMap.get(signatureAc);    
            }
            else    {
                signature = new Signature(signatureAc);
                signatureMap.put(signatureAc, signature);
            }
            signature.addModel(new Model(modelAc));
        }
        // Create and persist release
        SignatureLibraryRelease release =
                new SignatureLibraryRelease(SignatureLibrary.GENE3D, "3.0.0", 
                        new HashSet<Signature>(signatureMap.values()));
        signatureLibraryReleaseDAO.insert(release);
    }

    private void loadPfamModels() {
        // Parse and retrieve the signatures.
        Hmmer3ModelLoader modelLoader = new Hmmer3ModelLoader(SignatureLibrary.PFAM, "24.0");
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
        signatureLibraryReleaseDAO.insert(release);
        LOGGER.debug("Storing SignatureLibraryRelease...DONE");

    }

    /**
     * Creates simple text messages to be sent to Worker nodes.
     * Does all of this in a transaction, hence in this separate method.
     * @param stepInstance being the StepExecution to send as a message
     * @param sessionHandler used to create the ObjectMessage.
     * @param producer being the MessageProducer upon which to send the message.
     * @throws JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    private void sendMessage(StepInstance stepInstance, SessionHandler sessionHandler, MessageProducer producer) throws JMSException {
        final StepExecution stepExecution = stepInstance.createStepExecution();
        stepExecutionDAO.insert(stepExecution);
        stepExecution.submit(stepExecutionDAO);
        ObjectMessage message = sessionHandler.createObjectMessage(stepExecution);
        producer.setPriority(stepInstance.getStep(jobs).getSerialGroup() == null ? 4 : 7);
        producer.send(message);
        parallelWorkerRunner.startupNewWorker();
    }
}
