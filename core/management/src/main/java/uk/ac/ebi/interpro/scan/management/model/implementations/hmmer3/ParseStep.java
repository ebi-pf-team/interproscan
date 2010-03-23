package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.Hmmer3SearchMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parses and persists the output from binary.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
abstract class ParseStep<T extends RawMatch> extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseStep.class);

    private String outputFileTemplate;
    private Hmmer3SearchMatchParser<T> parser;
    private RawMatchDAO<T> rawMatchDAO;

    public Hmmer3SearchMatchParser<T> getParser() {
        return parser;
    }

    public void setParser(Hmmer3SearchMatchParser<T> parser) {
        this.parser = parser;
    }

    public String getOutputFileTemplate() {
        return outputFileTemplate;
    }

    @Required
    public void setOutputFileTemplate(String template) {
        this.outputFileTemplate = template;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<T> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Override public void execute(StepInstance stepInstance, String temporaryFileDirectory){
        if (LOGGER.isDebugEnabled())    {
            LOGGER.debug("Running ParseStep for proteins " + stepInstance.getBottomProtein() +
                    " to " + stepInstance.getTopProtein());
        }
        InputStream is = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, getOutputFileTemplate());
        try{
            is = new FileInputStream(fileName);
            final Set<RawProtein<T>> results = getParser().parse(is);
            rawMatchDAO.insertProteinMatches(results);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + fileName, e);
        } finally {
            try {
                if (is != null){
                    is.close();
                }
            }
            catch (IOException e) {
                LOGGER.warn("Error closing input stream", e);
            }
        }
    }
}
