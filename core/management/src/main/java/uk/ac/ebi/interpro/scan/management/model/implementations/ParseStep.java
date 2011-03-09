package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parses and persists the output from binary.
 *
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 */
public abstract class ParseStep<T extends RawMatch> extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseStep.class.getName());

    private String outputFileTemplate;
    private MatchParser<T> parser;
    private RawMatchDAO<T> rawMatchDAO;

    public MatchParser<T> getParser() {
        return parser;
    }

    @Required
    public void setParser(MatchParser<T> parser) {
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

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        delayForNfs();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Running ParseStep for proteins " + stepInstance.getBottomProtein() +
                    " to " + stepInstance.getTopProtein());
        }
        InputStream is = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, getOutputFileTemplate());
        try {
            is = new FileInputStream(fileName);
            final Set<RawProtein<T>> results = getParser().parse(is);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + results.size() + " proteins with matches from file " + fileName);
                int count = 0;
                for (RawProtein<T> rawProtein : results) {
                    count += rawProtein.getMatches().size();
                }
                LOGGER.debug("A total of " + count + " matches from file " + fileName);
            }
            rawMatchDAO.insertProteinMatches(results);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + fileName, e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Error closing input stream", e);
            }
        }
    }
}
