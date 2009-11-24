package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.business.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.business.cli.CommandLineConversationImpl;

import java.io.Serializable;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:33:36
 */
public class RunHmmer3StepExecution extends StepExecution<RunHmmer3StepInstance> implements Serializable {

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
            this.setToRun();
            try{
                List<String> command = new ArrayList<String>();
                command.add(this.getStepInstance().getStep().getFullPathToBinary());
                command.addAll(this.getStepInstance().getStep().getBinarySwitches());
                command.add(this.getStepInstance().getStep().getFullPathToHmmFile());
                command.add(this.getStepInstance().getFastaFilePathName());

                CommandLineConversation clc = new CommandLineConversationImpl();
                clc.setOutputPathToFile(stepInstance.getHmmerOutputFileName(), false, false);
                System.out.println("About to run HMMER binary... some output should follow.");
                int exitStatus = clc.runCommand(false, command);
                if (exitStatus == 0){
                    System.out.println("HMMER analysis completed successfully for fasta file " + this.getStepInstance().getFastaFilePathName());
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
                    System.out.println(failureMessage);
                    throw new Exception (failureMessage.toString());
                }
            } catch (Exception e) {
                this.fail();
                e.printStackTrace();
                LOGGER.error ("Exception thrown during RunHmmer3Step.", e);
            }
        }
    }
