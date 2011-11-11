package uk.ac.ebi.interpro.scan.io;

import org.apache.log4j.Logger;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class InstallerTemporaryDirectoryManagerImpl implements TemporaryDirectoryManager {

    private static final Logger LOGGER = Logger.getLogger(InstallerTemporaryDirectoryManagerImpl.class.getName());

    private static final String TEMPLATE_DIRECTORY_NAME = "template";

    /**
     * This method replaces (if found) the sub-string [UNIQUE] in any input String
     * with the value template.
     *
     * @param inputURI being the path that may contain [UNIQUE] to filter.
     * @return the input String with [UNIQUE] replaced.
     */
    @Override
    public String replacePath(String inputURI) {
        if (inputURI.contains(TemporaryDirectoryManager.DIRECTORY_TEMPLATE)) {
            return inputURI.replace(TemporaryDirectoryManager.DIRECTORY_TEMPLATE, TEMPLATE_DIRECTORY_NAME);
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
        return TEMPLATE_DIRECTORY_NAME;
    }
}
