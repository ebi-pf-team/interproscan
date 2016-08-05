package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation;

import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Jobs;

/**
 * @author Phil Jones
 *         Date: 21/06/11
 *         Time: 15:07
 */
public interface StepInstanceCreatingStep {

    String ANALYSIS_JOB_NAMES_KEY = "ANALYSIS_JOB_NAMES";
    String COMPLETION_JOB_NAME_KEY = "COMPLETION_JOB_NAME";
    String USE_MATCH_LOOKUP_SERVICE = "USE_MATCH_LOOKUP_SERVICE";
    String EXCLUDE_SITES = "EXCLUDE_SITES";


    void setJobs(Jobs jobs);

    void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO);
}
