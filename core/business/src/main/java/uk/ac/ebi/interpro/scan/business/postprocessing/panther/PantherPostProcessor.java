package uk.ac.ebi.interpro.scan.business.postprocessing.panther;

import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

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
    public PantherPostProcessor() {
    }

    /**
     * Promotes Panther/TreeGrafter subfamilies matches
     *
     * @param rawProteins Raw proteins, with associated matches.
     * @return Promoted Panther matches.
     */
    public Set<RawProtein<PantherRawMatch>> process(Set<RawProtein<PantherRawMatch>> rawProteins) {
        Set<RawProtein<PantherRawMatch>> results = new HashSet<>();

        for (RawProtein<PantherRawMatch> rawProtein : rawProteins) {
            RawProtein<PantherRawMatch> promoted = createSubFamilyMatch(rawProtein);
            for (PantherRawMatch rawProteinMatch : promoted.getMatches()) {
                rawProtein.addMatch(rawProteinMatch);
            }
            results.add(rawProtein);
        }

        return results;
    }

    /**
     * Promote the subFamily annotation into a separate Match
     *
     * @param rawProtein Raw protein, with associated matches.
     * @return Panther matches
     */
    private RawProtein<PantherRawMatch> createSubFamilyMatch(final RawProtein<PantherRawMatch> rawProtein) {
        RawProtein<PantherRawMatch> result = new RawProtein<>(rawProtein.getProteinIdentifier());
        for (PantherRawMatch rawProteinMatch : rawProtein.getMatches()) {
            if (rawProteinMatch.getSubFamilyModelId() != null) {
                PantherRawMatch subFamilyRawMatch = rawProteinMatch.getSubFamilyRawMatch();
                result.addMatch(subFamilyRawMatch);
            }

        }
        return result;
    }
}
