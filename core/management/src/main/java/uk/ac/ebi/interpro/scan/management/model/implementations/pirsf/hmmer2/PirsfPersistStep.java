package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfMatchTempParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
    protected String filteredMatchesFileName;

    // Matches passed post processing (blast required)
    protected String blastedMatchesFileName;

    private String signatureLibraryRelease;

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
        //do we need to skip
        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(10, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        double signatureLibraryReleaseValue = Double.parseDouble(signatureLibraryRelease);
        if (signatureLibraryReleaseValue <= 2.74d) {
            Set<RawProtein<PIRSFHmmer2RawMatch>> filteredRawMatches = new HashSet<RawProtein<PIRSFHmmer2RawMatch>>();

            // Retrieve list of filtered matches from temporary file - blast wasn't required for these
            final String filteredMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, filteredMatchesFileName);
            try {
                Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches = PirsfMatchTempParser.parse(filteredMatchesFilePath);
                filteredRawMatches.addAll(rawMatches);
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when parsing filtered matches file " + filteredMatchesFilePath);
            }

            // Retrieve list of filtered matches from temporary file - blast WAS required for these
            final String blastedMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastedMatchesFileName);
            try {
                Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches = PirsfMatchTempParser.parse(blastedMatchesFilePath);
                filteredRawMatches.addAll(rawMatches);
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when parsing blasted matches file " + blastedMatchesFilePath);
            }

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
        } else {
            throw new IllegalStateException("Step instance with ID " + stepInstance.getId() + " only supports signature library release version <= 2.74");
        }
    }
}