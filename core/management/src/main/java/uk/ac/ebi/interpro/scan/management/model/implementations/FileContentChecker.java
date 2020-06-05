package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Simple utility to check files in general, for instance if a file is empty.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FileContentChecker {

    private static final Logger LOGGER = LogManager.getLogger(FileContentChecker.class.getName());

    private final File fileToCheck;

    public FileContentChecker(File fileToCheck) {
        if (fileToCheck == null) {
            throw new IllegalArgumentException("File content checker doesn't allow NULL values!");
        } else if (!fileToCheck.exists()) {
            throw new IllegalArgumentException("The specified files doesn't exist - " + fileToCheck.getAbsolutePath() + "!");
        } else if (!fileToCheck.canRead()) {
            throw new IllegalArgumentException("File content checker doesn't have read access to the specified files - " + fileToCheck.getAbsolutePath() + "!");
        }
        this.fileToCheck = fileToCheck;
    }

    public boolean isFileEmpty() {
        FileInputStream fis;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking if the specified file is empty...");
            }
            fis = new FileInputStream(fileToCheck);
            int iByteCount = fis.read();
            if (iByteCount == -1) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}