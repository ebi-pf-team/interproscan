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
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


/**
 * Install InterProScan
 *
 * @author David Binns, EMBL-EBI, InterPro
 * @author Phil Jones, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 */
public class Installer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private SignatureLibraryReleaseDAO signatureLibraryReleaseDAO;

    private StepCreationSignatureDatabaseLoadListener stepCreationSignatureDatabaseLoadListener;

    private List<ModelFileParser> parsers;

    private JdbcEntryDao jdbcEntryDAO;

    private ReleaseDAO releaseDAO;

    /**
     * For more information please have a look at the enum.
     */
    private InstallerMode mode = Installer.InstallerMode.LOAD_ALL;


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

    /**
     * Set only from Spring, so lack of thread safety not a problem.
     *
     * @param mode
     */
    public void setMode(InstallerMode mode) {
        this.mode = mode;
    }

    @Override
    public void run() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Schema creation");
            LOGGER.info("Running installer in mode " + mode);
        }
        // By Magic!
          
        Utilities.verboseLog = true;
        Utilities.verboseLogLevel = 44;

        if (mode.equals(InstallerMode.LOAD_NONE)) {
            LOGGER.info("No signatures or entries will be loaded (empty database).");
            return;
        }
        if (mode.equals(Installer.InstallerMode.LOAD_ALL) || mode.equals(Installer.InstallerMode.LOAD_ENTRIES)) {
            LOGGER.info("Testing JDBC Connection");
            try {
                if (!testJDBC()) {
                    throw new SQLException("No connection available.");
                }
            } catch (SQLException e) {
                LOGGER.info("Unable to connect!!");
                e.printStackTrace();
                System.exit(1);
            }
            LOGGER.info("OK");
        }

        if (mode.equals(Installer.InstallerMode.LOAD_ALL) || mode.equals(Installer.InstallerMode.LOAD_MODELS)) {
            LOGGER.info("Loading signatures");
            loadModels();
            LOGGER.info("Loaded signatures");
        }
        if (mode.equals(Installer.InstallerMode.LOAD_ALL) || mode.equals(Installer.InstallerMode.LOAD_ENTRIES)) {
            LOGGER.info("Loading entries and related info");
            loadEntries();
            LOGGER.info("Loaded entries and related info");
        }
    }

    private boolean testJDBC() throws SQLException {
        return jdbcEntryDAO.testConnection();
    }

    private void loadEntries() {
        String releaseVersion = jdbcEntryDAO.getLatestDatabaseReleaseVersion();
        Release interProRelease = releaseDAO.getReleaseByVersion(releaseVersion);
        LOGGER.warn("Loading InterPro entries - " + releaseVersion + " version number " + interProRelease);
        if (interProRelease == null) {
            interProRelease = releaseDAO.insert(new Release(releaseVersion));
        }
        Long releaseId = (interProRelease == null ? new Long(-1) : interProRelease.getId());
        if (LOGGER.isInfoEnabled()) {
            long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            LOGGER.info("Current memory usage: " + heap + " bytes (" + (heap / 131072 * 0.125) + " MB)");
        }
        //load all entries
        jdbcEntryDAO.loadEntriesAndMappings(releaseId);
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
                if (stepCreationSignatureDatabaseLoadListener != null && parser.getAnalysisJobId() != null) {
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

    /**
     * Describes in which mode the installer should be run.
     * <p/>
     * LOAD_MODELS - Loads member database models/signatures into I5 database.
     * LOAD_ENTRIES - Loads all entries of the latest release into I5 database, including cross references like Pathways and GO terms.
     * LOAD_ALL - Default value. Loads models and entries at the same time into I5 database. PLEASE NOTICE: The LOAD_ENTRIES step also creates relations between
     * signatures and entries. So to create these relations you have to run the LOAD_MODELS step beforehand.
     * LOAD_NONE - Do not load any databse models/signatures into I5 database. This mode therefore just creates an empty database.
     */
    public enum InstallerMode {
        LOAD_MODELS, LOAD_ENTRIES, LOAD_ALL, LOAD_NONE
    }
}
