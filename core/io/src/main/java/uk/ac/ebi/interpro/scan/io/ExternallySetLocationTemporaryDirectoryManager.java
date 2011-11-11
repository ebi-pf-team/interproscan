package uk.ac.ebi.interpro.scan.io;


import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 16/09/11
 * Allows the working directory to be passed in, to allow remote workers to know where the temporary
 * directory is located.
 */
public class ExternallySetLocationTemporaryDirectoryManager implements TemporaryDirectoryManager {

    private static final Logger LOGGER = Logger.getLogger(ExternallySetLocationTemporaryDirectoryManager.class.getName());

    private String passedInDirectoryName;

    public void setPassedInDirectoryName(String passedInDirectoryName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Temporary directory name passed in: " + passedInDirectoryName);
        }
        this.passedInDirectoryName = passedInDirectoryName;
    }

    /**
     * This method replaces (if found) the sub-string [UNIQUE] in any input String
     * with the value determined by the implementation of this interface.
     *
     * @param inputURI being the URI that may contain [UNIQUE] to filter.
     * @return the input String (if it does not contain [UNIQUE]) or a filtered String
     *         with [UNIQUE] replaced by ... er... whatever the implementation chooses.
     */
    public String replacePath(String inputURI) {
        if (inputURI.contains(TemporaryDirectoryManager.DIRECTORY_TEMPLATE) && passedInDirectoryName != null && !passedInDirectoryName.isEmpty()) {
            final String mungedURI = inputURI.replace(TemporaryDirectoryManager.DIRECTORY_TEMPLATE, passedInDirectoryName);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("URI changed to " + mungedURI);
            }
            return mungedURI;
        } else {
            LOGGER.debug("Returning inputURI unchanged: " + inputURI);
            return inputURI;
        }
    }

    /**
     * Returns the directory name that will replace [UNIQUE].
     *
     * @return the directory name that will replace [UNIQUE].
     */
    @Override
    public String getReplacement() {
        return passedInDirectoryName;
    }
}
