package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation;

import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Jobs;

/**
 * @author Phil Jones
 *         Date: 21/06/11
 *         Time: 15:07
 */
public interface StepInstanceCreatingStep {

    public static final String ANALYSIS_JOB_NAMES_KEY = "ANALYSIS_JOB_NAMES";
    public static final String COMPLETION_JOB_NAME_KEY = "COMPLETION_JOB_NAME";
    public static final String USE_MATCH_LOOKUP_SERVICE = "USE_MATCH_LOOKUP_SERVICE";

    public void setJobs(Jobs jobs);

    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO);
}
