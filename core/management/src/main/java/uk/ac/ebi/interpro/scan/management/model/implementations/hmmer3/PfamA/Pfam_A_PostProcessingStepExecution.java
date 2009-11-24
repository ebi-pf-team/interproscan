package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:40:12
 */
public class Pfam_A_PostProcessingStepExecution extends StepExecution<Pfam_A_PostProcessingStepInstance> implements Serializable {

        private Map<String, List<PfamHmmer3RawMatch>> proteinAcToFilteredMatchMap;


        /**
         * Constructor that accepts a UUID as a unique identifier
         * for the StepInstance and a reference to the Step
         * to allow access to Step configuration.
         *
         * @param id           being a unique ID (UUID) object.
         * @param stepInstance the StepInstance used to create this StepExecution.
         */
        protected Pfam_A_PostProcessingStepExecution(UUID id, Pfam_A_PostProcessingStepInstance stepInstance) {
            super(id, stepInstance);
        }

        public Map<String, List<PfamHmmer3RawMatch>> getProteinAcToFilteredMatchMap() {
            return proteinAcToFilteredMatchMap;
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
                proteinAcToFilteredMatchMap = this.getStepInstance().getStep().getPostProcessor().process(this.getStepInstance().getProteinAcToRawMatchMap());
                this.completeSuccessfully();
            } catch (Exception e) {
                this.fail();
                // TODO - Complete explanation.
                LOGGER.error ("Explanation..." , e);
            }
        }
    }
