package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;

import java.util.List;
import java.util.UUID;
import java.io.File;

import org.springframework.beans.factory.annotation.Required;

/**
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileStep extends Step<WriteFastaFileStep.WriteFastaFileStepInstance, WriteFastaFileStep.WriteFastaFileStepExecution> {

    private String fastaFilePathTemplate;

    @Required
    public void setFastaFilePathTemplate(String fastaFilePathTemplate) {
        this.fastaFilePathTemplate = fastaFilePathTemplate;
    }

    public String getFastaFilePathTemplate() {
        return fastaFilePathTemplate;
    }

    public class WriteFastaFileStepInstance extends StepInstance<WriteFastaFileStep, WriteFastaFileStepExecution> {

        public WriteFastaFileStepInstance(UUID id, WriteFastaFileStep step, List<Protein> proteins, long bottomProteinId, long topProteinId) {
            super(id, step);
            this.proteins = proteins;
            this.setBottomProtein(bottomProteinId);
            this.setTopProtein(topProteinId);
            this.fastaFilePathName = filterFileNameProteinBounds(
                    this.getStep().getFastaFilePathTemplate(),
                    bottomProteinId, topProteinId
            );
        }

        private List<Protein> proteins;

        private String fastaFilePathName;

        public String getFastaFilePathName() {
            return fastaFilePathName;
        }

        public List<Protein> getProteins() {
            return proteins;
        }

        public WriteFastaFileStepInstance(UUID id, WriteFastaFileStep step) {
            super(id, step);
        }

        @Override
        public WriteFastaFileStepExecution createStepExecution() {
            return new WriteFastaFileStepExecution(UUID.randomUUID(), this);
        }
    }

    public static class WriteFastaFileStepExecution extends StepExecution<WriteFastaFileStepInstance> {

        private final WriteFastaFile fastaFile = new WriteFastaFile();

        protected WriteFastaFileStepExecution(UUID id, WriteFastaFileStepInstance stepInstance) {
            super(id, stepInstance);
        }

        @Override
        public void execute() {
            this.running();
            try{
                File file = new File(this.getStepInstance().getStep().getFastaFilePathTemplate());
                // Only write the file if it has not been written before.
                if (! file.exists()){
                    fastaFile.writeFastaFile(this.getStepInstance().getProteins(), this.getStepInstance().getFastaFilePathName());
                }
                this.completeSuccessfully();
            } catch (Exception e) {
                this.fail();
                LOGGER.error ("Exception thrown when attempting to write out a Fasta file to path " , e);
            }
        }
    }
}
