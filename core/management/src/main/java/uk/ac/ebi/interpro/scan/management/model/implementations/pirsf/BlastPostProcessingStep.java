package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.BlastPostProcessor;
import uk.ac.ebi.interpro.scan.io.pirsf.BlastMatchesFileParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfBlastResultParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.IOException;
import java.util.Map;

/**
 * @author Matthew Fraser, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class BlastPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(BlastPostProcessingStep.class.getName());

    private BlastPostProcessor postProcessor;

    private String blastResultOutputFileName;

    private String blastMatchesFileName;

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

        final String blastMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastMatchesFileName);
        Map<Long, String> proteinIdMap = null;
        try {
            proteinIdMap = BlastMatchesFileParser.parse(blastMatchesFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when parsing blast matches file " + blastMatchesFilePath);
        }

        final String blastOutFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastResultOutputFileName);
        try {
            Map<String, Integer> blastResultMap = PirsfBlastResultParser.parseBlastOutputFile(blastOutFileName);

            postProcessor.process(proteinIdMap, blastResultMap, temporaryFileDirectory);


        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to read BLAST result output file.", e);
        }
    }
}
