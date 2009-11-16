package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.PfamHMMER3PostProcessing;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.util.UUID;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

/**
 * Created by IntelliJ IDEA.
 * User: mumdad
 * Date: 15-Nov-2009
 * Time: 14:48:18
 * To change this template use File | Settings | File Templates.
 */
public class Pfam_A_PostProcessingStep extends Step<Pfam_A_PostProcessingStep.Pfam_A_PostProcessingStepInstance, Pfam_A_PostProcessingStep.Pfam_A_PostProcessingStepExecution> {

    private PfamHMMER3PostProcessing postProcessor;

    public PfamHMMER3PostProcessing getPostProcessor() {
        return postProcessor;
    }

    @Required
    public void setPostProcessor(PfamHMMER3PostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }

    /**
     * The StepInstance class for Pfam_A_PostProcessingStep
     */
    public class Pfam_A_PostProcessingStepInstance extends StepInstance<Pfam_A_PostProcessingStep, Pfam_A_PostProcessingStepExecution> {

        Map<String, List<PfamHmmer3RawMatch>> proteinAcToRawMatchMap;

        public void setProteinAcToRawMatchMap(Map<String, List<PfamHmmer3RawMatch>> proteinAcToRawMatchMap) {
            this.proteinAcToRawMatchMap = proteinAcToRawMatchMap;
        }

        public Map<String, List<PfamHmmer3RawMatch>> getProteinAcToRawMatchMap() {
            return proteinAcToRawMatchMap;
        }

        /**
         * Constructor that accepts a UUID as a unique identifier
         * for the StepInstance and a reference to the Step
         * to allow access to Step configuration.
         *
         * @param id   being a unique ID (UUID) object.
         * @param step the Step template used to create this StepInstance.
         */
        public Pfam_A_PostProcessingStepInstance(UUID id, Pfam_A_PostProcessingStep step) {
            super(id, step);
        }

        @Override
        public Pfam_A_PostProcessingStepExecution createStepExecution() {
            return new Pfam_A_PostProcessingStepExecution(UUID.randomUUID(), this);
        }

    }

    /**
     * The StepExecution class for Pfam_A_PostProcessingStep
     */
    public class Pfam_A_PostProcessingStepExecution extends StepExecution<Pfam_A_PostProcessingStepInstance> {

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
            this.running();
            try{
                proteinAcToFilteredMatchMap = postProcessor.process(this.getStepInstance().getProteinAcToRawMatchMap());
                this.completeSuccessfully();
            } catch (Exception e) {
                this.fail();
                // TODO - Complete explanation.
                LOGGER.error ("Explanation..." , e);
            }
        }
    }
}