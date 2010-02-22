package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class runs HMMER 3 and reports any errors
 * spat out if the exit status != 0.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunHmmer3Step extends Step {

    private static final Logger LOGGER = Logger.getLogger(RunHmmer3Step.class);

    private String fullPathToBinary;

    private String fullPathToHmmFile;

    private List<String> binarySwitches;

    private String hmmerOutputFilePathTemplate;

    private String fastaFilePathNameTemplate;

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public List<String> getBinarySwitches() {
        return binarySwitches;
    }

    @Required
    public void setBinarySwitches(List<String> binarySwitches) {
        this.binarySwitches = binarySwitches;
    }

    public String getFullPathToHmmFile() {
        return fullPathToHmmFile;
    }

    @Required
    public void setFullPathToHmmFile(String fullPathToHmmFile) {
        this.fullPathToHmmFile = fullPathToHmmFile;
    }

    public String getHmmerOutputFilePathTemplate() {
        return hmmerOutputFilePathTemplate;
    }

    @Required
    public void setHmmerOutputFilePathTemplate(String hmmerOutputFilePathTemplate) {
        this.hmmerOutputFilePathTemplate = hmmerOutputFilePathTemplate;
    }

    public String getFastaFilePathNameTemplate() {
        return fastaFilePathNameTemplate;
    }

    @Required
    public void setFastaFilePathNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFilePathNameTemplate = fastaFilePathNameTemplate;
    }


    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance containing the parameters for executing.
     */
    @Override
    public void execute(StepInstance stepInstance) throws Exception {
        LOGGER.debug("About to run HMMER binary... some output should follow.");

        final String fastaFilePathName = stepInstance.filterFileNameProteinBounds(this.getFastaFilePathNameTemplate());
        final String hmmerOutputFileName = stepInstance.filterFileNameProteinBounds(this.getHmmerOutputFilePathTemplate());
        List<String> command = new ArrayList<String>();

        command.add(this.getFullPathToBinary());
        command.addAll(this.getBinarySwitches());
        command.add(this.getFullPathToHmmFile());
        command.add(fastaFilePathName);

        CommandLineConversation clc = new CommandLineConversationImpl();
        clc.setWorkingDirectory(hmmerOutputFileName.substring(0, hmmerOutputFileName.lastIndexOf('/')));
        clc.setOutputPathToFile(hmmerOutputFileName, false, false);
        int exitStatus = clc.runCommand(false, command);
        if (exitStatus == 0){
            LOGGER.debug("hmmscan completed successfully!");
            // Delete the fasta file - no longer required
            // TODO - need to make sure that this fasta file is not being used by any other process.
            File file = new File(fastaFilePathName);

            if (! file.delete()){
                LOGGER.warn("Unable to delete fasta file "+ fastaFilePathName + " after running Hmmer.");
            }
            else if (LOGGER.isDebugEnabled()){
                LOGGER.debug("fasta file "+ fastaFilePathName + " deleted.");
            }
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
