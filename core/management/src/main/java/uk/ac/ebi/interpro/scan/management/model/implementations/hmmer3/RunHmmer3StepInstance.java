package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Required;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:33:55
 */
public class RunHmmer3StepInstance extends StepInstance<RunHmmer3Step, RunHmmer3StepExecution> implements Serializable {

        private String hmmerOutputFileName;

        private String fastaFilePathName;

        public String getHmmerOutputFileName() {
            return hmmerOutputFileName;
        }

        public String getFastaFilePathName() {
            return fastaFilePathName;
        }

        public RunHmmer3StepInstance(UUID id, RunHmmer3Step step, long bottomProteinId, long topProteinId) {
            super(id, step, bottomProteinId, topProteinId);
            this.fastaFilePathName = this.filterFileNameProteinBounds(this.getStep().getFastaFilePathNameTemplate(), bottomProteinId, topProteinId);
            this.hmmerOutputFileName = this.filterFileNameProteinBounds(this.getStep().getHmmerOutputFilePathTemplate(), bottomProteinId, topProteinId);
        }

        @Override
        public RunHmmer3StepExecution createStepExecution() {
            return new RunHmmer3StepExecution(UUID.randomUUID(), this);
        }
    }
