package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.File;
import java.io.IOException;

/**
 * Deletes a directory located at the path provided (and any files and sub-directories contained within).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */
public class DeleteDirectoryStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(DeleteDirectoryStep.class.getName());

    private boolean deleteDirectory;

    @Required
    public void setDeleteDirectory(boolean deleteDirectory) {
        this.deleteDirectory = deleteDirectory;
    }

    /**
     * Deletes the specified file directory recursively.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           Containing the parameters for executing.
     * @param temporaryFileDirectory Directory to delete.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        if (deleteDirectory) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting the following directory recursively: " + temporaryFileDirectory);
            }
            try {
                File file = new File(temporaryFileDirectory);
                int numFiles = file.listFiles().length;
                if (numFiles > 0) {
                    LOGGER.warn("Directory contained " + numFiles + " files which will be deleted as well");
                }
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.warn("Could not delete the following directory: " + temporaryFileDirectory, e);
                }
            }
        }
        else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not deleting the following directory as this optional step has been disabled: " + temporaryFileDirectory);
            }
        }
    }
}
