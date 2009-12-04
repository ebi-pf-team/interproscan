package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.Hmmer3SearchMatchParser;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.io.Serializable;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseHMMER3OutputStep<T extends RawMatch> extends Step<ParseHMMER3OutputStepInstance, ParseHMMER3OutputStepExecution> implements Serializable {

    private String fullPathToHmmFile;

    private String hmmerOutputFilePathTemplate;

    private Hmmer3SearchMatchParser<T> parser;

    public Hmmer3SearchMatchParser<T> getParser() {
        return parser;
    }

    public void setParser(Hmmer3SearchMatchParser<T> parser) {
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
}
