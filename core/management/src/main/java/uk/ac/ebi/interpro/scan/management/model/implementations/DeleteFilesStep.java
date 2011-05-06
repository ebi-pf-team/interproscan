package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Simply deletes a File located at the path provided.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class DeleteFilesStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(DeleteFilesStep.class.getName());

    /**
     * Deletes the specified file directory recursively.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        try {
            FileUtils.deleteDirectory(new File(temporaryFileDirectory));
        } catch (IOException e) {
            LOGGER.warn("Could not delete the following directory: " + temporaryFileDirectory, e);
        }
    }
}
