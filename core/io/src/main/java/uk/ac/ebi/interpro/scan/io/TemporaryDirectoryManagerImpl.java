package uk.ac.ebi.interpro.scan.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Performs the job of naming & creating a suitable directory
 * for a particular instance of running the Stand alone master.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TemporaryDirectoryManagerImpl implements TemporaryDirectoryManager {

    private static final Logger LOGGER = LogManager.getLogger(TemporaryDirectoryManagerImpl.class.getName());

    private volatile String temporaryDirectoryName;

    private List<String> createdDirectories = new ArrayList<String>();

    private static final Object LOCK = new Object();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");

    /**
     * This method replaces (if found) the sub-string [TEMP] in any input String
     * with the value determined by the implementation of this interface.
     * <p/>
     * TODO: There is a possibility of a race condition where two separate processes (JVMs) are attempting
     * to create the same directory name simultaneously.  This needs to be addressed.
     *
     * @param inputURI being the path that may contain [TEMP] to filter.
     * @return the input String (if it does not contain [TEMP]) or a filtered String
     *         with [TEMP] replaced by ... er... whatever the implementation chooses.
     */
    @Override
    public String replacePath(String inputURI) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("About to filter: " + inputURI);
        }
        if (inputURI.contains(TemporaryDirectoryManager.DIRECTORY_TEMPLATE)) {

            synchronized (LOCK) {

                initialiseDirectory();

                String[] components = inputURI.split("[:;]");
                for (String component : components) {
                    if (component.contains(DIRECTORY_TEMPLATE)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Component of URL: " + component);
                            LOGGER.debug("temporaryDirectoryName: " + temporaryDirectoryName);
                        }
                        String prefix = component.substring(0, component.indexOf(DIRECTORY_TEMPLATE));
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("prefix: " + prefix);
                        }
                        File temporaryDirectory = new File(prefix, temporaryDirectoryName);

                        if (!createdDirectories.contains(temporaryDirectory.getPath())) {
                            if (!temporaryDirectory.exists()) {
                                if (!temporaryDirectory.mkdirs()) {
                                    // Temporary directory is not writeable?
                                    throw new IllegalStateException(
                                            "Directory " + temporaryDirectory + " could not be created while configuring " + inputURI);
                                } else {
                                    createdDirectories.add(temporaryDirectory.getPath());
                                }
                            } else {
                                // The directory already exists (probably from a previous run of I5).
                                createdDirectories.add(temporaryDirectory.getPath());
                            }
                        }
                    }
                }
            }

            final String modifiedInputURI = inputURI.replace(TemporaryDirectoryManager.DIRECTORY_TEMPLATE, temporaryDirectoryName);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("modifiedInputURI: " + modifiedInputURI);
            }
            return modifiedInputURI;
        }
        return inputURI;
    }

    /**
     * Returns the directory name that will replace [UNIQUE].
     *
     * @return the directory name that will replace [UNIQUE].
     */
    @Override
    public String getReplacement() {
        synchronized (LOCK) {
            initialiseDirectory();
        }
        return temporaryDirectoryName;
    }

    /**
     * This method checks if the temporaryDirectory path has been determined.  If not,
     * it creates a new unique directory path and creates this directory.
     */
    private void initialiseDirectory() {
        if (temporaryDirectoryName != null) return;

        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "localhost";
        }
        StringBuilder directoryNameStart = new StringBuilder(hostName);
        directoryNameStart.append('_');
        directoryNameStart.append(DATE_FORMAT.format(new Date()));
        directoryNameStart.append('_');
        directoryNameStart.append(Long.toString(Math.abs(System.nanoTime()) % 1000000, 36));
        temporaryDirectoryName = directoryNameStart.toString();
    }
}
