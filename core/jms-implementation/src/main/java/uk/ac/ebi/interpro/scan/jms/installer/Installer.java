package uk.ac.ebi.interpro.scan.jms.installer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ModelFileParser;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.memberDatabaseLoad.StepCreationSignatureDatabaseLoadListener;
import uk.ac.ebi.interpro.scan.model.Release;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.ReleaseDAO;
import uk.ac.ebi.interpro.scan.persistence.SignatureLibraryReleaseDAO;
import uk.ac.ebi.interpro.scan.persistence.installer.JdbcEntryDao;

import java.io.IOException;
import java.util.List;


/**
 * Install InterProScan
 */
public class Installer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private SignatureLibraryReleaseDAO signatureLibraryReleaseDAO;

    private StepCreationSignatureDatabaseLoadListener stepCreationSignatureDatabaseLoadListener;

    private List<ModelFileParser> parsers;

    private JdbcEntryDao jdbcEntryDAO;

    private ReleaseDAO releaseDAO;


    @Required
    public void setSignatureLibraryReleaseDAO(SignatureLibraryReleaseDAO signatureLibraryReleaseDAO) {
        this.signatureLibraryReleaseDAO = signatureLibraryReleaseDAO;
    }

    @Required
    public void setParsers(List<ModelFileParser> parsers) {
        this.parsers = parsers;
    }

    public void setStepCreationSignatureDatabaseLoadListener(StepCreationSignatureDatabaseLoadListener stepCreationSignatureDatabaseLoadListener) {
        this.stepCreationSignatureDatabaseLoadListener = stepCreationSignatureDatabaseLoadListener;
    }

    @Required
    public void setJdbcEntryDAO(JdbcEntryDao jdbcEntryDAO) {
        this.jdbcEntryDAO = jdbcEntryDAO;
    }

    @Required
    public void setReleaseDAO(ReleaseDAO releaseDAO) {
        this.releaseDAO = releaseDAO;
    }

    @Override
    public void run() {
        LOGGER.info("Schema creation");
        // By Magic!

//        LOGGER.info("Loading signatures");
//        loadModels();
//        LOGGER.info("Loaded signatures");

        LOGGER.info("Loading entries and related info");
        loadEntries();
        LOGGER.info("Loaded entries and related info");

    }

    private void loadEntries() {
        String releaseVersion = jdbcEntryDAO.getLatestDatabaseReleaseVersion();
        Release interProRelease = releaseDAO.getReleaseByVersion(releaseVersion);
        if (interProRelease == null) {
            interProRelease = releaseDAO.insert(new Release(releaseVersion));
        }
        Long releaseId = (interProRelease == null ? new Long(-1) : interProRelease.getId());
        long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        LOGGER.info("Current memory usage: " + heap + " bytes (" + (heap / 131072 * 0.125) + " MB)");
        //load all entries
        jdbcEntryDAO.loadEntriesAndMappings(releaseId); // TODO
    }

    private void loadModels() {
        loadModels(null, null);
    }

    private void loadModels(SignatureLibrary library, String versionNumber) {
        if ((library == null) ^ (versionNumber == null)) {
            throw new IllegalArgumentException("When calling Installer.loadModels(SignatureLibrary library, String versionNumber) " +
                    "the two arguments must either both be null, or both be set.");
        }
        boolean loadedAtLeastOneLibrary = false;
        if (parsers != null) {
            for (ModelFileParser parser : parsers) {
                if (library != null) {
                    // Model loading has been restricted to a single Signature library release.
                    if ((!library.equals(parser.getSignatureLibrary())) || (!(versionNumber.equals(parser.getReleaseVersionNumber())))) {
                        continue;
                    }
                }
                LOGGER.warn("Loading " + parser.getSignatureLibrary() + " version number " + parser.getReleaseVersionNumber());
                if (signatureLibraryReleaseDAO.isReleaseAlreadyPersisted(parser.getSignatureLibrary(), parser.getReleaseVersionNumber())) {
                    LOGGER.warn(parser.getSignatureLibrary() + " version " + parser.getReleaseVersionNumber() + " is already loaded.");
                    return;
                }
                final SignatureLibraryRelease release;
                try {
                    release = parser.parse();
                } catch (IOException e) {
                    LOGGER.fatal("IOException thrown when parsing using the " + parser.getClass().getName() + " parser.", e);
                    throw new IllegalStateException("Unable to load " + parser.getSignatureLibrary() + " models.", e);
                }

                // Store the Models / Signatures to the database.
                signatureLibraryReleaseDAO.insert(release);

                // Finally, if configured, create StepInstances for any proteins currently in the database.
                if (stepCreationSignatureDatabaseLoadListener != null) {
                    stepCreationSignatureDatabaseLoadListener.signatureDatabaseLoaded(release, parser.getAnalysisJobId());
                }
                loadedAtLeastOneLibrary = true;
            }

        }
        if (!loadedAtLeastOneLibrary && library != null) {
            LOGGER.error(new StringBuilder()
                    .append("You have requested to load the signatures for ")
                    .append(library.getName())
                    .append(" version ")
                    .append(versionNumber)
                    .append(" however no configuration has been found.  Please check in the installer-context.xml configuration file.")
                    .toString()
            );
        }
    }
}
