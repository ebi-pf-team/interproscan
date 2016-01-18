package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.jms.lsf.LSFMonitor;
import uk.ac.ebi.interpro.scan.jms.master.ClusterState;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Creates a new worker by executing a command.
 * <p/>
 * Note that this class is NOT thread safe and is expected to be used in a single controlling thread.
 */

public class SubmissionWorkerRunner implements WorkerRunner {

    private static final Logger LOGGER = Logger.getLogger(SubmissionWorkerRunner.class.getName());

    private String submissionCommand;

    private String submissionCommandHeredocOpen;

    private String submissionCommandHeredocClose;

    private String gridCommand;

    private String projectId;

    private String userDir;

    private String logDir;

    private String i5Command;

    private int tier = 1;

    private int gridJobsLimit = 1000;

    private boolean highMemory;

    private boolean masterWorker;

    private static long submissionTimeMillis = 0;

    private WorkerStartupStrategy workerStartupStrategy;

    private LSFMonitor lsfMonitor;

    private String agent_id;

    private int workerCount = 0;

    private int newWorkersCount = 0;

    private String gridName = "lsf";

    private boolean gridArray = false;

    private long currentMasterClockTime;
    private long lifeSpanRemaining;

    private ClusterState clusterState;

    private Long timeLastDisplayedGridWarnMessage = System.currentTimeMillis();


    @Required
    public void setGridJobsLimit(int gridJobsLimit) {
        this.gridJobsLimit = gridJobsLimit;
    }

    @Required
    public void setLsfMonitor(LSFMonitor lsfMonitor) {
        this.lsfMonitor = lsfMonitor;
    }

    @Required
    public void setSubmissionCommand(String submissionCommand) {
        this.submissionCommand = submissionCommand;
    }

    public String getSubmissionCommandHeredocOpen() {
        return submissionCommandHeredocOpen;
    }

    public void setSubmissionCommandHeredocOpen(String submissionCommandHeredocOpen) {
        this.submissionCommandHeredocOpen = submissionCommandHeredocOpen;
    }

    public String getSubmissionCommandHeredocClose() {
        return submissionCommandHeredocClose;
    }

    public void setSubmissionCommandHeredocClose(String submissionCommandHeredocClose) {
        this.submissionCommandHeredocClose = submissionCommandHeredocClose;
    }

    @Required
    public void setGridCommand(String gridCommand) {
        this.gridCommand = gridCommand;
    }

    @Required
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Required
    public void setI5Command(String i5Command) {
        this.i5Command = i5Command;
    }

    @Required
    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    public String getGridName() {
        return gridName;
    }

    @Required
    public void setHighMemory(boolean highMemory) {
        this.highMemory = highMemory;
    }

    @Required
    public void setWorkerStartupStrategy(WorkerStartupStrategy workerStartupStrategy) {
        this.workerStartupStrategy = workerStartupStrategy;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public long getCurrentMasterClockTime() {
        return currentMasterClockTime;
    }

    public void setCurrentMasterClockTime(long currentMasterClockTime) {
        this.currentMasterClockTime = currentMasterClockTime;
    }

    public long getLifeSpanRemaining() {
        return lifeSpanRemaining;
    }

    public void setLifeSpanRemaining(long lifeSpanRemaining) {
        this.lifeSpanRemaining = lifeSpanRemaining;
    }


    public void setClusterState(ClusterState clusterState) {
        this.clusterState = clusterState;
    }

    public ClusterState getClusterState() {
        return clusterState;
    }

    /**
     * create an id for the new worker given a project id
     *
     * @param tcpUri
     */
    public void setAgent_id(String tcpUri) {
        //label log file if high memory worker (hw) or normal worker (nw)
        String workerType = highMemory ? "hm" : "nw";
        if (projectId != null) {
            this.agent_id = projectId + "_" + tcpUri.hashCode() + "_"+workerType+"_" + getWorkerCountString();
        }else if(tcpUri != null ){
            this.agent_id = "worker" + "_" + tcpUri.hashCode() + "_"+workerType + "_" + getWorkerCountString();
        }else{
            this.agent_id = "worker_unid" + "_" + workerType+"_" + getWorkerCountString();
        }
    }

    public String getWorkerCountString() {
        if(workerCount < 10){
            return "0"+Integer.toString(workerCount);
        }
        return Integer.toString(workerCount);
    }
    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     */
    @Override
    public int startupNewWorker() {
        final int priority = (Math.random() < 0.5) ? 4 : 8;
        int actualWorkersStarted = startupNewWorker(priority);
        return actualWorkersStarted;
    }

    /**
     * The masterWorker boolean flag indicates if a worker was created by the master itself. TRUE if created by the master, otherwise FALSE.
     */
    public int startupNewWorker(final int priority, final String tcpUri, final String temporaryDirectory, boolean masterWorker) {
        this.masterWorker = masterWorker;
        int actualWorkersStarted = startupNewWorker(priority, tcpUri, temporaryDirectory);
        //reset the masterworker variable
        this.masterWorker = false;
        return actualWorkersStarted;
    }

    /**
     * The newWorkersCount is the number of workers to be created
     */
    public int startupNewWorker(final int priority, final String tcpUri, final String temporaryDirectory, int newWorkersCount) {
        this.newWorkersCount = newWorkersCount;
        int actualWorkersStarted = startupNewWorker(priority, tcpUri, temporaryDirectory);
        //reset the masterworker variable
        this.newWorkersCount = 0;
        return actualWorkersStarted;
    }

    @Override
    public int startupNewWorker(final int priority, final String tcpUri, final String temporaryDirectory) {
        //monitor the cluster

        Utilities.verboseLog("startupNewWorker ");

        if(gridName.equals("lsf")){

            if(clusterState != null) { // && timeSinceLastClusterStateUpdate < 2 * 60 * 1000){
                Long timeSinceLastClusterStateUpdate = System.currentTimeMillis() - clusterState.getLastUpdated();

                int runningJobs =  clusterState.getAllClusterProjectJobsCount() - clusterState.getPendingClusterProjectJobsCount();

                DateFormat df = new SimpleDateFormat("dd:MM:yy HH:mm:ss");
                if(Utilities.verboseLogLevel > 1){
                    Utilities.verboseLog("Grid Job Control: "
                            + "gridJobsLimit: " + gridJobsLimit
                            + " activeJobs: " + clusterState.getAllClusterProjectJobsCount()
                            + " runningJobs: " + runningJobs
                            + " runningJobs: * 10%: " + runningJobs * 0.1
                            + " pendingJobs:" +  clusterState.getPendingClusterProjectJobsCount()
                            + "timestamp: " + df.format(new Date(clusterState.getLastUpdated())));
                }
                if (! clusterState.canSubmitToCluster()) {
                    Long timeSinceLastDisplayedWarningMessage = System.currentTimeMillis() - timeLastDisplayedGridWarnMessage;
                    //only display this message every five minutes
                    if(timeSinceLastClusterStateUpdate > 5 * 60 * 1000){
                        LOGGER.warn("Grid Job Control: You have reached the maximum jobs allowed on the cluster or you have many pending jobs.  active Jobs: " + clusterState.getAllClusterProjectJobsCount() + " pending Jobs : " + clusterState.getPendingClusterProjectJobsCount()
                            + "\n In the meantime InterProScan will continue to run");
                         timeLastDisplayedGridWarnMessage = System.currentTimeMillis();
                    }
                    return 0;
                }else{
                    if(newWorkersCount > 1 && (newWorkersCount > gridJobsLimit - clusterState.getAllClusterProjectJobsCount()) ){
                        newWorkersCount = gridJobsLimit - clusterState.getAllClusterProjectJobsCount();
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("startupNewWorker(): GridJobs -   active Jobs on the cluster: " + clusterState.getAllClusterProjectJobsCount());
                }
            }else{
                LOGGER.warn("startupNewWorker(): clusterState IsNull " );
            }
        }
        if (workerStartupStrategy.startUpWorker(priority)) {
            LOGGER.debug("startupNewWorker(): " );
            setAgent_id(tcpUri);

            if(newWorkersCount >  1){
                workerCount += newWorkersCount;
            }else{
                workerCount++;
            }

            StringBuilder commandToSubmit = new StringBuilder();

            try {
                int maxWorkerIndex = newWorkersCount;
                if(gridName.equals("lsf")) {
                    //for lsf we submit using job arrays
                    maxWorkerIndex = 1;
                }
                int failedSubmissions = 0;
                for(int workerIndex = 0;workerIndex < maxWorkerIndex;workerIndex++){
                    final CommandLineConversation clc = new CommandLineConversationImpl();
                    if(gridName.equals("lsf")) {
                        commandToSubmit = new StringBuilder(gridCommand)
                                .append(getClusterCommandArguments(workerIndex)
                                        .append(" " + i5Command)
                                        .append(getInterproscanCommandArguments(priority, tcpUri, temporaryDirectory)));
                    }else if(gridName.equals("other")){
                        commandToSubmit = new StringBuilder()
                                .append(submissionCommandHeredocOpen)
                                .append(gridCommand)
                                .append(getClusterCommandArguments(workerIndex)
                                        .append("  ")
                                        .append("\n")
                                        .append(i5Command)
                                        .append(getInterproscanCommandArguments(priority, tcpUri, temporaryDirectory)))
                                .append("\n")
                                .append(submissionCommandHeredocClose);
                    }else{
                        commandToSubmit = new StringBuilder(gridCommand)
                                .append(getClusterCommandArguments(workerIndex)
                                        .append(" " + i5Command)
                                        .append(getInterproscanCommandArguments(priority, tcpUri, temporaryDirectory)));
                    }

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("command to submit to cluster: " + commandToSubmit);
                    }
                    if(Utilities.verboseLogLevel > 1){
                        Utilities.verboseLog("command to submit to cluster:  "
                                + commandToSubmit);
                    }
                    int exitStatus = clc.runCommand(false, commandToSubmit.toString().split(" "));
                    if (exitStatus != 0) {
                        LOGGER.warn("Non-zero exit status from attempting to run a worker: \nCommand:"
                                + commandToSubmit + "\nExit status: "
                                + exitStatus + "\nError output: "
                                + clc.getErrorMessage());
                        failedSubmissions++;
                    }
                    if(Utilities.verboseLogLevel > 1) {
                        String clcOutput = clc.getOutput();
                        Utilities.verboseLog("Output from attempting to run a worker: "
                                + ((clcOutput == null) ? "NULL" : clcOutput));
                    }
                }
                if(failedSubmissions > 0 && maxWorkerIndex == 1) {
                    newWorkersCount = 0;
                }else{
                    newWorkersCount -= failedSubmissions;
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to start worker - MASTER WILL CONTINUE however. \nCommand:"
                        + commandToSubmit + "\nSee stack trace: ", e);
                newWorkersCount = 0;
            } catch (InterruptedException e) {
                LOGGER.warn("Unable to start worker - MASTER WILL CONTINUE however. \nCommand:"
                        + commandToSubmit + "\nSee stack trace: ", e);
                newWorkersCount = 0;
            }
        }
        return newWorkersCount;
    }

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    @Override
    public int startupNewWorker(int priority) {
        int actualWorkersStarted = startupNewWorker(priority, null, null);
        return actualWorkersStarted;
    }

    /**
     *  get the cluster command arguments
     *
     * @param workerIndex
     * @return
     */
    private StringBuilder getClusterCommandArguments(int workerIndex){
        StringBuilder clusterCommandAguments = new StringBuilder();

        //add error and output log handling for the cluster
        String jobArray;
        String jobIndex;
        if(agent_id != null){
            if(gridName.equals("lsf") && newWorkersCount > 1){
                jobArray = "[1-"+newWorkersCount+"]";
                jobIndex = ".%I";
            }else{
                jobArray = "";
                jobIndex = "." + workerIndex;
            }
            clusterCommandAguments.append(" -o " + logDir + File.separator+ agent_id+".out" + jobIndex);
            clusterCommandAguments.append(" -e " + logDir + File.separator+ agent_id+".err" + jobIndex);

            if(gridName.equals("lsf")) {
                clusterCommandAguments.append(" -J "+ agent_id + jobArray);
            }
        }
        if(gridName.equals("lsf") && (projectId != null)) {
            clusterCommandAguments.append(" -P "+ projectId);
        }

        //other grid submission commands
//        if(gridName.equals("other")){
//            clusterCommandAguments = new StringBuilder();
//            clusterCommandAguments.append("/bin/bash -c 'echo \"");
//        }
        return clusterCommandAguments;
    }

    /**
     * get the arguments for the interproscan5 command
     * @param priority
     * @param tcpUri
     * @param temporaryDirectory
     * @return
     */
    private StringBuilder getInterproscanCommandArguments(final int priority, final String tcpUri, final String temporaryDirectory){
        StringBuilder interproscanCommandArguments = new StringBuilder();
        if (tcpUri == null) {
            interproscanCommandArguments.append(highMemory ? " --mode=highmem_worker" : " --mode=worker");
        } else {
//                command.append(highMemory ? " --mode=cl_highmem_worker" : " --mode=cl_worker");
//                command.append(highMemory ? " --mode=distributed_worker --highmem" : " --mode=distributed_worker");
            interproscanCommandArguments.append(highMemory ? " --mode=distributed_worker --highmem" : " --mode=distributed_worker");
        }
        if (priority > 0) {
            interproscanCommandArguments.append(" --priority=")
                    .append(priority);
        }

        if (tcpUri != null) {
            interproscanCommandArguments.append(" --masteruri=")
                    .append(tcpUri);
        }

        if (temporaryDirectory != null) {
            interproscanCommandArguments.append(" --tempdirname=")
                    .append(temporaryDirectory);
        }

        if (userDir != null) {
            interproscanCommandArguments.append(" --userdir=")
                    .append(userDir);
        }


        if (tier > 0) {
            interproscanCommandArguments.append(" --tier1=")
                    .append(tier);
        }
        if (this.projectId != null) {
            interproscanCommandArguments.append(" --clusterrunid=")
                    .append(projectId);
        }
        interproscanCommandArguments.append(" --mastermaxlife=")
                .append(currentMasterClockTime + ":" + lifeSpanRemaining);

        return interproscanCommandArguments;
    }

//TODO: work out how to configure submission scripts.

    //    private String optionalPrefix;
//
//    private Resource submissionJobFile;
//
//    //Optional parameter so user can set shell to one of their own choosing.
//    //Default for both SGE and LSF is Bourne shell (/bin/sh).
//    //Default for PBS is to use the user's login shell on the execution host.
//    private String shellPath;
//
//    //This is a required parameter and currently can have the values 'sge', 'lsf' or 'pbs'.
//    private String submissionType;
//
//    private Map<String,String> submissionCommand = popSubmissionKey.newMap();
//
//    public void setOptionalPrefix(String optionalPrefix) {
//        this.optionalPrefix = optionalPrefix;
//    }
//
//    @Required
//    public void setSubmissionJobFile(Resource submissionJobFile) {
//        this.submissionJobFile = submissionJobFile;
//    }
//
//    public void setShellPath(String shellPath) {
//        this.shellPath = shellPath;
//    }
//
//    @Required
//    public void setSubmissionType(String submissionType) {
//        this.submissionType = submissionType;
//    }
//
//    @Override
//    public void startupNewWorker() {
//
//        StringBuffer commandBuf = new StringBuffer();
//        if (optionalPrefix != null){
//            commandBuf.append(optionalPrefix).append(' ');
//        }
//
//        commandBuf.append(submissionCommand.get(submissionType)).append(' ');
//
//        if (shellPath != null && shellPath.length() > 0) {
//            commandBuf.append(submissionCommand.get(submissionType+"_sh")).append(' ').append(shellPath).append(' ');
//        }
//
//        if (submissionCommand.containsKey(submissionType+"_exec")) {
//            commandBuf.append(submissionCommand.get(submissionType+"_exec")).append(' ');
//        }
//
//        try{
//            commandBuf.append(submissionJobFile.getFile().getAbsolutePath());
//
//            LOGGER.debug("Submitted Command:\t" + commandBuf.toString());
//
//            Runtime.getRuntime().exec(commandBuf.toString());
//        } catch (IOException e) {
//            throw new IllegalStateException("Cannot run the worker", e);
//        }
//    }
//
//    static class popSubmissionKey{
//        private popSubmissionKey(){
//        }
//        private static Map<String, String> newMap(){
//            Map<String, String> m = new java.util.HashMap<String,String>();
//            m.put("sge", "qsub");
//            m.put("sge_sh", "-S");
//            m.put("pbs", "qsub");
//            m.put("pbs_sh", "-S");
//            m.put("lsf", "bsub");
//            m.put("lsf_sh", "-L");
//            m.put("lsf_exec", "<");
//            return Collections.unmodifiableMap(m);
//        }
//    }
}
