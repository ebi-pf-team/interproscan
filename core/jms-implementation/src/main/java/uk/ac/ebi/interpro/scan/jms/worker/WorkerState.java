package uk.ac.ebi.interpro.scan.jms.worker;

/**
 * The WorkerState class is used to transmit the state of
 * a Worker to a caller, usually this is the master.
 *
 * @author gift nuka
 * @since 5.1-44.0
 *
 */

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.io.File;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class WorkerState implements Serializable {


    private static final Logger LOGGER = LogManager.getLogger(WorkerState.class.getName());

    /**
     * How long has the Worker been alive for?
     */
    private long timeAliveMillis;

    /**
     * How long has the Worker been alive for?
     */
    private String logDir;


    /**
     * All the jobs this worker has handled
     */
    private List<StepExecution> allJobs = new ArrayList<StepExecution>();

    /**
     * All the jobs this worker has handled
     */
    private List<StepExecution> allJobsHandled = new ArrayList<StepExecution>();

    /**
     * All the jobs not yet finished
     */
    private ConcurrentMap<Long, StepExecution>  nonFinishedJobs = new ConcurrentHashMap<Long, StepExecution>();

    private  List<Message>  locallyRunningJobs = new ArrayList<Message>();

    private  List<Message>  locallyCompletedJobs = new ArrayList<Message>();

    /**
     * String with any content (at the moment!) describing
     * the status of the Worker.
     */
    private String workerStatus;

    /**
     * Last known state of the current StepExecution
     */
    private StepExecutionState stepExecutionStatus;

    /**
     * The host that the Worker is running on.
     */
    private String hostName;

    /**
     * The masterUri for the master broker.
     */
    private String masterTcpUri;


    /**
     * The uri for this worker.
     */
    private String tcpUri;

    private String projectId;

    private UUID workerIdentification;

    private Throwable exceptionThrown;

    private int processors;

    private int tier;

    private int workersSpawned;

    private boolean processedByMaster = false;

    private WorkerState(){

    }

    public WorkerState(long timeAliveMillis, String hostName, UUID workerIdentification, String projectId,
                       String masterTcpUri, String logDir) {
        this.timeAliveMillis = timeAliveMillis;
        this.tcpUri = hostName;
        this.hostName = hostName;
        this.workerIdentification = workerIdentification;
        this.projectId = projectId;
        this.masterTcpUri = masterTcpUri;
        this.logDir =logDir;
    }

    public List<StepExecution> getAllJobs() {
        return allJobs;
    }

    public void addNewJob(StepExecution stepExecution) {
        this.allJobs.add(stepExecution);
    }

    public void addNonFinishedJob(Message message){
        try {
            LOGGER.debug("addNonFinishedJob: " + message.getJMSMessageID());
            ObjectMessage stepExecutionMessage1 = (ObjectMessage) message;
            ActiveMQObjectMessage stepExecutionMessage = (ActiveMQObjectMessage) message;
            List<String> trustedPackages = new ArrayList();
            trustedPackages.add("uk.ac.ebi.interpro.scan.*");
            trustedPackages.add("*");
            stepExecutionMessage.setTrustedPackages(trustedPackages);
            //stepExecutionMessage.setTrustAllPackages(true);
            //activeMQObjectMessage.settr

            final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
            this.nonFinishedJobs.put(stepExecution.getStepInstance().getId(), stepExecution);
            this.allJobs.add(stepExecution);
            if(Utilities.verboseLogLevel > 4){
                Utilities.verboseLog(1100, "Received StepInstance:  added to unfinishedJobs - " + stepExecution.getStepInstance().toString());
            }
        } catch (JMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * remove
     *
     * @param message
     */
    public synchronized void removeFromNonFinishedJobs(Message message){

        try {
            LOGGER.debug("removeFromNonFinishedJobs: " + message.getJMSMessageID());
            ObjectMessage stepExecutionMessage = (ObjectMessage) message;
            final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
            this.nonFinishedJobs.remove(stepExecution.getStepInstance().getId());
        } catch (JMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public ConcurrentMap<Long, StepExecution> getNonFinishedJobs() {
        return nonFinishedJobs;
    }

    public void addLocallyCompletedJobsJob(Message message) {
        this.locallyCompletedJobs.add(message);
    }

    public List<Message> getLocallyCompletedJobs() {
        return locallyCompletedJobs;
    }

    public void addLocallyCompletedJob(Message message) {
        this.locallyCompletedJobs.add(message);
    }

    public void setTimeAliveMillis(long timeAliveMillis) {
        this.timeAliveMillis = timeAliveMillis;
    }


    public void setWorkerStatus(String workerStatus) {
        this.workerStatus = workerStatus;
    }

    public void setExceptionThrown(Throwable exceptionThrown) {
        this.exceptionThrown = exceptionThrown;
    }

    public StepExecutionState getStepExecutionStatus() {
        return stepExecutionStatus;
    }

    public void setStepExecutionState(StepExecutionState stepExecutionStatus) {
        this.stepExecutionStatus = stepExecutionStatus;
    }

    public long getTimeAliveMillis() {
        return timeAliveMillis;
    }

    public String getWorkerStatus() {
        return workerStatus;
    }

    public String getHostName() {
        return hostName;
    }

    public UUID getWorkerIdentification() {
        return workerIdentification;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setTcpUri(String tcpUri) {
        this.tcpUri = tcpUri;
    }

    public void setMasterTcpUri(String masterTcpUri) {
        this.masterTcpUri = masterTcpUri;
    }

    public String getMasterTcpUri() {
        return masterTcpUri;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setWorkerIdentification(UUID workerIdentification) {
        this.workerIdentification = workerIdentification;
    }

    public Throwable getExceptionThrown() {
        return exceptionThrown;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public void setWorkersSpawned(int workersSpawned) {
        this.workersSpawned = workersSpawned;
    }

    public int getProcessors() {
        return processors;
    }

    public String getTcpUri() {
        return tcpUri;
    }

    public int getWorkersSpawned() {
        return workersSpawned;
    }

    public int getTier() {
        return tier;
    }

    public String getLogDir() {
        return logDir;
    }

    public int getTotalStepCount(){
        return allJobs.size();
    }


    public int getUnfinishedStepCount(){
        return nonFinishedJobs.size();
    }

    public void printWorkerState(){
        Utilities.verboseLog(toString());
    }

    public boolean isProcessedByMaster() {
        return processedByMaster;
    }

    public void setProcessedByMaster(boolean processedByMaster) {
        this.processedByMaster = processedByMaster;
    }

    @Override
    public String toString() {
        StringBuffer workerState = new StringBuffer("WorkerState: " + workerIdentification).append("\n");
        workerState.append("hostname: " + hostName).append("\n");
        workerState.append("processors: " + processors).append("\n");
        workerState.append("thisworker_id: " + projectId
                + "_" + tcpUri.hashCode()).append("\n");
        workerState.append("tier: " + tier).append("\n");

        workerState.append("masterId: " + projectId
                + "_" + masterTcpUri.hashCode()).append("\n");

        workerState.append("status: " + workerStatus).append("\n");

        workerState.append("logDir: " + logDir).append("\n");
        workerState.append("logDirPath: " + logDir + File.separator + projectId
                + "_" + masterTcpUri.hashCode()).append("\n");
        workerState.append("workers spawned: " + workersSpawned).append("\n");
        workerState.append("jotalJobsReceived: " + allJobs.size()).append("\n");
        workerState.append("jobsNotFinished: " + nonFinishedJobs.size()).append("\n");
        workerState.append("summary: " + hostName + ":"
                + processors + ":" + allJobs.size()).append("\n");
        int count = 0;
        for(StepExecution stepExecution: nonFinishedJobs.values()){
            workerState.append(count + ": " + stepExecution.getStepInstance().getId()
                    + " - " + stepExecution.getStepInstance().getStepId()
            ).append("\n");
            count ++;
        }
        return workerState.toString();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerState)) return false;

        WorkerState that = (WorkerState) o;
        // If its the same JVM, its the same worker.
        if (this.getWorkerIdentification().equals(that.getWorkerIdentification())) {
            return true;
        }

        if (timeAliveMillis != that.timeAliveMillis) return false;
        if (exceptionThrown != null ? !exceptionThrown.equals(that.exceptionThrown) : that.exceptionThrown != null)
            return false;
        if (!hostName.equals(that.hostName)) return false;
        if (masterTcpUri != null ? !masterTcpUri.equals(that.masterTcpUri) : that.masterTcpUri != null)
            return false;
        if (workerStatus != null ? !workerStatus.equals(that.workerStatus) : that.workerStatus != null) return false;
        if (!workerIdentification.equals(that.workerIdentification)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (timeAliveMillis ^ (timeAliveMillis >>> 32));
        result = 31 * result + (logDir != null ? logDir.hashCode() : 0);
        result = 31 * result + (masterTcpUri != null ? masterTcpUri.hashCode() : 0);
        if(hostName == null){
            try {
               String localhostname = java.net.InetAddress.getLocalHost().getHostName();
                String fullHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                hostName = localhostname;
            } catch (UnknownHostException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        result = 31 * result + hostName.hashCode();
        if(workerIdentification == null){
            workerIdentification = UUID.randomUUID();
        }
        UUID.randomUUID();
        result = 31 * result + workerIdentification.hashCode();
        result = 31 * result + (exceptionThrown != null ? exceptionThrown.hashCode() : 0);
        return result;
    }
}
