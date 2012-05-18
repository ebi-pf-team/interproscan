package uk.ac.ebi.interpro.scan.management.model;

import java.io.File;
import java.util.Map;

/**
 * Determines the status of a given job.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class JobStatusChecker {
    public static JobStatusWrapper getJobStatus(Job job) {
        Map<String, String> mandatoryParams = job.getMandatoryParameters();
        // Check that mandatory parameters are set
        if (mandatoryParams != null) {
            StringBuilder missingParameters = new StringBuilder();
            for (String paramKey : mandatoryParams.keySet()) {
                String paramValue = mandatoryParams.get(paramKey);
                if (paramValue == null || paramValue.trim().length() == 0) {
                    if (missingParameters.length() > 0) {
                        missingParameters.append(", ");
                    }
                    missingParameters.append(paramKey);
                }
            }
            if (missingParameters.length() > 0) {
                return new JobStatusWrapper(JobStatusWrapper.JobStatus.DEACTIVATED, "Analysis " + job.getId().replace("job", "") + " is deactivated, because the following parameters are not set in the interproscan.properties file: " + missingParameters);
            }
        }
        // Check that the specified resources are available
        if (job.getNonEmptyPaths() != null) {
            StringBuilder missingPaths = new StringBuilder();
            for (String path : job.getNonEmptyPaths()) {
                File file = new File(path);
                if (!file.exists()) {
                    if (missingPaths.length() > 0) {
                        missingPaths.append(", ");
                    }
                    missingPaths.append(path);
                }
            }
            if (missingPaths.length() > 0) {
                return new JobStatusWrapper(JobStatusWrapper.JobStatus.DEACTIVATED, "Analysis " + job.getId().replace("job", "") + " is deactivated, because the resources expected at the following paths do not exist: " + missingPaths);
            }
        }
        return new JobStatusWrapper();
    }
}
