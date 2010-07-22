package uk.ac.ebi.interpro.scan.management.model.implementations.phobius;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.phobius.PhobiusMatchParser;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.persistence.PhobiusFilteredMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parses the output of phobius and stores the results to the database
 * (as filtered results - there is currently no second filtering step.)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class ParsePhobiusOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParsePhobiusOutputStep.class.getName());

    private String phobiusOutputFileNameTemplate;

    private PhobiusFilteredMatchDAO phobiusMatchDAO;

    private PhobiusMatchParser parser;

    @Required
    public void setPhobiusOutputFileNameTemplate(String phobiusOutputFileNameTemplate) {
        this.phobiusOutputFileNameTemplate = phobiusOutputFileNameTemplate;
    }

    @Required
    public void setPhobiusMatchDAO(PhobiusFilteredMatchDAO phobiusMatchDAO) {
        this.phobiusMatchDAO = phobiusMatchDAO;
    }

    @Required
    public void setParser(PhobiusMatchParser parser) {
        this.parser = parser;
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
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, phobiusOutputFileNameTemplate);
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
            Set<PhobiusProtein> phobiusProteins = parser.parse(is, fileName);
            phobiusMatchDAO.persist(phobiusProteins);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse Phobius file " + fileName, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the Phobius output file located at " + fileName, e);
                    throw new IllegalStateException("IOException thrown when attempting to close the InputStream from the Phobius output file.", e);
                }
            }
        }
    }
}
