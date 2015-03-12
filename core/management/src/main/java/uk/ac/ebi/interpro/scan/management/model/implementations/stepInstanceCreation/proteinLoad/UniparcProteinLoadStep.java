package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.uniparcdb.LoadUniParcFromDB;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

/**
 * Loads proteins from the UniParc database
 * and create StepInstances as required.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class UniparcProteinLoadStep extends Step {

    private LoadUniParcFromDB uniparcLoader;

    @Required
    public void setUniparcLoader(LoadUniParcFromDB uniparcLoader) {
        this.uniparcLoader = uniparcLoader;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        uniparcLoader.loadNewSequences();
    }
}
