package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatFileParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * PIRSF post-processing -> also requires a pirsf.dat file.
 *
 * Algorithm:
 *
 */
public class PirsfPostProcessing implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PirsfPostProcessing.class.getName());

    private PirsfDatFileParser pirsfDatFileParser;

    private Resource pirsfDatFileResource;

    private Map<String, PirsfDatRecord> pirsfDatRecordMap;

    private Map<String, RawProtein<PIRSFHmmer2RawMatch>> allFilteredMatches = new HashMap<String, RawProtein<PIRSFHmmer2RawMatch>>();


    @Required
    public void setPirsfDatFileParser(PirsfDatFileParser pirsfDatFileParser) {
        this.pirsfDatFileParser = pirsfDatFileParser;
    }

    @Required
    public void setPirsfDatFileResource(Resource pirsfDatFileResource) {
        this.pirsfDatFileResource = pirsfDatFileResource;
    }

    /**
     * Perform post processing.
     *
     * @param rawMatchesSet Raw matches to post process.
     * @return Filtered matches
     * @throws IOException If pirsf.dat file could not be read
     */
    public Map<String, RawProtein<PIRSFHmmer2RawMatch>> process(Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatchesSet) throws IOException {

        // Read in extra information from pirsf.dat file
        pirsfDatRecordMap = pirsfDatFileParser.parse(pirsfDatFileResource);

        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (RawProtein<PIRSFHmmer2RawMatch> rawMatch : rawMatchesSet) {
            processProtein(rawMatch, allFilteredMatches);
        }

        return allFilteredMatches;
    }

    private void processProtein(RawProtein<PIRSFHmmer2RawMatch> matchRawProtein,
                                Map<String, RawProtein<PIRSFHmmer2RawMatch>> filteredMatches) {

        for (PIRSFHmmer2RawMatch pirsfRawMatch : matchRawProtein.getMatches()) {
            String id = pirsfRawMatch.getSequenceIdentifier();
            RawProtein<PIRSFHmmer2RawMatch> p;
            if (filteredMatches.containsKey(id)) {
                p = filteredMatches.get(id);
            } else {
                p = new RawProtein<PIRSFHmmer2RawMatch>(id);
                filteredMatches.put(id, p);
            }
            p.addMatch(pirsfRawMatch);
        }

    }

}
