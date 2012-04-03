package uk.ac.ebi.interpro.scan.management.model;

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
        if (mandatoryParams != null) {
            for (String paramKey : mandatoryParams.keySet()) {
                String paramValue = mandatoryParams.get(paramKey);
                if (paramValue == null || paramValue.trim().length() == 0) {
                    return new JobStatusWrapper(JobStatusWrapper.JobStatus.DEACTIVATED, "Analysis " + job.getId().replace("job", "") + " is deactivated, because parameter " + paramKey + " is missing!");
                }
            }
        }
        return new JobStatusWrapper();
    }
}