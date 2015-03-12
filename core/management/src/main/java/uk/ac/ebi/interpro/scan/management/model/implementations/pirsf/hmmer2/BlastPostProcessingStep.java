package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2.BlastPostProcessor;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfBlastResultParser;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfMatchTempParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Perform second post processing match filtering step.
 *
 * @author Matthew Fraser, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class BlastPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(BlastPostProcessingStep.class.getName());

    private BlastPostProcessor postProcessor;

    private String blastResultOutputFileName;

    private String blastMatchesFileName;

    private String blastedMatchesFileName;

    @Required
    public void setBlastMatchesFileName(String blastMatchesFileName) {
        this.blastMatchesFileName = blastMatchesFileName;
    }

    @Required
    public void setPostProcessor(BlastPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setBlastResultOutputFileName(String blastResultOutputFileName) {
        this.blastResultOutputFileName = blastResultOutputFileName;
    }

    @Required
    public void setBlastedMatchesFileName(String blastedMatchesFileName) {
        this.blastedMatchesFileName = blastedMatchesFileName;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     * <p/>
     * Implementations of this method MAY call delayForNfs() before starting, if, for example,
     * they are operating of file system resources.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        // Prepare temporary filenames required by this step
        final String matchesToBlastFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastMatchesFileName); // Matches to blast
        final String blastOutputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastResultOutputFileName); // Blast output
        final String blastedMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastedMatchesFileName); // Matches that passed the blast filtering step

        Set<RawProtein<PIRSFHmmer2RawMatch>> proteinsToBlast;
        try {
            // Matches to blast
            proteinsToBlast = PirsfMatchTempParser.parse(matchesToBlastFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when parsing blast matches file " + matchesToBlastFilePath);
        }

        try {
            Map<String, Integer> blastResultMap = PirsfBlastResultParser.parseBlastOutputFile(blastOutputFileName);
            postProcessor.process(proteinsToBlast, blastResultMap, blastedMatchesFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to read BLAST result output file.", e);
        }
    }
}
