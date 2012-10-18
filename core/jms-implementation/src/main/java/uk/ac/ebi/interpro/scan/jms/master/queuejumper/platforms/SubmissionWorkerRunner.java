package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;

import java.io.IOException;


/**
 * Creates a new worker by executing a command.
 */

public class SubmissionWorkerRunner implements WorkerRunner {

    private static final Logger LOGGER = Logger.getLogger(SubmissionWorkerRunner.class.getName());

    private String submissionCommand;

    private String gridCommand;

    private String projectId;

    private String i5Command;

    private boolean highMemory;

    private boolean masterWorker;

    private static long submissionTimeMillis = 0;

    private WorkerStartupStrategy workerStartupStrategy;

    @Required
    public void setSubmissionCommand(String submissionCommand) {
        this.submissionCommand = submissionCommand;
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
    public void setHighMemory(boolean highMemory) {
        this.highMemory = highMemory;
    }

    @Required
    public void setWorkerStartupStrategy(WorkerStartupStrategy workerStartupStrategy) {
        this.workerStartupStrategy = workerStartupStrategy;
    }

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     */
    @Override
    public void startupNewWorker() {
        final int priority = (Math.random() < 0.5) ? 4 : 8;
        startupNewWorker(priority);
    }

    /**
     * The masterWorker boolean flag indicates if a worker was created by the master itself. TRUE if created by the master, otherwise FALSE.
     */
    public void startupNewWorker(final int priority, final String tcpUri, final String temporaryDirectory, boolean masterWorker) {
        this.masterWorker=masterWorker;
        startupNewWorker(priority,tcpUri,temporaryDirectory);
    }


    @Override
    public void startupNewWorker(final int priority, final String tcpUri, final String temporaryDirectory) {
        if (workerStartupStrategy.startUpWorker(priority)) {
            StringBuilder command = new StringBuilder(gridCommand);

            if (!projectId.equals(null)) {
                command.append(" -P "+ projectId);
            }

            command.append(" " + i5Command);

            LOGGER.debug("command without arguments : "+command);
            if (tcpUri == null) {
                command.append(highMemory ? " --mode=highmem_worker" : " --mode=worker");
            } else {
//                command.append(highMemory ? " --mode=cl_highmem_worker" : " --mode=cl_worker");
//                command.append(highMemory ? " --mode=distributed_worker --highmem" : " --mode=distributed_worker");
                command.append(highMemory ? " --mode=distributed_worker --highmem" : " --mode=distributed_worker");
            }
            if (priority > 0) {
                command.append(" --priority=")
                        .append(priority);
            }

            if (tcpUri != null) {
                command.append(" --masteruri=")
                        .append(tcpUri);
            }

            if (temporaryDirectory != null) {
                command.append(" --tempdirname=")
                        .append(temporaryDirectory);
            }

            if(this.masterWorker){
                command.append(" --tier1=")
                        .append("1");
            }
            if(this.projectId!= null){
                command.append(" --projectid=")
                        .append(projectId);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Command ABOUT to be submitted: " + command);
            }
            try {
//                Runtime.getRuntime().exec(command.toString());
                final CommandLineConversation clc = new CommandLineConversationImpl();
                int exitStatus = clc.runCommand(false, command.toString().split(" "));
                if (exitStatus != 0) {
                    LOGGER.warn("Non-zero exit status from attempting to run a worker: \nCommand:" + command + "\nExit status: " + exitStatus + "\nError output: " + clc.getErrorMessage());
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to start worker - MASTER WILL CONTINUE however. \nCommand:" + command + "\nESee stack trace: ", e);
            } catch (InterruptedException e) {
                LOGGER.warn("Unable to start worker - MASTER WILL CONTINUE however. \nCommand:" + command + "\nESee stack trace: ", e);
            }
        }
    }

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    @Override
    public void startupNewWorker(int priority) {
        startupNewWorker(priority, null, null);
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
