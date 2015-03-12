package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2.SubfamilyPostProcessor;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.IOException;
import java.util.Set;

/**
 * Perform first post processing match filtering step.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */

public class SubfamilyPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(SubfamilyPostProcessingStep.class.getName());

    private SubfamilyPostProcessor postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<PIRSFHmmer2RawMatch> rawMatchDAO;

    // Temporary file to hold matches that pass post processing in this step (subfamilies)
    private String subFamilyMatchesFileName;

    private String subFamilyMapFileName;


    @Required
    public void setPostProcessor(SubfamilyPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<PIRSFHmmer2RawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setSubFamilyMatchesFileName(String subFamilyMatchesFileName) {
        this.subFamilyMatchesFileName = subFamilyMatchesFileName;
    }

    @Required
    public void setSubFamilyMapFileName(String subFamilyMapFileName) {
        this.subFamilyMapFileName = subFamilyMapFileName;
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

        // Retrieve protein data (need the sequence length)
        long bottom = stepInstance.getBottomProtein();
        long top = stepInstance.getTopProtein();

        // Retrieve raw results for protein range.
        Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                bottom,
                top,
                signatureLibraryRelease
        );
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("PIRSF: Retrieved " + rawMatches.size() + " proteins to post-process.");
            int matchCount = 0;
            for (final RawProtein rawProtein : rawMatches) {
                matchCount += rawProtein.getMatches().size();
            }
            LOGGER.debug("PIRSF: A total of " + matchCount + " raw matches.");
        }

        // Prepare temporary filenames required by this step
        final String subFamilyMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, subFamilyMatchesFileName);
        final String subFamilyMapFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, subFamilyMapFileName);
        final Resource subFamilyMapFileResource = new FileSystemResource(subFamilyMapFilePath);

        LOGGER.debug("Executing subfamily post processing...");
        try {
            postProcessor.process(rawMatches, subFamilyMatchesFilePath, subFamilyMapFilePath, subFamilyMapFileResource);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to read subfamily mapping file!", e);
        }
    }
}