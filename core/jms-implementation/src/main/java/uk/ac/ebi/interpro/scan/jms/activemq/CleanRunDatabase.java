package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: May 7, 2010
 * Time: 9:32:05 AM
 */

// TODO:  Consider use of org.apache.commons.io Class FileUtils.copyFile(File srcFile, File destFile)  rather than overwriting database file

// TODO:  Or alternatively could use java.nio for very fast (non-blocking) copy.

public class CleanRunDatabase implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(CleanRunDatabase.class);

    private String originalDatabasePath;

    private String installedDatabasePath;

    private TemporaryDirectoryManager directoryManager;

    @Required
    public void setOriginalDatabasePath(String originalDatabasePath) {
        this.originalDatabasePath = originalDatabasePath;
    }

    @Required
    public void setInstalledDatabasePath(String installedDatabasePath) {
        this.installedDatabasePath = installedDatabasePath;
    }

    @Required
    public void setDirectoryManager(TemporaryDirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    @Override
    public void run() {
        cleanInstalledDatabase();
    }

    public void cleanInstalledDatabase() {
        // Filter the path for the database, if necessary.
        final String filteredDatabasePath = directoryManager.replacePath(installedDatabasePath);
        LOGGER.debug("installed Database path = " + filteredDatabasePath);
        File installedFile = new File(filteredDatabasePath);
        LOGGER.debug("original Database path = " + originalDatabasePath);
        File originalFile = new File(originalDatabasePath);

        InputStream in;

        OutputStream out;

        if (!originalFile.exists()) {
            LOGGER.fatal("Unable to find original database file: " + originalDatabasePath);
            throw new IllegalStateException("Unable to find original database file");
        }

        if (!installedFile.exists()) {
            try {
                installedFile.createNewFile();
            } catch (IOException e) {
                LOGGER.fatal("Unable to create new file for database overwrite: " + filteredDatabasePath);
                throw new IllegalStateException("Unable to create new database file", e);
            }
        }

        try {
            LOGGER.info("Installed file length = " + installedFile.length());
            out = new FileOutputStream(installedFile);
        } catch (FileNotFoundException e) {
            LOGGER.fatal("Still unable to find file for database overwrite: " + filteredDatabasePath);
            throw new IllegalStateException("Unable to find installed database file", e);
        }


        try {
            LOGGER.info("original file length = " + originalFile.length());
            in = new FileInputStream(originalFile);
        } catch (FileNotFoundException e) {
            LOGGER.fatal("Unable to find original database file: " + originalDatabasePath);
            throw new IllegalStateException("Unable to find original database file", e);
        }

        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            LOGGER.fatal("Unable to find original database file: " + originalDatabasePath);
            throw new IllegalStateException("Unable to find original database file", e);
        }

        try {
            in.close();
            out.close();
        } catch (IOException e) {
            LOGGER.debug("Error closing database files.");
        }

        LOGGER.info("Installed database has been restored to the original.");
    }

}
