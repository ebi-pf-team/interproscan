package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileStep extends Step<WriteFastaFileStep.WriteFastaFileStepInstance, WriteFastaFileStep.WriteFastaFileExecution> {

    private String fullPathToFastaFile;

    private List<Protein> proteins;

    public void setFullPathToFastaFile(String fullPathToFastaFile) {
        this.fullPathToFastaFile = fullPathToFastaFile;
    }

    public void setProteins(List<Protein> proteins) {
        this.proteins = proteins;
    }

    public class WriteFastaFileStepInstance extends StepInstance<WriteFastaFileStep, WriteFastaFileExecution> {
        public WriteFastaFileStepInstance(UUID id, WriteFastaFileStep step) {
            super(id, step);
        }
    }

    public class WriteFastaFileExecution extends StepExecution<WriteFastaFileStep, WriteFastaFileStepInstance> {

        protected WriteFastaFileExecution(UUID id, WriteFastaFileStepInstance stepInstance, StepExecutionState state) {
            super(id, stepInstance);
        }

        @Override
        public void execute() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
