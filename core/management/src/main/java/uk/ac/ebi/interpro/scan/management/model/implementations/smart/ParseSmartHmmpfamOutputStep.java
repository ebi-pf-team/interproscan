package uk.ac.ebi.interpro.scan.management.model.implementations.smart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.HmmPfamParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * This class parses and persists the output from hmmpfam for SMART
 *
 * @author Phil Jones, adapted by John Maslen
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
// TODO: Eliminate all code by extending uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.ParseStep
public class ParseSmartHmmpfamOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseSmartHmmpfamOutputStep.class.getName());

    private String hmmerOutputFilePathTemplate;

    private HmmPfamParser<SmartRawMatch> parser;

    private RawMatchDAO<SmartRawMatch> smartRawMatchDAO;

    @Required
    public void setHmmerOutputFileNameTemplate(String hmmerOutputFilePathTemplate) {
        this.hmmerOutputFilePathTemplate = hmmerOutputFilePathTemplate;
    }

    @Required
    public void setSmartRawMatchDAO(RawMatchDAO<SmartRawMatch> smartRawMatchDAO) {
        this.smartRawMatchDAO = smartRawMatchDAO;
    }

    @Required
    public void setParser(HmmPfamParser<SmartRawMatch> parser) {
        this.parser = parser;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        delayForNfs();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Running Parser HMMER2 Output Step for proteins " + stepInstance.getBottomProtein() + " to " + stepInstance.getTopProtein());
        }
        InputStream is = null;
        try {
            final String hmmerOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, hmmerOutputFilePathTemplate);
            is = new FileInputStream(hmmerOutputFilePath);
            final Set<RawProtein<SmartRawMatch>> parsedResults = parser.parse(is);
            smartRawMatchDAO.insertProteinMatches(parsedResults);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to read " + hmmerOutputFilePathTemplate, e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Parsed OK, but can't close the input stream?", e);
                }
            }
        }
    }
}
