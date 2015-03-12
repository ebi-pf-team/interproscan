package uk.ac.ebi.interpro.scan.io;

/**
 * Manager that creates / filters path names to allow temporary directories
 * to be created for concurrent use of the standalone I5.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface TemporaryDirectoryManager {

    static final String DIRECTORY_TEMPLATE = "[UNIQUE]";

    /**
     * This method replaces (if found) the sub-string [UNIQUE] in any input String
     * with the value determined by the implementation of this interface.
     *
     * @param inputURI being the URI that may contain [UNIQUE] to filter.
     * @return the input String (if it does not contain [UNIQUE]) or a filtered String
     *         with [UNIQUE] replaced by ... er... whatever the implementation chooses.
     */
    String replacePath(String inputURI);

    /**
     * Returns the directory name that will replace [UNIQUE].
     *
     * @return the directory name that will replace [UNIQUE].
     */
    String getReplacement();
}
