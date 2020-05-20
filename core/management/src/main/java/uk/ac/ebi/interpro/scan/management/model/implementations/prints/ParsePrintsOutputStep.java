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
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.PrintsRawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

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

        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(110, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        delayForNfs();
        InputStream inputStreamParser = null;
        try {
            final String printsOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, printsOutputFileNameTemplate);
            inputStreamParser = new FileInputStream(printsOutputFilePath);
            final Set<RawProtein<PrintsRawMatch>> parsedResults = parser.parse(inputStreamParser, printsOutputFilePath, signatureLibraryRelease);

            RawMatch represantiveRawMatch = null;
            int count = 0;

            for (RawProtein<PrintsRawMatch> rawProtein : parsedResults) {
                count += rawProtein.getMatches().size();
                if (represantiveRawMatch == null) {
                    if (rawProtein.getMatches().size() > 0) {
                        represantiveRawMatch = rawProtein.getMatches().iterator().next();
                    }
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + parsedResults.size() + " proteins with matches from file " + printsOutputFilePath);

                LOGGER.debug("A total of " + count + " matches from file " + printsOutputFilePath);
            }

            printsMatchDAO.insertProteinMatches(parsedResults);
            Long now = System.currentTimeMillis();
            if (count > 0){
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog(1100, "represantiveRawMatch :" + represantiveRawMatch.toString());
                    String signatureLibraryRelease = represantiveRawMatch.getSignatureLibraryRelease();
                    while (matchesFound < count) {
                        Utilities.sleep(waitTimeFactor * 1000);
//                        for ( RawProtein<PrintsRawMatch> rawProtein :printsMatchDAO.getRawMatchesForProteinIdsInRange(stepInstance.getBottomProtein(),
//                                stepInstance.getTopProtein(), signatureLibraryRelease).values()){
//                            matchesFound += rawProtein.getMatches().size();
//                        }
                        matchesFound = printsMatchDAO.getActualRawMatchesForProteinIdsInRange(stepInstance.getBottomProtein(),
                                stepInstance.getTopProtein(), signatureLibraryRelease).size();
                        if (matchesFound < count){
                            LOGGER.warn("Raw matches not yet committed - sleep for 5 seconds , count: " + count);
                            Utilities.verboseLog(1100, "Raw matches not yet committed - sleep for "
                                    +  waitTimeFactor + " seconds, matches found: " + matchesFound
                                    + " matchesCount expected: " + count);
                        }
                        Long timeTaken = System.currentTimeMillis() - now;
                        if(timeTaken > (waitTimeFactor * waitTimeFactor * 100 * 1000)){
                            LOGGER.warn("H2 database problem: failed to verify " + count + " matches in database for "
                                    + represantiveRawMatch.getSignatureLibrary().getName()
                                    + " after " + timeTaken + " ms "
                                    + " - matches found : " + matchesFound);
                            break;
                        }
                    }
                }else{
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                    Utilities.verboseLog(1100, "Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog(1100, "ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
                        + " time taken: " + timeTaken);
            }
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
