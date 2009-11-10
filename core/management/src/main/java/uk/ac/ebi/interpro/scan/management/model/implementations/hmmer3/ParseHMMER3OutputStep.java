package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.Step;
import org.springframework.beans.factory.annotation.Required;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseHMMER3OutputStep extends Step {

    /**
     * The parser has a reference to the run binary step because:
     * (a) the binary must have been defined to have a file to parse in the first place, and
     * (b) lots of configuration of the binary step will be required here.
     */
    private RunHMMER3Step runBinaryStep;

    @Required
    public void setRunBinaryStep(RunHMMER3Step runBinaryStep) {
        this.runBinaryStep = runBinaryStep;
    }
}
