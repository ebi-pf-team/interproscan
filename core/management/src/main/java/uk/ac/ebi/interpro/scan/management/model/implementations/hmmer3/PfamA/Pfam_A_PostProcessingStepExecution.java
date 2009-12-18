package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:40:12
 */
@Entity
@DiscriminatorValue("pfam_A_postproc")
public class Pfam_A_PostProcessingStepExecution extends StepExecution<Pfam_A_PostProcessingStepInstance> implements Serializable {


    /**
     * Constructor that accepts a UUID as a unique identifier
     * for the StepInstance and a reference to the Step
     * to allow access to Step configuration.
     *
     * @param stepInstance the StepInstance used to create this StepExecution.
     */
    protected Pfam_A_PostProcessingStepExecution(Pfam_A_PostProcessingStepInstance stepInstance) {
        super(stepInstance);
    }

    /**
     * DO NOT USE - For JPA only.
     */
    protected Pfam_A_PostProcessingStepExecution() {
    }

    /**
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     * <p/>
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     * @param daoManager required to allow data retrieval and storage in this process.
     */
    @Override
    public void execute(DAOManager daoManager) {
        this.setToRun();
        try{
            Thread.sleep(2000);  // Have a snooze to allow NFS to catch up.
            if (daoManager == null){
                throw new IllegalArgumentException ("This StepExecution must have a valid DAOManager object passed in.");
            }
            // Retrieve raw results for protein range.
            Map<String, RawProtein<PfamHmmer3RawMatch>> rawMatches = daoManager.getPfamRawMatchDAO().getRawMatchesForProteinIdsInRange(
                    Long.toString(this.getStepInstance().getBottomProtein()),
                    Long.toString(this.getStepInstance().getTopProtein()),
                    this.getStepInstance().getStep().getSignatureLibraryRelease()
            );

            if (LOGGER.isDebugEnabled()){
                LOGGER.debug("Count of unfiltered matches: " + countMatches(rawMatches));
            }
            // Post process
            Map<String, RawProtein<PfamHmmer3RawMatch>> filteredMatches = this.getStepInstance().getStep().getPostProcessor().process(rawMatches);
            if (LOGGER.isDebugEnabled()){
                LOGGER.debug("Count of filtered matches: " + countMatches(filteredMatches));
            }
            LOGGER.debug("About to store filtered matches...");
            daoManager.getPfamFilteredMatchDAO().persistFilteredMatches(filteredMatches.values());
            LOGGER.debug("About to store filtered matches...DONE");
            this.completeSuccessfully();
        } catch (Exception e) {
            this.fail();
            // TODO - Complete explanation.
            LOGGER.error ("Exception thrown:" , e);
        }
    }

    private int countMatches(Map<String, RawProtein<PfamHmmer3RawMatch>> matches) {
        int count = 0;
        for (RawProtein<PfamHmmer3RawMatch> protein : matches.values()){
            if (protein.getMatches() != null) count += protein.getMatches().size();
        }
        return count;
    }
}
