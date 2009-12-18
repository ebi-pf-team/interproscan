package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Column;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:37:24
 */
@Entity
@DiscriminatorValue("parse_hmmer3")
public class ParseHMMER3OutputStepInstance extends StepInstance<ParseHMMER3OutputStep, ParseHMMER3OutputStepExecution> implements Serializable {

    @Column (nullable=true)
    private String hmmerOutputFilePath;

    /**
     * DO NOT USE - For JPA only.
     */
    protected ParseHMMER3OutputStepInstance() {
    }

    public String getHmmerOutputFilePath() {
        return hmmerOutputFilePath;
    }

    public ParseHMMER3OutputStepInstance(ParseHMMER3OutputStep step, long bottomProteinId, long topProteinId) {
        super(step, bottomProteinId, topProteinId);
        this.hmmerOutputFilePath = this.filterFileNameProteinBounds(this.getStep().getHmmerOutputFilePathTemplate(), bottomProteinId, topProteinId);
    }

    @Override
    public ParseHMMER3OutputStepExecution createStepExecution() {
        return new ParseHMMER3OutputStepExecution(this);
    }
}
