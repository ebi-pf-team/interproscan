package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.PirsfPostProcessing;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatFileParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PIRSFPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PIRSFPostProcessingStep.class.getName());

    private PirsfPostProcessing postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<PIRSFHmmer2RawMatch> rawMatchDAO;

    private FilteredMatchDAO<PIRSFHmmer2RawMatch, Hmmer2Match> filteredMatchDAO;

    @Required
    public void setPostProcessor(PirsfPostProcessing postProcessor) {
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
    public void setFilteredMatchDAO(FilteredMatchDAO<PIRSFHmmer2RawMatch, Hmmer2Match> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
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
        // Retrieve raw results for protein range.
        Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
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

        try {
            // Filter the raw matches
            Map<String, RawProtein<PIRSFHmmer2RawMatch>> filteredMatches = postProcessor.process(rawMatches);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("PIRSF: " + filteredMatches.size() + " proteins passed through post processing.");
                int matchCount = 0;
                for (final RawProtein rawProtein : filteredMatches.values()) {
                    matchCount += rawProtein.getMatches().size();
                }
                LOGGER.debug("PIRSF: A total of " + matchCount + " matches PASSED.");
            }

            // Persist the remaining (filtered) raw matches
            filteredMatchDAO.persist(filteredMatches.values());
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to post process filtered PIRSF matches.", e);
        }
    }
}
