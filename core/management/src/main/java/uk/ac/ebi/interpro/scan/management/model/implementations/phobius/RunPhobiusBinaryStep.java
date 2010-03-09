package uk.ac.ebi.interpro.scan.management.model.implementations.phobius;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Phobius binary on the fasta file provided to the output file provided.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class RunPhobiusBinaryStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(RunPhobiusBinaryStep.class);

    private String fullPathToBinary;

    private List<String> binarySwitches;

    private String phobiusOutputFileNameTemplate;

    private String fastaFileNameTemplate;

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public void setBinarySwitches(List<String> binarySwitches) {
        this.binarySwitches = binarySwitches;
    }

    @Required
    public void setPhobiusOutputFileNameTemplate(String phobiusOutputFileNameTemplate) {
        this.phobiusOutputFileNameTemplate = phobiusOutputFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * TODO - this looks the same as the RunHmmer3Step implementation - replace with Antony's code.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     * @throws Exception could be anything thrown by the execute method.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) throws Exception {
        final String fastaFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFileNameTemplate);
        final String phobiusOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, phobiusOutputFileNameTemplate);
        final List<String> command = new ArrayList<String>();

        command.add(fullPathToBinary);
        if (binarySwitches != null){
            command.addAll(binarySwitches);
        }
        command.add(fastaFilePath);
        CommandLineConversation clc = new CommandLineConversationImpl();
        clc.setOutputPathToFile(phobiusOutputFilePath, true, false);
        clc.setWorkingDirectory(phobiusOutputFilePath.substring(0, phobiusOutputFilePath.lastIndexOf('/')));
        int exitStatus = clc.runCommand(false, command);
        if (exitStatus == 0){
            LOGGER.debug("phobius completed successfully!");
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
            throw new Exception (failureMessage.toString());
        }
    }
}
