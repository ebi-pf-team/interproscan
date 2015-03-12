package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer3;

import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Read in PIRSF raw matches, perform any post processing required and persist filtered matches.
 */
public class PirsfPostProcessor implements Serializable {

    public Map<String, RawProtein<PirsfHmmer3RawMatch>> process(Map<String, RawProtein<PirsfHmmer3RawMatch>> proteinIdToRawMatchMap) throws IOException {
        // Currently no post processing, just copy the required information from the PIRSF Hmmer3 raw match table to
        // the filtered match table!
        // TODO Review this - possible post processing required in the future?
        return proteinIdToRawMatchMap;
    }

}
