package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Restores the in-memory database from a backup file.
 * <p/>
 * Run as a Singleton in a single thread - this implementation is NOT thread safe.
 * <p/>
 * Only works in its current form with H2.
 *
 * @author Phil Jones
 * @author John Maslen
 *         Date: May 7, 2010
 *         Time: 9:32:05 AM
 */


public class CleanRunDatabase implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(CleanRunDatabase.class.getName());

    private String databaseBackupFile;

    private boolean stillLoading = true;

    private boolean parentProcessRunning = true;

    private String inMemoryDatabaseDriverClass;

    private String inMemoryDatabaseURL;

    private String inMemoryDatabaseUsername;

    private String inMemoryDatabasePassword;

    @Required
    public void setDatabaseBackupFile(String databaseBackupFile) {
        this.databaseBackupFile = databaseBackupFile;
    }

    @Required
    public void setInMemoryDatabaseDriverClass(String inMemoryDatabaseDriverClass) {
        this.inMemoryDatabaseDriverClass = inMemoryDatabaseDriverClass;
    }

    @Required
    public void setInMemoryDatabaseURL(String inMemoryDatabaseURL) {
        this.inMemoryDatabaseURL = inMemoryDatabaseURL;
    }

    @Required
    public void setInMemoryDatabaseUsername(String inMemoryDatabaseUsername) {
        this.inMemoryDatabaseUsername = inMemoryDatabaseUsername;
    }

    @Required
    public void setInMemoryDatabasePassword(String inMemoryDatabasePassword) {
        this.inMemoryDatabasePassword = inMemoryDatabasePassword;
    }


    @Override
    public void run() {
        try {
            cleanInstalledDatabase();
        } catch (SQLException sqle) {
            throw new IllegalStateException("SQLException thrown when attempting to close connection to the in memory database.", sqle);
        } catch (IOException ioe) {
            throw new IllegalStateException("IOException thrown when attempting to load database backup file.", ioe);
        }
    }

    public void closeDatabaseCleaner() {
        this.parentProcessRunning = false;
    }

    public void cleanInstalledDatabase() throws SQLException, IOException {
        // Filter the path for the database, if necessary.
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("original Database path = " + databaseBackupFile);
        }
        // First, try on the file system
        Resource databaseBackupResource = new FileSystemResource(databaseBackupFile);
        if (!databaseBackupResource.exists()) {
            // Try again on the classpath
            databaseBackupResource = new ClassPathResource(databaseBackupFile);
        }
        if (!databaseBackupResource.exists()) {
            LOGGER.fatal("Unable to find original database file: " + databaseBackupFile);
            throw new IllegalStateException("Unable to find original database file");
        }
        File originalFile = databaseBackupResource.getFile();
        Connection conn = null;
        try {
            Class.forName(inMemoryDatabaseDriverClass);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("inMemoryDatabaseURL = " + inMemoryDatabaseURL);
            }

            conn = DriverManager.getConnection(inMemoryDatabaseURL, inMemoryDatabaseUsername, inMemoryDatabasePassword);

            // TODO - this statement is H2 specific.
            conn.createStatement().execute("RUNSCRIPT from '" + originalFile.getAbsolutePath() + "' COMPRESSION ZIP");
            stillLoading = false;
            while (parentProcessRunning) {
                // To ensure the in-memory database restored in this method is used by the
                // parent process, keep this thread alive.
                Thread.sleep(500);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalStateException("The CleanRunDatabase class cannot load the database driver.", cnfe);
        } catch (SQLException sqle) {
            throw new IllegalStateException("An SQLException has been thrown when attempting to restore the in-memory database.", sqle);
        } catch (InterruptedException ie) {
            throw new IllegalStateException("An InterruptedException was thrown by the CleanRunDatabase thread.", ie);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public boolean stillLoading() {
        return stillLoading;
    }
}
