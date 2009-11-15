package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.business.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.business.cli.CommandLineConversationImpl;
import org.springframework.beans.factory.annotation.Required;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunHmmer3Step extends Step<RunHmmer3Step.RunHmmer3StepInstance, RunHmmer3Step.RunHmmer3StepExecution> {

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

    public class RunHmmer3StepInstance extends StepInstance<RunHmmer3Step, RunHmmer3StepExecution> {

        private String hmmerOutputFileName;

        private String fastaFilePathName;

        public String getHmmerOutputFileName() {
            return hmmerOutputFileName;
        }

        @Required
        public void setHmmerOutputFileName(String hmmerOutputFileName) {
            this.hmmerOutputFileName = hmmerOutputFileName;
        }

        public String getFastaFilePathName() {
            return fastaFilePathName;
        }

        @Required
        public void setFastaFilePathName(String fastaFilePathName) {
            this.fastaFilePathName = fastaFilePathName;
        }

        public RunHmmer3StepInstance(UUID id, RunHmmer3Step step) {
            super(id, step);
        }
    }

    public class RunHmmer3StepExecution extends StepExecution<RunHmmer3StepInstance> {

        protected RunHmmer3StepExecution(UUID id, RunHmmer3StepInstance stepInstance) {
            super(id, stepInstance);
        }

        /**
         * This method is called to execute the action that the StepExecution must perform.
         * This method should typically perform its activity in a try / catch / finally block
         * that sets the state of the step execution appropriately.
         * <p/>
         * Note that the implementation DOES have access to the protected stepInstance,
         * and from their to the protected Step, to allow it to access parameters for execution.
         */
        @Override
        public void execute() {
            this.running();
            try{
                List<String> command = new ArrayList<String>();
                command.add(this.getStepInstance().getStep().getFullPathToBinary());
                command.addAll(this.getStepInstance().getStep().getBinarySwitches());
                command.add(this.getStepInstance().getStep().getFullPathToHmmFile());
                command.add(this.getStepInstance().getFastaFilePathName());

                CommandLineConversation clc = new CommandLineConversationImpl();
                clc.setOutputPathToFile(stepInstance.getHmmerOutputFileName(), false, false);
                int exitStatus = clc.runCommand(false, command);
                if (exitStatus == 0){
                    this.completeSuccessfully();
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
                    throw new Exception (failureMessage.toString());
                }
            } catch (Exception e) {
                this.fail();
                LOGGER.error ("Exception thrown during RunHmmer3Step.", e);
            }
        }
    }
}
