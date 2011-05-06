package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.pirsf.FilteredMatchesFileParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.IOException;
import java.util.*;

/**
 * Represents the persistence step of the filtered raw matches at the end of the post processing workflow.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */

public class PirsfPersistStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PirsfPersistStep.class.getName());

    // Matches passed post processing (no blast required)
    private String filteredMatchesFileName;

    // Matches passed post processing (blast required)
    private String blastedMatchesFileName;

    private String signatureLibraryRelease;

    private RawMatchDAO<PIRSFHmmer2RawMatch> rawMatchDAO;

    private FilteredMatchDAO<PIRSFHmmer2RawMatch, Hmmer2Match> filteredMatchDAO;

    @Required
    public void setFilteredMatchesFileName(String filteredMatchesFileName) {
        this.filteredMatchesFileName = filteredMatchesFileName;
    }

    @Required
    public void setBlastedMatchesFileName(String blastedMatchesFileName) {
        this.blastedMatchesFileName = blastedMatchesFileName;
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

        // Retrieve list of filtered protein Ids from temporary files

        final String filteredMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, filteredMatchesFileName);
        Set<Long> filteredProteinIds = new HashSet<Long>();
        try {
            filteredProteinIds.addAll(FilteredMatchesFileParser.parse(filteredMatchesFilePath));
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when parsing filtered matches file " + filteredMatchesFilePath);
        }

        final String blastedMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastedMatchesFileName);
        try {
            filteredProteinIds.addAll(FilteredMatchesFileParser.parse(blastedMatchesFilePath));
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when parsing blasted matches file " + blastedMatchesFilePath);
        }

        Set<RawProtein<PIRSFHmmer2RawMatch>> filteredRawMatches = rawMatchDAO.getProteinsByIds(filteredProteinIds, signatureLibraryRelease);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("PIRSF: Retrieved " + filteredRawMatches.size() + " proteins.");
            int matchCount = 0;
            for (final RawProtein rawProtein : filteredRawMatches) {
                matchCount += rawProtein.getMatches().size();
            }
            LOGGER.debug("PIRSF: A total of " + matchCount + " raw matches.");
        }

        // Persist the remaining (filtered) raw matches
        LOGGER.info("Persisting filtered raw matches...");
        filteredMatchDAO.persist(filteredRawMatches);
    }

}
