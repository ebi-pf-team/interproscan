package uk.ac.ebi.interpro.scan.business.postprocessing;

import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Gift Nuka
 *
 */
public interface PostProcessor<T extends RawMatch> extends Serializable {
    /**
     * Performs post processing for RPSBlast based member databases  (e.g. CDD).
     * Very simple filter - if the hittype Specific  is included in the List of
     * acceptable levels (passed in be the Bean setter) then the match passes.
     *
     * @param proteinIdToRawMatchMap being the Collection of unfiltered matches.
     * @return a Map of filtered matches
     */
    Map<String, RawProtein<T>> process(Map<String, RawProtein<T>> proteinIdToRawMatchMap);

}
