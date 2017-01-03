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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            int matchCount = 0;
            Collection<T> firstMatchList = new ArrayList();
            for (RawProtein<T> rawProtein : results) {
                matchCount += rawProtein.getMatches().size();
                if (represantiveRawMatch == null) {
                    if (rawProtein.getMatches().size() > 0) {
                        represantiveRawMatch = rawProtein.getMatches().iterator().next();
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    if (represantiveRawMatch.getSignatureLibrary().getName().contains("PANTHER") ||
                            represantiveRawMatch.getSignatureLibrary().getName().contains("ProSitePatterns")) {
                        int rmCount = 0;
                        for (RawMatch rawMatch : rawProtein.getMatches()) {
                            rmCount++;
                            Utilities.verboseLog(represantiveRawMatch.getSignatureLibrary().getName() + " " + rmCount + " "
                                    + rawMatch.toString());
                        }
                    }
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + results.size() + " proteins with matches from file " + fileName);
                LOGGER.debug("A total of " + matchCount + " matches from file " + fileName);
            }
            rawMatchDAO.insertProteinMatches(results);
            Long now = System.currentTimeMillis();
            long chunkSize =  stepInstance.getTopProtein() - stepInstance.getBottomProtein();

            if (matchCount > 0){
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(matchCount).intValue();
                if (represantiveRawMatch != null) {
                    String signatureLibraryRelease = represantiveRawMatch.getSignatureLibraryRelease();
                    Long queryStartTime = System.currentTimeMillis();
                    Set<RawProtein<T>> rawProteins = rawMatchDAO.getProteinsByIdRange(stepInstance.getBottomProtein(),
                            stepInstance.getTopProtein(), signatureLibraryRelease);
                    Long queryTime = System.currentTimeMillis() - queryStartTime;

                    for (RawProtein<T> rawProtein : rawProteins) {
                        matchesFound += rawProtein.getMatches().size();
                        if (LOGGER.isDebugEnabled()) {
                            Utilities.verboseLog("Considering ... " + represantiveRawMatch.getSignatureLibrary().getName());
                            if (represantiveRawMatch.getSignatureLibrary().getName().contains("PANTHER") ||
                                    represantiveRawMatch.getSignatureLibrary().getName().contains("ProSitePatterns")) {
                                int rmCount = 0;
                                for (RawMatch rawMatch : rawProtein.getMatches()) {
                                    rmCount++;
                                    Utilities.verboseLog(represantiveRawMatch.getSignatureLibrary().getName() + "_R2 " + rmCount + " "
                                            + rawMatch.toString());
                                }
                            }
                        }
                    }

                    Utilities.verboseLog("represantiveRawMatch :" + represantiveRawMatch.toString());
                    Utilities.verboseLog(represantiveRawMatch.getSignatureLibrary().getName() + " matchesFound: " + matchesFound + " matchCount: " + matchCount
                            + " queryTime: " + queryTime);
                    int retryCount = 0;
                    Long allowedWaitTime = Long.valueOf(waitTimeFactor) * waitTimeFactor * 100 * 1000;
                    while (matchesFound < matchCount) {
                        retryCount ++;
                        Utilities.sleep(waitTimeFactor * 1000);
                        List<T> rawMatchesInDb = rawMatchDAO.getActualRawMatchesForProteinIdsInRange(stepInstance.getBottomProtein(),
                                stepInstance.getTopProtein(), signatureLibraryRelease);

                        matchesFound = rawMatchesInDb.size();
                        Utilities.verboseLog(represantiveRawMatch.getSignatureLibrary().getName() + " matchesFound-2: " +matchesFound + " matchCount: " + matchCount);

                        if (matchesFound < matchCount) {
                            int matchCountDifference = matchCount - matchesFound;


                            if (retryCount == 1) {
                                LOGGER.warn("Raw matches may not yet committed - sleep for" + waitTimeFactor + " seconds , count: " + matchCount);
                            }
                            Long timeTaken = System.currentTimeMillis() - now;
                            //we try three times then break
                            if (matchCountDifference < 2 || chunkSize < 100 || timeTaken > allowedWaitTime || retryCount > 3) {
                                //just break as something else might be happening, need to investigate
                                String matchPersistWarning = "Possible database problem: failed to " + retryCount + "x verify " + matchCount + " matches in database for "
                                        + represantiveRawMatch.getSignatureLibrary().getName()
                                        + " after " + timeTaken + " ms "
                                        + " - matches found : " + matchesFound;
                                LOGGER.warn(matchPersistWarning);
                                Utilities.verboseLog(matchPersistWarning);
                                break;
                            }
                        }
                    }
                }else{
                    String matchPersistWarning = "Check if Raw matches committed " + matchCount + " repm: " + represantiveRawMatch;
                    LOGGER.warn(matchPersistWarning);
                    Utilities.verboseLog(matchPersistWarning);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog("ParseStep: count: " + matchCount + " represantiveRawMatch : " + represantiveRawMatch.toString()
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
        LOGGER.info("Step with Id " + this.getId() + " finished.");
    }
}
