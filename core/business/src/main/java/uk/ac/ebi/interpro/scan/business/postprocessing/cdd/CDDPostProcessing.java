package uk.ac.ebi.interpro.scan.business.postprocessing.cdd;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.PostProcessor;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the simple post-processing required for CDD member
 * database - allows filtering based on domain query Type 'Specific or Non-Specific, etc'
 *
 * @author Gift Nuka, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class CDDPostProcessing<T extends RPSBlastRawMatch> implements PostProcessor<T> {

    private static final Logger LOGGER = Logger.getLogger(CDDPostProcessing.class.getName());

    private List<RPSBlastRawMatch.HitType> hitTypes;

    @Required
    public void setHitTypes(List<RPSBlastRawMatch.HitType> hitTypes) {
        this.hitTypes = hitTypes;
    }

    /**
     * Performs post processing for RPSBlast based member databases  (e.g. CDD).
     * Very simple filter - if the hittype Specific  is included in the List of
     * acceptable levels (passed in be the Bean setter) then the match passes.
     *
     * @param proteinIdToRawMatchMap being the Collection of unfiltered matches.
     * @return a Map of filtered matches
     */
    public Map<String, RawProtein<T>> process(Map<String, RawProtein<T>> proteinIdToRawMatchMap) {
        if (hitTypes == null) {
            throw new IllegalStateException("The RPSBlastProcessing class has not been correctly initialised. " +
                    "A List of acceptable hitTypes must be passed in.");
        }
        if (hitTypes.size() == 0) {
            LOGGER.warn("The RPSBlastProcessing class has been initialised such that NO matches will pass. " +
                    "(The list of acceptable hit types is empty).");
        }
        Map<String, RawProtein<T>> filteredMatches = new HashMap<>();
        for (String candidateProteinId : proteinIdToRawMatchMap.keySet()) {
            RawProtein<T> candidateRawProtein = proteinIdToRawMatchMap.get(candidateProteinId);
            RawProtein<T> filteredProtein = new RawProtein<>(candidateRawProtein.getProteinIdentifier());
            for (T rawMatch : candidateRawProtein.getMatches()) {
                if (hitTypes.contains(rawMatch.getHitType())) {
                    filteredProtein.addMatch(rawMatch);
                }
            }
            if (filteredProtein.getMatches() != null && filteredProtein.getMatches().size() > 0) {
                filteredMatches.put(candidateProteinId, filteredProtein);
            }
        }
        return filteredMatches;
    }
}
