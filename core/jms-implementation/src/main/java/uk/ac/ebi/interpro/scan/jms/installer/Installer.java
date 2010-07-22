package uk.ac.ebi.interpro.scan.jms.installer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ModelFileParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.SignatureLibraryReleaseDAO;

import java.io.IOException;
import java.util.List;


/**
 * Install InterProScan
 */
public class Installer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private SignatureLibraryReleaseDAO signatureLibraryReleaseDAO;

    private List<ModelFileParser> parsers;

    @Required
    public void setSignatureLibraryReleaseDAO(SignatureLibraryReleaseDAO signatureLibraryReleaseDAO) {
        this.signatureLibraryReleaseDAO = signatureLibraryReleaseDAO;
    }

    @Required
    public void setParsers(List<ModelFileParser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public void run() {
        LOGGER.info("Schema creation");
        // By Magic!
        LOGGER.info("Loading signatures");
        loadModels();
        LOGGER.info("Loaded signatures");
    }

    private void loadModels() {
        if (parsers != null) {
            for (ModelFileParser parser : parsers) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Loading " + parser.getSignatureLibrary() + " version number " + parser.getReleaseVersionNumber());
                }
                if (signatureLibraryReleaseDAO.isReleaseAlreadyPersisted(parser.getSignatureLibrary(), parser.getReleaseVersionNumber())) {
                    LOGGER.info(parser.getSignatureLibrary() + " version " + parser.getReleaseVersionNumber() + " is already loaded.");
                    return;
                }
                SignatureLibraryRelease release;
                try {
                    release = parser.parse();
                } catch (IOException e) {
                    LOGGER.fatal("IOException thrown when parsing using the " + parser.getClass().getName() + " parser.", e);
                    throw new IllegalStateException("Unable to load " + parser.getSignatureLibrary() + " models.", e);
                }

                // And store the Models / Signatures to the database.
                signatureLibraryReleaseDAO.insert(release);
            }
        }
    }
}
