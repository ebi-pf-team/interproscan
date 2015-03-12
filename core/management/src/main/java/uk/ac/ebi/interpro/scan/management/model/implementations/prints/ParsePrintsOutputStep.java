package uk.ac.ebi.interpro.scan.management.model.implementations.prints;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Jun 11, 2010
 * Time: 1:46:05 PM
 */

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.prints.PrintsMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.PrintsRawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ParsePrintsOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParsePrintsOutputStep.class.getName());

    private String printsOutputFileNameTemplate;

    private PrintsRawMatchDAO printsMatchDAO;

    private PrintsMatchParser parser;

    private String signatureLibraryRelease;

    @Required
    public void setPrintsOutputFileNameTemplate(String printsOutputFileNameTemplate) {
        this.printsOutputFileNameTemplate = printsOutputFileNameTemplate;
    }

    @Required
    public void setPrintsRawMatchDAO(PrintsRawMatchDAO printsMatchDAO) {
        this.printsMatchDAO = printsMatchDAO;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setParser(PrintsMatchParser parser) {
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
     * @param temporaryFileDirectory
     * @throws Exception could be anything thrown by the execute method.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        delayForNfs();
        InputStream inputStreamParser = null;
        try {
            final String printsOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, printsOutputFileNameTemplate);
            inputStreamParser = new FileInputStream(printsOutputFilePath);
            final Set<RawProtein<PrintsRawMatch>> parsedResults = parser.parse(inputStreamParser, printsOutputFilePath, signatureLibraryRelease);
            printsMatchDAO.insertProteinMatches(parsedResults);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse Prints file " + printsOutputFileNameTemplate, e);
        } finally {
            if (inputStreamParser != null) {
                try {
                    inputStreamParser.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the Prints output file located at " + printsOutputFileNameTemplate, e);
                }
            }
        }
    }
}
