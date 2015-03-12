package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processes sub family matches before added them to the final result set.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SubfamilyPersistenceProcessor implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SubfamilyPersistenceProcessor.class.getName());

    private RawMatchDAO<PIRSFHmmer2RawMatch> rawMatchDAO;

    @Required
    public void setRawMatchDAO(RawMatchDAO<PIRSFHmmer2RawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    /**
     * Adds sub families to the result set.
     *
     * @throws java.io.IOException If subfamilies.out file couldn't be read.
     */
    public void process(final Set<RawProtein<PIRSFHmmer2RawMatch>> subfamRawProteins,
                        final Map<String, RawProtein<PIRSFHmmer2RawMatch>> proteinIdToProteinMap,
                        final Map<String, String> subfamToSuperFamMap) {
        LOGGER.info("Added sub families to result...");
        //Iterate over sub families and check for super families
        for (RawProtein<PIRSFHmmer2RawMatch> subfamRawProtein : subfamRawProteins) {
            if (isExpectedMatchSize(subfamRawProtein)) {
                //Get superfam matches
                final String proteinId = subfamRawProtein.getProteinIdentifier();
                RawProtein<PIRSFHmmer2RawMatch> superfamRawProtein = proteinIdToProteinMap.get(proteinId);
                if (superfamRawProtein != null) {
                    List<PIRSFHmmer2RawMatch> subfamRawMatches = new ArrayList<PIRSFHmmer2RawMatch>(subfamRawProtein.getMatches());
                    List<PIRSFHmmer2RawMatch> superfamRawMatches = new ArrayList<PIRSFHmmer2RawMatch>(superfamRawProtein.getMatches());
                    PIRSFHmmer2RawMatch bestSuperfamMatch = superfamRawMatches.get(0);
                    String bestSuperFamModelId = bestSuperfamMatch.getModelId();
                    PIRSFHmmer2RawMatch bestSubfamMatch = subfamRawMatches.get(0);
                    String parentFamModelId = subfamToSuperFamMap.get(bestSubfamMatch.getModelId());
                    //Best sub family is a child of best super family
                    if (isChildOfCheck(bestSuperFamModelId, parentFamModelId)) {
                        superfamRawProtein.addMatch(bestSubfamMatch);
                        proteinIdToProteinMap.put(proteinId, superfamRawProtein);
                    }
                    //Best sub fam isn't a child of best super fam (search in raw matches for the parent match)
                    else {
                        PIRSFHmmer2RawMatch parentFamMatch = rawMatchDAO.getMatchesByModel(parentFamModelId);
                        if (parentFamMatch != null) {
                            subfamRawProtein.addMatch(parentFamMatch);
                            proteinIdToProteinMap.put(proteinId, subfamRawProtein);
                        } else {
                            LOGGER.warn("Missing parent match for best subfamily: " + bestSubfamMatch.getModelId());
                        }
                    }
                } else {
                    LOGGER.warn("No super family matches found for protein with ID " + proteinId + "! Ironically found sub family matches.");
                }
            }
        }
    }

    /**
     * Checks if parent model Id is the same as best super family model Id.
     *
     * @param bestSuperFamModelId Model accession of the best super family match.
     * @param parentFamModelId    Model accession of the parent of best subfamily match.
     */
    private boolean isChildOfCheck(final String bestSuperFamModelId,
                                   final String parentFamModelId) {
        if (bestSuperFamModelId != null && parentFamModelId != null) {
            if (bestSuperFamModelId.equals(parentFamModelId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExpectedMatchSize(final RawProtein<PIRSFHmmer2RawMatch> rawProtein) {
        if (rawProtein.getMatches().size() != 1) {
            if (LOGGER.isEnabledFor(Level.WARN)) {
                LOGGER.warn("Unexpected matches size of " + rawProtein.getMatches().size() + " (expected size is 1) of protein with ID " + rawProtein.getProteinIdentifier() + "!");
            }
            return false;
        }
        return true;
    }
}