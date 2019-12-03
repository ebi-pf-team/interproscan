package uk.ac.ebi.interpro.scan.business.postprocessing.panther;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * PANTHER post-processing.
 *
 * @author Antony Quinn, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
public class PantherPostProcessor implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PantherPostProcessor.class.getName());

    private final double eValueCutoff;

    public PantherPostProcessor(double eValue) {
        this.eValueCutoff = eValue;
    }

    public double geteValueCutoff() {
        return eValueCutoff;
    }

    /**
     * Returns a set of filtered matches.
     *
     * @param rawProteins Raw proteins , with associated raw matches.
     * @return Filtered Panther matches.
     */
    public Set<RawProtein<PantherRawMatch>> process(Set<RawProtein<PantherRawMatch>> rawProteins) {
        LOGGER.info("Filtering PANTHER raw matches...");
        Set<RawProtein<PantherRawMatch>> filteredMatches = new HashSet<>();
        int rawMatchCounter = 0;
        int filteredMatchesCounter = 0;
        for (RawProtein<PantherRawMatch> rawProtein : rawProteins) {
            rawMatchCounter += rawProtein.getMatches().size();
            RawProtein<PantherRawMatch> filtered = processProtein(rawProtein);
            filteredMatchesCounter += filtered.getMatches().size();
            filteredMatches.add(filtered);
        }
        String filterMessage =  "Finished filtering of PANTHER raw matches. Printing out Summary... \n"
            + "Original number of raw matches: " + rawMatchCounter + "\n"
            + "Number of discarded raw matches: " + (rawMatchCounter - filteredMatchesCounter) + "\n";

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(filterMessage);
        }
        Utilities.verboseLog(filterMessage);
        return filteredMatches;
    }

    /**
     * Filter raw matches just by E-Value cutoff.
     */
    private RawProtein<PantherRawMatch> processProtein(final RawProtein<PantherRawMatch> rawProtein) {
        RawProtein<PantherRawMatch> result = new RawProtein<>(rawProtein.getProteinIdentifier());
        for (PantherRawMatch rawProteinMatch : rawProtein.getMatches()) {
            if (rawProteinMatch.getEvalue() <= geteValueCutoff()) {
                result.addMatch(rawProteinMatch);
            } else {
                LOGGER.info("Discarding the following protein raw match because it is not hold on the evalue cutoff: " + rawProteinMatch.getModelId());
            }
        }
        return result;
    }
}
