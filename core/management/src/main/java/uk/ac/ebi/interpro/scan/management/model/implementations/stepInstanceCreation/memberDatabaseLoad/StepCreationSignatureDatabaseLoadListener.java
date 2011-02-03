package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.memberDatabaseLoad;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.signatures.SignatureDatabaseLoadListener;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.AbstractStepInstanceCreator;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This listener is responsible for creating new StepInstances
 * when a new member database is loaded.  Naturally it relies upon
 * a new Job XML file having been created.
 * <p/>
 * The jobXML ID is passed in as an argument to allow this to work.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StepCreationSignatureDatabaseLoadListener
        extends AbstractStepInstanceCreator
        implements SignatureDatabaseLoadListener {

    private static final Logger LOGGER = Logger.getLogger(StepCreationSignatureDatabaseLoadListener.class.getName());


    private ProteinDAO proteinDAO;

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    /**
     * Optional constructor for use by Spring - do not remove.
     */
    public StepCreationSignatureDatabaseLoadListener() {
    }

    public StepCreationSignatureDatabaseLoadListener(Jobs jobs, Map<String, String> parameters) {
        this.jobs = jobs;
        this.parameters = parameters;
    }

    /**
     * A new set of member database signatures has been loaded into the database.
     * For ALL proteins in the database, new StepInstances should be created FOR THIS MEMBER DATABASE RELEASE.
     *
     * @param release      being the SignatureLibraryRelease object - indicates which member database Steps must be created.
     * @param analysisName being the name of the job, for which StepInstances must be added for this SignatureLibraryRelease
     */
    public void signatureDatabaseLoaded(SignatureLibraryRelease release, String analysisName) {
        try {
            final Map<Step, List<StepInstance>> stepToStepInstances = new HashMap<Step, List<StepInstance>>();
            final Long topProteinId = proteinDAO.getMaximumPrimaryKey();

            // Instantiate the StepInstances - no dependencies yet.
            for (Job job : jobs.getJobList()) {
                if (analysisName.equals(job.getId())) {
                    for (Step step : job.getSteps()) {
                        if (step.isCreateStepInstancesForNewProteins()) {
                            final List<StepInstance> jobStepInstances = createStepInstances(step, 0L, topProteinId);
                            stepToStepInstances.put(step, jobStepInstances);
                        }
                    }
                }
            }
            // Add the dependencies between the StepInstances and store them to the database.
            addDependenciesAndStore(stepToStepInstances);
        } catch (Exception e) {
            LOGGER.error("Exception thrown in createStepInstances() method: ", e);
            throw new IllegalStateException("Caught and logged Exception, re-thrown so things work properly.", e);
        }
    }
}
