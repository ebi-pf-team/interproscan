package uk.ac.ebi.interpro.scan.jms.broker.platforms;

import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

/**
 * Runs worker JVMs on an LSF cluster.
 *
 * @author Phil Jones
 * @version $Id: LSFWorkerRunner.java,v 1.2 2009/10/16 12:04:06 pjones Exp $
 * @since 1.0
 */
public class LSFWorkerRunner implements WorkerRunner {
   
    private String optionalPrefix;

    private String command;

    private String queueName;

    private String lsfEmailNotification;

    private int memoryUsageMB = 1024;

    private String outFilePath;

    /**
     * Command line prior to the bsub command.
     * For example, the master application may not be running on the LSF submission host,
     * in which case it may need to ssh (or alternative) on to this host to submit the job.
     *
     * Example:  "ssh -x username@lsf-submit-host.ebi.ac.uk"
     *
     * Of course this will only work if the user does not need to enter a password.
     * @param optionalPrefix Command line prior to the bsub command.
     */
    public void setOptionalPrefix(String optionalPrefix) {
        this.optionalPrefix = optionalPrefix;
    }

    /**
     * The command to be run by bsub.
     * @param command the command to be run by bsub.  Presumably java executable.
     * TODO - need to consider this more carefully.
     */
    @Required
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Optional LSF queue to submit the job to.
     * @param queueName optional LSF queue to submit the job to.
     */
    public void setLsfQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Optional list of email addresses to send LSF notifications to.
     * @param lsfEmailNotification optional list of email addresses to send LSF notifications to.
     */
    public void setLsfEmailNotification(String lsfEmailNotification) {
        this.lsfEmailNotification = lsfEmailNotification;
    }

    /**
     * Optional setter for the memory usage requirement to be passed to the bsub command.
     * Default to 1000MB.
     * @param memoryUsageMB the memory usage requirement to be passed to the bsub command.
     */
    public void setMemoryUsageMB(int memoryUsageMB) {
        this.memoryUsageMB = memoryUsageMB;
    }

    public void setOutFilePath(String outFilePath) {
        this.outFilePath = outFilePath;
    }

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     */
    public void startupNewWorker() {
        StringBuffer commandBuf = new StringBuffer();
        if (optionalPrefix != null){
            commandBuf.append(optionalPrefix).append(' ');
        }
        commandBuf  .append("bsub -R \"rusage[mem=")
                    .append(memoryUsageMB)
                    .append("]\" ");
        if (queueName != null){
            commandBuf.append("-q ").append(queueName).append(' ');
        }

        if (this.lsfEmailNotification != null){
            commandBuf.append("-u ").append(lsfEmailNotification).append(' ');
        }

        if (this.outFilePath != null){
            commandBuf.append("-o ").append(outFilePath).append(' ');
        }

        commandBuf.append('"').append(command).append('"');
        System.out.println("LSF Command:\n" + commandBuf);
        try{
//            Runtime.getRuntime().exec("ssh -x " + username + "@ant17.ebi.ac.uk bsub mvn -P runDestination -f /net/isilon3/production/seqdb2/production/interpro/jira/IBU-1067/hornetQ_Test/pom.xml exec:java");
            Runtime.getRuntime().exec(commandBuf.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start the worker", e);
        }

    }
}
