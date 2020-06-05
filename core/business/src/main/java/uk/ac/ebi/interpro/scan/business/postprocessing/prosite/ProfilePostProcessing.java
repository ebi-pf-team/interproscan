package uk.ac.ebi.interpro.scan.business.postprocessing.prosite;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the simple post-processing required for Prosite Profile based member
 * databases - allows filtering based upon allowed levels (of match)
 *
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class ProfilePostProcessing<T extends ProfileScanRawMatch> implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(ProfilePostProcessing.class.getName());

    private List<ProfileScanRawMatch.Level> passLevels;

    @Required
    public void setPassLevels(List<ProfileScanRawMatch.Level> passLevels) {
        this.passLevels = passLevels;
    }

    /**
     * Performs post processing for Prosite Profiles (e.g. Prosite or HAMAP).
     * Very simple filter - if the Level of the match is included in the List of
     * acceptable levels (passed in be the Bean setter) then the match passes.
     *
     * @param proteinIdToRawMatchMap being the Collection of unfiltered matches.
     * @return a Map of filtered matches
     */
    public Map<String, RawProtein<T>> process(Map<String, RawProtein<T>> proteinIdToRawMatchMap) {
        if (passLevels == null) {
            throw new IllegalStateException("The ProfilePostProcessing class has not been correctly initialised. A List of acceptable Levels must be passed in.");
        }
        if (passLevels.size() == 0) {
            LOGGER.warn("The ProfilePostProcessing class has been initialised such that NO matches will pass. (The list of acceptable levels is empty).");
        }
        Map<String, RawProtein<T>> filteredMatches = new HashMap<String, RawProtein<T>>();
        for (String candidateProteinId : proteinIdToRawMatchMap.keySet()) {
            RawProtein<T> candidateRawProtein = proteinIdToRawMatchMap.get(candidateProteinId);
            RawProtein<T> filteredProtein = new RawProtein<T>(candidateRawProtein.getProteinIdentifier());
            for (T rawMatch : candidateRawProtein.getMatches()) {
                if (passLevels.contains(rawMatch.getLevel())) {
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
