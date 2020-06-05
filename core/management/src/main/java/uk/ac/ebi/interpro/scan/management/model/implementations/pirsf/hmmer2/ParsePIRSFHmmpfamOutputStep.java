package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.AbstractParseHmmpfamOutputStep;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;

/**
 * This class parses and persists the output from hmmpfam for PIRSF.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParsePIRSFHmmpfamOutputStep extends AbstractParseHmmpfamOutputStep<PIRSFHmmer2RawMatch> {

    private static final Logger LOGGER = LogManager.getLogger(ParsePIRSFHmmpfamOutputStep.class.getName());

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        try {
            super.execute(stepInstance, temporaryFileDirectory);
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Skipped step instance with ID " + stepInstance.getId() + " which usually store subfamilies raw matches (for cases the run binary step for subfams didn't " +
                            "produce any result file which needs to be parse.");
                }
            }
        }
    }
}
