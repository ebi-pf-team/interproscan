package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: May 25, 2010
 * Time: 3:04:26 PM
 * To change this template use File | Settings | File Templates.
 */

abstract public class RunBinaryStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(RunBinaryStep.class);

    private String outputFileNameTemplate;

    public String getOutputFileNameTemplate() {
        return outputFileNameTemplate;
    }

    @Required
    public void setOutputFileNameTemplate(String hmmerOutputFilePathTemplate) {
        this.outputFileNameTemplate = hmmerOutputFilePathTemplate;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        try{
        LOGGER.debug("About to run binary... some output should follow.");
        Thread.sleep(5000);
        final String outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());
        List<String> command = createCommand(stepInstance, temporaryFileDirectory);

        CommandLineConversation clc = new CommandLineConversationImpl();
        clc.setOutputPathToFile( outputFileName, true, false);
        int exitStatus = clc.runCommand(false, command);
        if (exitStatus == 0){
            LOGGER.debug("binary finished successfully!");
        }
        else {
            StringBuffer failureMessage = new StringBuffer();
            failureMessage.append ("Command line failed with exit code: ")
                    .append (exitStatus)
                    .append ("\nCommand: ");
            for (String element : command){
                failureMessage.append (element).append(' ');
            }
            failureMessage.append ("\nError output from binary:\n");
            failureMessage.append (clc.getErrorMessage());
            LOGGER.error(failureMessage);
            // TODO Look for a more specific Exception to throw here...
            throw new IllegalStateException (failureMessage.toString());
        }
        } catch (IOException e) {
            throw new IllegalStateException ("IOException thrown when attempting to run binary", e);
        } catch (InterruptedException e) {
            throw new IllegalStateException ("InterruptedException thrown when attempting to run binary", e);
        }
    }

    /**
     *
     * Implementations of this method should return a List<String> containing all the components of the command line to be called
     * including any arguments. The StepInstance and temporary file are provided to allow parameters to be built. Use
     * stepInstance.buildFullyQualifiedFilePath to assist building paths.
     *
     * @param stepInstance containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return elements of the command in a list.
     */

    protected abstract List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory);

}
