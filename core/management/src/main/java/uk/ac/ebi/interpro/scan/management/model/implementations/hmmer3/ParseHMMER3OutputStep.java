package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3.Hmmer3SearchParser;
import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;
import org.springframework.beans.factory.annotation.Required;

import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseHMMER3OutputStep extends Step<ParseHMMER3OutputStep.ParseHMMER3OutputStepInstance, ParseHMMER3OutputStep.ParseHMMER3OutputStepExecution> {

    private String fullPathToHmmFile;

    private String hmmerOutputFilePathTemplate;

    private Hmmer3SearchParser parser;

    public Hmmer3SearchParser getParser() {
        return parser;
    }

    public void setParser(Hmmer3SearchParser parser) {
        this.parser = parser;
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



    public class ParseHMMER3OutputStepInstance extends StepInstance<ParseHMMER3OutputStep, ParseHMMER3OutputStepExecution>{

        private String hmmerOutputFilePath;

        public String getHmmerOutputFilePath() {
            return hmmerOutputFilePath;
        }

        public void setHmmerOutputFilePath(String hmmerOutputFilePath) {
            this.hmmerOutputFilePath = hmmerOutputFilePath;
        }

        public ParseHMMER3OutputStepInstance(UUID id, ParseHMMER3OutputStep step) {
            super(id, step);
        }

        @Override
        public ParseHMMER3OutputStepExecution createStepExecution() {
            return new ParseHMMER3OutputStepExecution(UUID.randomUUID(), this);
        }
    }



    
    public class ParseHMMER3OutputStepExecution extends StepExecution<ParseHMMER3OutputStepInstance> {

        private Set<RawSequenceIdentifier> parsedResults;

        public Set<RawSequenceIdentifier> getParsedResults() {
            return parsedResults;
        }

        protected ParseHMMER3OutputStepExecution(UUID id, ParseHMMER3OutputStepInstance stepInstance) {
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
            InputStream is = null;
            try{
                is = new FileInputStream(this.getStepInstance().getHmmerOutputFilePath());
                parsedResults = parser.parse(is);
                this.completeSuccessfully();
            } catch (Exception e) {
                this.fail();
                LOGGER.error ("Doh." , e);
            }
            finally {
                if (is != null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        LOGGER.error ("Duh - parsed OK, but can't close the input stream?" , e);
                    }
                }
            }
        }
    }
}
