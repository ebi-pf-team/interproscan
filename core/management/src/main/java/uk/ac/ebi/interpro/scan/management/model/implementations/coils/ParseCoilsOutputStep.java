package uk.ac.ebi.interpro.scan.management.model.implementations.coils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.coils.CoilsMatchParser;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.persistence.CoilsFilteredMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parses the output of Coils and stores the results to the database
 * (as filtered results - there is currently no second filtering step.)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class ParseCoilsOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseCoilsOutputStep.class.getName());

    private String coilsOutputFileNameTemplate;

    private CoilsMatchParser parser;

    private CoilsFilteredMatchDAO matchDAO;

    @Required
    public void setCoilsOutputFileNameTemplate(String coilsOutputFileNameTemplate) {
        this.coilsOutputFileNameTemplate = coilsOutputFileNameTemplate;
    }

    @Required
    public void setParser(CoilsMatchParser parser) {
        this.parser = parser;
    }

    @Required
    public void setMatchDAO(CoilsFilteredMatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory being the directory in which the raw file is being stored.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        delayForNfs();
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, coilsOutputFileNameTemplate);
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
            Set<ParseCoilsMatch> matches = parser.parse(is, fileName);
            matchDAO.persist(matches);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse Coils file " + fileName, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the Coils output file located at " + fileName, e);
                    throw new IllegalStateException("IOException thrown when attempting to close the InputStream from the Coils output file.", e);
                }
            }
        }

    }
}
