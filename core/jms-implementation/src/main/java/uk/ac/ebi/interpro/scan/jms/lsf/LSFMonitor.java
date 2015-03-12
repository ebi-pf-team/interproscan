package uk.ac.ebi.interpro.scan.jms.lsf;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nuka
 * Date: 23/10/12
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class LSFMonitor {

    private static final Logger LOGGER = Logger.getLogger(LSFMonitor.class.getName());

    /**
     *
     * @param projectId
     * @return   number of active bsub jobs for a project
     */
    public int activeJobs(String projectId) {
        LOGGER.info("Monitoring jobs with project name " + projectId);
        //Build bjobs command
        LSFBjobsCommand monitorCmd = buildBjobsCommand(projectId);
        LSFJobInfoParser parser = LSFJobInfoParser.getInstance();
        InputStream bjobsOutput = runBjobs(monitorCmd);
        Map<String, Set<LSFJobInformation>> jobInformationMap = parser.parse(bjobsOutput);
        Collection<Set<LSFJobInformation>> s = jobInformationMap.values();
        Collection<Set<LSFJobInformation>> jobInformations = jobInformationMap.values();
        int count =0;
        for (Set<LSFJobInformation> jobInformationSet: jobInformations) {
            for (LSFJobInformation jobInformation: jobInformationSet) {
                LSFJobStatus jobStatus = jobInformation.getStatus();
                if(jobStatus.equals(LSFJobStatus.PEND) || jobStatus.equals(LSFJobStatus.RUN) || jobStatus.equals(LSFJobStatus.WAIT)){
                     count++;
                }
            }
        }
        return count;
    }

    /**
     *
     * @param projectId
     * @return   number of active bsub PENDING jobs for a project
     */
    public int pendingJobs(String projectId) {
        LOGGER.info("Monitoring jobs with project name " + projectId);
        //Build bjobs command
        LSFBjobsCommand monitorCmd = buildBjobsCommand(projectId);
        LSFJobInfoParser parser = LSFJobInfoParser.getInstance();
        InputStream bjobsOutput = runBjobs(monitorCmd);
        Map<String, Set<LSFJobInformation>> jobInformationMap = parser.parse(bjobsOutput);
        Collection<Set<LSFJobInformation>> s = jobInformationMap.values();
        Collection<Set<LSFJobInformation>> jobInformations = jobInformationMap.values();
        int count = 0;
        for (Set<LSFJobInformation> jobInformationSet: jobInformations) {
            for (LSFJobInformation jobInformation: jobInformationSet) {
                LSFJobStatus jobStatus = jobInformation.getStatus();
                if(jobStatus.equals(LSFJobStatus.PEND) || jobStatus.equals(LSFJobStatus.WAIT)){
                    count++;
                }
            }
        }
        return count;
    }


    private Set<LSFJobStatus> getJobStatuses(Set<LSFJobInformation> jobInformations) {
        Set<LSFJobStatus> jobStatuses = new HashSet<LSFJobStatus>();
        for (LSFJobInformation jobInformation: jobInformations) {
            LSFJobStatus jobStatus = jobInformation.getStatus();
            jobStatuses.add(jobStatus);
        }
        return jobStatuses;
    }


    /**
     *
     * @param lsfCommand
     * @return     bjobs result output
     */
    private InputStream runBjobs(LSFBjobsCommand lsfCommand) {
        InputStream is = null;
        List<String> command = lsfCommand.getCommand();
        if (command != null && command.size() > 0) {
            LOGGER.info("Running the following command: " + command);
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                Process pr = pb.start();
                int exitStatus = pr.waitFor();
                is = pr.getInputStream();
                if (exitStatus == 0) {
                    LOGGER.debug("Bjobs command finished successfully!");
                } else {
                    StringBuffer failureMessage = new StringBuffer();
                    failureMessage.append("Bjobs command failed with exit code: ")
                            .append(exitStatus)
                            .append("\nBjobs command: ");
                    for (String element : command) {
                        failureMessage.append(element).append(' ');
                    }
                    failureMessage.append("Bjobs output:\n").append(getStandardOutput(is));
                    throw new IllegalStateException(failureMessage.toString());
                }


            } catch (InterruptedException e) {
                throw new IllegalStateException("InterruptedException thrown when attempting to run binary", e);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return is;
    }


    protected LSFBjobsCommand buildBjobsCommand(String projectName) {
        Map<LSFBjobsOption, Object> options = new HashMap<LSFBjobsOption, Object>();
        options.put(LSFBjobsOption.WIDE_FORMAT, "");
        options.put(LSFBjobsOption.SHOW_ALL_STATES, "");
        options.put(LSFBjobsOption.PROJECT_NAME, projectName);
        return new LSFBjobsCommand(options);
    }


    /**
     * Reads {@link InputStream} line by line and converts it into a {@link String}.
     *
     * @param is Input stream. Always close at the end.
     * @return
     */
    public static String getStandardOutput(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedInputStream buffer = new BufferedInputStream(is);
        BufferedReader reader = new BufferedReader(new InputStreamReader(buffer));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);

            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return sb.toString();
    }
}
