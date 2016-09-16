package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import uk.ac.ebi.interpro.scan.util.Utilities;


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
    private boolean useSingleSequenceMode;

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

    public boolean isUseSingleSequenceMode() {
        return useSingleSequenceMode;
    }

    public void setUseSingleSequenceMode(boolean useSingleSequenceMode) {
        this.useSingleSequenceMode = useSingleSequenceMode;
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
        LOGGER.debug("Output fileName: " + fileName);
        try {
            is = new FileInputStream(fileName);
            final Set<RawProtein<T>> results = getParser().parse(is);
            RawMatch represantiveRawMatch = null;
            int count = 0;
            for (RawProtein<T> rawProtein : results) {
                count += rawProtein.getMatches().size();
                if (represantiveRawMatch == null) {
                    if (rawProtein.getMatches().size() > 0) {
                        represantiveRawMatch = rawProtein.getMatches().iterator().next();
                    }
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + results.size() + " proteins with matches from file " + fileName);
                LOGGER.debug("A total of " + count + " matches from file " + fileName);
            }
            rawMatchDAO.insertProteinMatches(results);
            Long now = System.currentTimeMillis();
            if (count > 0){
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog("represantiveRawMatch :" + represantiveRawMatch.toString());
                    String signatureLibraryRelease = represantiveRawMatch.getSignatureLibraryRelease();
                    while (matchesFound < count) {
                        Utilities.sleep(waitTimeFactor * 1000);
                        matchesFound = rawMatchDAO.getActualRawMatchesForProteinIdsInRange(stepInstance.getBottomProtein(),
                                stepInstance.getTopProtein(), signatureLibraryRelease).size();
                        if (matchesFound < count) {
                            LOGGER.warn("Raw matches not yet committed - sleep for 5 seconds , count: " + count);
                            Utilities.verboseLog("Raw matches not yet committed - sleep for "
                                    + waitTimeFactor + " seconds, matches found: " + matchesFound
                                    + " matchesCount expected: " + count);
                        }
                        Long timeTaken = System.currentTimeMillis() - now;
                        if(timeTaken > (waitTimeFactor * waitTimeFactor * 100 * 1000) && matchesFound < count){
                            LOGGER.warn("H2 database problem: failed to verify " + count + " matches in database for "
                                    + represantiveRawMatch.getSignatureLibrary().getName()
                                    + " after " + timeTaken + " ms "
                                    + " - matches found : " + matchesFound);
                            break;
                        }
                        //TODO remove this break statement after SFLD is completed implemented
//                        LOGGER.warn("For test purposes: rememeber to remove the break statement ");
//                        break;

                    }
                }else{
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                    Utilities.verboseLog("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog("ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
                    + " time taken: " + timeTaken);
            }
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
