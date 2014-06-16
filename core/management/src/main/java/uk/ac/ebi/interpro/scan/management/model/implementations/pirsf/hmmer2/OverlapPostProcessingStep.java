package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2.OverlapPostProcessor;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Perform first post processing match filtering step.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */

public class OverlapPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(OverlapPostProcessingStep.class.getName());

    private OverlapPostProcessor postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<PIRSFHmmer2RawMatch> rawMatchDAO;

    private ProteinDAO proteinDAO;

    // Temporary file to hold matches that pass post processing in this step (no blast required)
    private String filteredMatchesFileName;

    // Matches that passed this post processing but now need to be sent to blast
    private String blastMatchesFileName;

    // Name of the file which maps subfamilies to superfamilies
    private String subfamToSuperfamMapFileName;


    @Required
    public void setPostProcessor(OverlapPostProcessor postProcessor) {
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
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setFilteredMatchesFileName(String filteredMatchesFileName) {
        this.filteredMatchesFileName = filteredMatchesFileName;
    }

    @Required
    public void setBlastMatchesFileName(String blastMatchesFileName) {
        this.blastMatchesFileName = blastMatchesFileName;
    }

    public void setSubfamToSuperfamMapFileName(String subfamToSuperfamMapFileName) {
        this.subfamToSuperfamMapFileName = subfamToSuperfamMapFileName;
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
        Map<Long, Integer> proteinLengthMap = getProteinSequenceLengths(bottom, top);

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
        final String filteredMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, filteredMatchesFileName);
        final String blastMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastMatchesFileName);
        double signatureLibraryReleaseValue = Double.parseDouble(signatureLibraryRelease);
        String subFamiliesFilePath = null;
        if (signatureLibraryReleaseValue >= 2.75d && subfamToSuperfamMapFileName != null) {
            subFamiliesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, subfamToSuperfamMapFileName);
        }
        try {
            postProcessor.process(rawMatches, proteinLengthMap, filteredMatchesFilePath, blastMatchesFilePath, subFamiliesFilePath, signatureLibraryReleaseValue);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to write flat files for filtered matches and " +
                    "matches which have to be BLASTed!", e);
        }
    }

    /**
     * Build up a list of the protein lengths.
     *
     * @param bottomProtein Protein Id to start from
     * @param topProtein    Protein Id to end on
     * @return Map of values
     */
    private Map<Long, Integer> getProteinSequenceLengths(long bottomProtein, long topProtein) {
        Map<Long, Integer> proteinLengthMap = new HashMap<Long, Integer>();

        List<Protein> proteins = proteinDAO.getProteinsBetweenIds(bottomProtein, topProtein);
        for (Protein protein : proteins) {
            proteinLengthMap.put(protein.getId(), protein.getSequenceLength());
        }

        return proteinLengthMap;
    }
}