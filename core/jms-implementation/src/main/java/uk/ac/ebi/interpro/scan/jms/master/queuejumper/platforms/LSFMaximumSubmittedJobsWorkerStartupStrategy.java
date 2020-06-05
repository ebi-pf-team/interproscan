package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Phil Jones
 *         <p/>
 *         Startup strategy that works on the assumption that (a) you are using LSF and (b)
 *         you are running the master on the cluster (i.e. you can successfully call the busers command)
 */
public class LSFMaximumSubmittedJobsWorkerStartupStrategy implements WorkerStartupStrategy {

    private static final Logger LOG = LogManager.getLogger(LSFMaximumSubmittedJobsWorkerStartupStrategy.class.getName());

    private int maximumSubmittedJobs;

    private int submittedJobs = 0;

    private long timeLastChecked = Long.MAX_VALUE;

    /**
     * By default, check busers every five minutes maximum.
     */
    private long checkInterval = 300000l;

    private static final Object LOCK_CHECK = new Object();

    private String linuxAccountName;

    @Required
    public void setLinuxAccountName(String linuxAccountName) {
        this.linuxAccountName = linuxAccountName;
    }

    @Required
    public void setMaximumSubmittedJobs(int maximumSubmittedJobs) {
        this.maximumSubmittedJobs = maximumSubmittedJobs;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    /*
USER/GROUP          JL/P    MAX  NJOBS   PEND    RUN  SSUSP  USUSP    RSV
username               0.5      -      0      0      0      0      0      0

     */

    /**
     * Returns true if the worker should be run, false otherwise.
     *
     * @param priority the message priority that the worker is being configured to process.
     *                 (1 - 10, 10 is high)
     * @return true if the worker should be run, false otherwise.
     */
    public boolean startUpWorker(int priority) {
        final long now = System.currentTimeMillis();

        // Don't call busers every time - this would be inefficient. An approximate count will do fine...
        if (timeLastChecked + checkInterval < now) {
            synchronized (LOCK_CHECK) {
                if (timeLastChecked + checkInterval < now) {
                    // Check the number of submitted jobs
                    final CommandLineConversationImpl clc = new CommandLineConversationImpl();
                    try {
                        int exitStatus = clc.runCommand(false, "busers", linuxAccountName);
                        if (exitStatus != 0) {
                            LOG.warn("Unable to run busers command to find out the number of submitted jobs on LSF");
                        }
                        final String busersOut = clc.getOutput();
                        if (busersOut == null) {
                            LOG.warn("No output retrieved from the busers command:");
                        }
                        if (busersOut != null) {
                            String[] lines = busersOut.split("\\n");

                            if (lines.length != 2) {
                                LOG.warn("Unable to parse the output from the busers command:\n" + busersOut);
                            }
                            String[] fields = lines[1].split("\\s+");
                            if (fields.length < 9) {
                                LOG.warn("Unable to parse the output from the busers command:\n" + busersOut);
                            }
                            submittedJobs = Integer.parseInt(fields[3]);
                        }
                    } catch (IOException e) {
                        LOG.warn("Unable to run busers command to find out the number of submitted jobs on LSF", e);
                    } catch (InterruptedException e) {
                        LOG.warn("Unable to run busers command to find out the number of submitted jobs on LSF", e);
                    }
                    // Note - if the attempt to call busers fails, the system will wait for another interval
                    // of checkInterval before trying again (and not just barf)
                    timeLastChecked = now;
                }
            }
        }
        if (submittedJobs < maximumSubmittedJobs) {
            submittedJobs++;
            return true;
        }
        return false;
    }
}
