package uk.ac.ebi.interpro.scan.management.model.implementations.cdd;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.io.match.cdd.CDDMatchParser;

import uk.ac.ebi.interpro.scan.management.model.implementations.ParseStep;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.persistence.CDDFilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

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

/**
  TODO refactor ideally this should be simply as follows

public class ParseCDDOutputStep extends ParseStep<CDDRawMatch> {

}
 */

public class ParseCDDOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseCDDOutputStep.class.getName());

    private String cddOutputFileNameTemplate;

    private CDDMatchParser parser;

    private RawMatchDAO<CDDRawMatch> rawMatchDAO;
    private FilteredMatchDAO<CDDRawMatch, RPSBlastMatch> matchDAO;

    @Required
    public void setCddOutputFileNameTemplate(String cddOutputFileNameTemplate) {
        this.cddOutputFileNameTemplate = cddOutputFileNameTemplate;
    }

    @Required
    public void setParser(CDDMatchParser parser) {
        this.parser = parser;
    }

    @Required
    public void setMatchDAO(FilteredMatchDAO<CDDRawMatch, RPSBlastMatch> matchDAO) {
        this.matchDAO = matchDAO;
    }



//    public void setMatchDAO(CDDFilteredMatchDAO matchDAO) {
//        this.matchDAO = matchDAO;
//    }

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
        int count = 0;
        RawMatch represantiveRawMatch = null;
        try {
            is = new FileInputStream(fileName);
            Set<RawProtein<CDDRawMatch>> rawProteins = parser.parse(is, fileName);
            for (RawProtein<CDDRawMatch> rawProtein : rawProteins) {
                count += rawProtein.getMatches().size();
                if (represantiveRawMatch == null) {
                    if (rawProtein.getMatches().size() > 0) {
                        represantiveRawMatch = rawProtein.getMatches().iterator().next();
                    }
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + rawProteins.size() + " proteins with matches from file " + fileName);

                LOGGER.debug("A total of " + count + " raw matches from file " + fileName);
            }
            //Set<CDDRawMatch> matches = parser.parse(is, fileName);
            matchDAO.persist(rawProteins);

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
