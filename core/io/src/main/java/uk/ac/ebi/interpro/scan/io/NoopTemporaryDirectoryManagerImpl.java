package uk.ac.ebi.interpro.scan.io;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class NoopTemporaryDirectoryManagerImpl implements TemporaryDirectoryManager {
    /**
     * This method does not modify it's parameter
     *
     * @param inputURI being the location
     * @return the inputURI
     */
    @Override
    public String replacePath(String inputURI) {
        return inputURI;
    }

    /**
     * Returns the directory name that will replace [UNIQUE].
     *
     * @return the directory name that will replace [UNIQUE].
     */
    @Override
    public String getReplacement() {
        return null;
    }
}
