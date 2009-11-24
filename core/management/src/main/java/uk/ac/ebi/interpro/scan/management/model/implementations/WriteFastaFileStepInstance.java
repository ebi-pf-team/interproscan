package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.UUID;
import java.util.List;
import java.io.Serializable;

public class WriteFastaFileStepInstance extends StepInstance<WriteFastaFileStep, WriteFastaFileStepExecution> implements Serializable {

        public WriteFastaFileStepInstance(UUID id, WriteFastaFileStep step, List<Protein> proteins, long bottomProteinId, long topProteinId) {
            super(id, step);
            this.proteins = proteins;
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

        @Override
        public WriteFastaFileStepExecution createStepExecution() {
            return new WriteFastaFileStepExecution(UUID.randomUUID(), this);
        }
    }
