package uk.ac.ebi.interpro.scan.management.model.implementations.smart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.HmmPfamParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.ParseStep;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

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

public class ParseSmartHmmpfamOutputStep extends ParseStep<SmartRawMatch> {

}

/*
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

*/

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */

    /*
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
            RawMatch represantiveRawMatch = null;
            int count = 0;
            for (RawProtein<SmartRawMatch> rawProtein : parsedResults) {
                count += rawProtein.getMatches().size();
                if (represantiveRawMatch == null) {
                    if (rawProtein.getMatches().size() > 0) {
                        represantiveRawMatch = rawProtein.getMatches().iterator().next();
                    }
                }
            }
            smartRawMatchDAO.insertProteinMatches(parsedResults);
            Long now = System.currentTimeMillis();
            if (count > 0){
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog("represantiveRawMatch :" + represantiveRawMatch.toString());
                    String signatureLibraryRelease = represantiveRawMatch.getSignatureLibraryRelease();
                    while (matchesFound < count) {
                        Utilities.sleep(waitTimeFactor * 1000);
                        matchesFound = smartRawMatchDAO.getActualRawMatchesForProteinIdsInRange(stepInstance.getBottomProtein(),
                                stepInstance.getTopProtein(), signatureLibraryRelease).size();
                        if (matchesFound < count) {
                            LOGGER.warn("Raw matches not yet committed - sleep for 5 seconds , count: " + count);
                            Utilities.verboseLog("Raw matches not yet committed - sleep for "
                                    + waitTimeFactor + " seconds, matches found: " + matchesFound
                                    + " matchesCount expected: " + count);
                        }
                    }
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

    */