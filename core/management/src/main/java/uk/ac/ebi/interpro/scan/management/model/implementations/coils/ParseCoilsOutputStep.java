package uk.ac.ebi.interpro.scan.management.model.implementations.coils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.coils.CoilsMatchParser;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.CoilsFilteredMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

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
        ParseCoilsMatch represantiveRawMatch = null;
        int count = 0;
        try {
            is = new FileInputStream(fileName);
            Set<ParseCoilsMatch> matches = parser.parse(is, fileName);
            for (ParseCoilsMatch parseCoilsMatch : matches) {
                count += 1;
                if (represantiveRawMatch == null) {
                        represantiveRawMatch = parseCoilsMatch;
                }
            }

            matchDAO.persist(matches);
            //TODO refactor this
            Long now = System.currentTimeMillis();
            if (count > 0){
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog("represantiveRawMatch :" + represantiveRawMatch.toString());
                    Utilities.sleep(waitTimeFactor * 1000);
                    //ignore the usual check until refactoring of the parse step
                }else{
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                    Utilities.verboseLog("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog("ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
                        + " time taken: " + timeTaken);
            }
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
