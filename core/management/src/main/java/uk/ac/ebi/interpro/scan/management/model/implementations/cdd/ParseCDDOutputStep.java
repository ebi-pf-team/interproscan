package uk.ac.ebi.interpro.scan.management.model.implementations.cdd;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.io.match.cdd.CDDMatchParser;
import uk.ac.ebi.interpro.scan.io.match.cdd.ParseCDDMatch;
import uk.ac.ebi.interpro.scan.model.CDDMatch;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.CDDFilteredMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parses the output of CDD and stores the results to the database
 * (as filtered results - there is currently no second filtering step.)
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class ParseCDDOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseCDDOutputStep.class.getName());

    private String cddOutputFileNameTemplate;

    private CDDMatchParser parser;

    private CDDFilteredMatchDAO matchDAO;

    @Required
    public void setCddOutputFileNameTemplate(String cddOutputFileNameTemplate) {
        this.cddOutputFileNameTemplate = cddOutputFileNameTemplate;
    }

    @Required
    public void setParser(CDDMatchParser parser) {
        this.parser = parser;
    }

    @Required
    public void setMatchDAO(CDDFilteredMatchDAO matchDAO) {
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
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, cddOutputFileNameTemplate);
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
            Set<RawProtein<CDDRawMatch>> rawProteins = parser.parse(is, fileName);
            //Set<CDDRawMatch> matches = parser.parse(is, fileName);
            matchDAO.persist(rawProteins);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse CDD file " + fileName, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the CDD output file located at " + fileName, e);
                    throw new IllegalStateException("IOException thrown when attempting to close the InputStream from the CDD output file.", e);
                }
            }
        }

    }
}
