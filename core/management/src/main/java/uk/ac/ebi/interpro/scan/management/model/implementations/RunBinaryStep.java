package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This abstract class factors out the functionality required to run a binary.
 * The only assumption made is that some output from the binary needs to be consumed
 * and is routed to the file described by 'outputFileNameTemplate'.
 * <p/>
 * It also allows binary switches to be passed in as a white-space separated String.
 * <p/>
 * Implementations just need to build the command line in the form of a List<String>.
 *
 * @author John Maslen
 * @author Phil Jones
 *         Date: May 25, 2010
 *         Time: 3:04:26 PM
 */

abstract public class RunBinaryStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(RunBinaryStep.class);

    private String outputFileNameTemplate;

    private List<String> binarySwitchesInList = Collections.emptyList();

    private String binarySwitches;

    public String getOutputFileNameTemplate() {
        return outputFileNameTemplate;
    }

    @Required
    public void setOutputFileNameTemplate(String hmmerOutputFilePathTemplate) {
        this.outputFileNameTemplate = hmmerOutputFilePathTemplate;
    }

    /**
     * Allows binary switches to be passed in as a white-space separated String
     * (for ease of configuration).
     *
     * @param binarySwitches binary switches to be passed in as a white-space separated String
     *                       (for ease of configuration).
     */
    public void setBinarySwitches(String binarySwitches) {
        this.binarySwitches = binarySwitches;
        if (binarySwitches == null) {
            this.binarySwitchesInList = Collections.emptyList();
        } else {
            this.binarySwitchesInList = Arrays.asList(binarySwitches.split("\\s+"));
        }
    }

    public String getBinarySwitches() {
        return binarySwitches;
    }

    public final List<String> getBinarySwitchesAsList() {


        return binarySwitchesInList;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        try {
            LOGGER.debug("About to run binary... some output should follow.");
            Thread.sleep(5000);
            final String outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());
            List<String> command = createCommand(stepInstance, temporaryFileDirectory);

            CommandLineConversation clc = new CommandLineConversationImpl();
            clc.setOutputPathToFile(outputFileName, true, false);
            int exitStatus = clc.runCommand(false, command);
            if (exitStatus == 0) {
                LOGGER.debug("binary finished successfully!");
            } else {
                StringBuffer failureMessage = new StringBuffer();
                failureMessage.append("Command line failed with exit code: ")
                        .append(exitStatus)
                        .append("\nCommand: ");
                for (String element : command) {
                    failureMessage.append(element).append(' ');
                }
                failureMessage.append("\nError output from binary:\n");
                failureMessage.append(clc.getErrorMessage());
                LOGGER.error(failureMessage);
                // TODO Look for a more specific Exception to throw here...
                throw new IllegalStateException(failureMessage.toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to run binary", e);
        } catch (InterruptedException e) {
            throw new IllegalStateException("InterruptedException thrown when attempting to run binary", e);
        }
    }

    /**
     * Implementations of this method should return a List<String> containing all the components of the command line to be called
     * including any arguments. The StepInstance and temporary file are provided to allow parameters to be built. Use
     * stepInstance.buildFullyQualifiedFilePath to assist building paths.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return elements of the command in a list.
     */
    protected abstract List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory);


}
