package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.io.match.MatchAndSiteParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawProteinSite;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawSiteDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Parses and persists the output from binary.
 *
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 */
public abstract class CompositeParseStep<T extends RawMatch,  U extends RawSite> extends Step {

    private static final Logger LOGGER = Logger.getLogger(CompositeParseStep.class.getName());

    private String outputFileTemplate;
    private MatchAndSiteParser<T, U> parser;
    private RawMatchDAO<T> rawMatchDAO;
    private RawSiteDAO<U> rawSiteDAO;

    public MatchAndSiteParser<T, U> getParser() {
        return parser;
    }



    @Required
    public void setParser(MatchAndSiteParser<T, U> parser) {
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

    @Required
    public void setRawSiteDAO(RawSiteDAO<U> rawSiteDAO) {
        this.rawSiteDAO = rawSiteDAO;
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
            MatchSiteData<T, U> matchSiteData = getParser().parseMatchesAndSites(is);
            final Set<RawProtein<T>> results = matchSiteData.getRawProteins();
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
                    Utilities.verboseLog("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog("ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
                    + " time taken: " + timeTaken);
            }

            //deal with sites if any
            final Map<String, String> parameters = stepInstance.getParameters();
            final boolean excludeSites = Boolean.TRUE.toString().equals(parameters.get(StepInstanceCreatingStep.EXCLUDE_SITES));
            if (!excludeSites) {
                final Set<RawProteinSite<U>> rawProteinSites = matchSiteData.getRawProteinSites();
                Utilities.verboseLog("Parsed out " + rawProteinSites.size() + " proteins with sites from file " + fileName);
                RawSite represantiveRawSite = null;
                count = 0;
                if (rawProteinSites.size() > 0) {
                    for (RawProteinSite<U> rawProteinSite : rawProteinSites) {
                        count += rawProteinSite.getSites().size();
                        if (represantiveRawSite == null) {
                            if (rawProteinSite.getSites().size() > 0) {
                                represantiveRawSite = rawProteinSite.getSites().iterator().next();
                            }
                        }
                    }
                }
                Utilities.verboseLog("A total of " + count + " residue sites from file " + fileName);

                rawSiteDAO.insertSites(rawProteinSites);
            }
            now = System.currentTimeMillis();


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
