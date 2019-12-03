package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factors out common functionality required by classes that
 * create new StepInstances
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractStepInstanceCreator {

    private static final Logger LOGGER = Logger.getLogger(AbstractStepInstanceCreator.class.getName());

    protected StepInstanceDAO stepInstanceDAO;
    protected Jobs jobs;
    protected Map<String, String> parameters;

    /**
     * Utility method to return the minimum of two Long values,
     * either of which may be null.
     *
     * @param l1 first Long to compare
     * @param l2 second Long to compare
     * @return the minimum of two Long values,
     *         either of which may be null.  If one is null, returns the non-null Long.
     *         If both are null, returns null.
     */
    protected static Long min(Long l1, Long l2) {
        if (l1 == null && l2 == null) {
            return null;
        }
        if (l2 == null) {
            return l1;
        }
        if (l1 == null) {
            return l2;
        }
        return Math.min(l1, l2);
    }

    /**
     * Utility method to return the maximum of two Long values,
     * either of which may be null.
     *
     * @param l1 first Long to compare
     * @param l2 second Long to compare
     * @return the maximum of two Long values,
     *         either of which may be null.  If one is null, returns the non-null Long.
     *         If both are null, returns null.
     */
    protected static Long max(Long l1, Long l2) {
        if (l1 == null && l2 == null) {
            return null;
        }
        if (l2 == null) {
            return l1;
        }
        if (l1 == null) {
            return l2;
        }
        return Math.max(l1, l2);
    }

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    /**
     * Should be private - but want to junit test as prone to boundary errors!
     *
     * @param step            being the Step for which StepInstances should be created
     * @param bottomProteinId being the lowest protein primary key to consider
     * @param topProteinId    being the highest protein primary key to consider.
     * @return a List of StepInstance objects for the Step passed in.
     */
    protected List<StepInstance> createStepInstances(Step step, Long bottomProteinId, Long topProteinId) {
        final List<StepInstance> stepInstances = new ArrayList<StepInstance>();
        Utilities.verboseLog(20, "step.getMaxProteins(): " + step.getMaxProteins());
        //LOGGER.warn( "step.getMaxProteins(): " + step.getMaxProteins());
        final long sliceSize = (step.getMaxProteins() == null)
                ? topProteinId - bottomProteinId + 1    // No maximum number of proteins, so all of them!
                : step.getMaxProteins();                // Limit to the slice size.

        // Make StepInstances for the appropriate slice size.
        for (long bottom = bottomProteinId; bottom <= topProteinId; bottom += sliceSize) {
            final long top = Math.min(topProteinId, bottom + sliceSize - 1);
            stepInstances.add(new StepInstance(step, bottom, top, null, null));
        }
        return stepInstances;
    }

    /**
     * Takes a list of newly created StepInstance objects in a Map<Step, List<StepInstance>>
     * and sets up the dependencies between them.  Then stores the StepInstance objects to the database.
     *
     * @param stepToStepInstances a Map<Step, List<StepInstance>> to allow the dependencies to be efficiently set up.
     */
    protected void addDependenciesAndStore(Map<Step, List<StepInstance>> stepToStepInstances) {
        // Add the dependencies to the StepInstances.

        for (Step step : stepToStepInstances.keySet()) {
            for (StepInstance stepInstance : stepToStepInstances.get(step)) {
                final List<Step> dependsUpon = stepInstance.getStep(jobs).getDependsUpon();
                if (dependsUpon != null) {
                    for (Step stepRequired : dependsUpon) {
                        List<StepInstance> candidateStepInstances = stepToStepInstances.get(stepRequired);
                        if (candidateStepInstances != null) {
                            for (StepInstance candidate : candidateStepInstances) {
                                if (stepInstance.proteinBoundsOverlap(candidate)) {
                                    stepInstance.addDependentStepInstance(candidate);
                                }
                            }
                        }
                    }
                }
            }
            // Persist the StepInstances that now have their dependencies added.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + stepToStepInstances.get(step).size() + " StepInstances for Step " + step.getId());
            }
        }
        stepInstanceDAO.insert(stepToStepInstances);
    }
}
