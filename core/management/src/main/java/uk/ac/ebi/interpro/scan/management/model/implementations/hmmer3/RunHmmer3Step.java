package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;

import java.io.Serializable;
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
public class RunHmmer3Step extends Step implements Serializable {

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
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     * <p/>
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     *
     * @param daoManager    for DAO processes.
     * @param stepExecution record of execution
     */
    @Override
    public void execute(DAOManager daoManager, StepExecution stepExecution) {
        stepExecution.setToRun();
        final StepInstance stepInstance = stepExecution.getStepInstance();
        final String fastaFilePathName = stepInstance.filterFileNameProteinBounds(this.getFastaFilePathNameTemplate());
        final String hmmerOutputFileName = stepInstance.filterFileNameProteinBounds(this.getHmmerOutputFilePathTemplate());
        try{
            Thread.sleep(2000);  // Have a snooze to allow NFS to catch up.
            List<String> command = new ArrayList<String>();

            command.add(this.getFullPathToBinary());
            command.addAll(this.getBinarySwitches());
            command.add(this.getFullPathToHmmFile());
            command.add(fastaFilePathName);

            CommandLineConversation clc = new CommandLineConversationImpl();
            clc.setOutputPathToFile(hmmerOutputFileName, false, false);
            LOGGER.debug("About to run HMMER binary... some output should follow.");
            int exitStatus = clc.runCommand(false, command);
            if (exitStatus == 0){
                LOGGER.debug("HMMER analysis completed successfully for fasta file " + fastaFilePathName);
                stepExecution.completeSuccessfully();
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
                throw new Exception (failureMessage.toString());
            }
        } catch (Exception e) {
            stepExecution.fail();
            e.printStackTrace();
            LOGGER.error ("Exception thrown during RunHmmer3Step.", e);
        }
    }
}
