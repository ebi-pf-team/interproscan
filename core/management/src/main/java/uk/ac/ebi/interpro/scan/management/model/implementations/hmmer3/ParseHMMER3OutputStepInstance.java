package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:37:24
 */
public class ParseHMMER3OutputStepInstance extends StepInstance<ParseHMMER3OutputStep, ParseHMMER3OutputStepExecution> implements Serializable {

    private String hmmerOutputFilePath;

    public String getHmmerOutputFilePath() {
        return hmmerOutputFilePath;
    }

    public ParseHMMER3OutputStepInstance(UUID id, ParseHMMER3OutputStep step, long bottomProteinId, long topProteinId) {
        super(id, step, bottomProteinId, topProteinId);
        this.hmmerOutputFilePath = this.filterFileNameProteinBounds(this.getStep().getHmmerOutputFilePathTemplate(), bottomProteinId, topProteinId);
    }

    @Override
    public ParseHMMER3OutputStepExecution createStepExecution() {
        return new ParseHMMER3OutputStepExecution(UUID.randomUUID(), this);
    }
}
