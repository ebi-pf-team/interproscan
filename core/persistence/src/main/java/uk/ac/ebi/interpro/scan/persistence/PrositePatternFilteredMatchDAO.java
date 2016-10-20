package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.PatternScanMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.ProSitePatternRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PrositePatternFilteredMatchDAO
        extends FilteredMatchDAOImpl<ProSitePatternRawMatch, PatternScanMatch>
        implements FilteredMatchDAO<ProSitePatternRawMatch, PatternScanMatch> {

    public PrositePatternFilteredMatchDAO() {
        super(PatternScanMatch.class);
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins             being the Collection of filtered RawProtein objects to persist
     * @param modelAccessionToSignatureMap a Map of model accessions to Signature objects.
     * @param proteinIdToProteinMap        a Map of Protein IDs to Protein objects
     */
    @Override
    public void persist(Collection<RawProtein<ProSitePatternRawMatch>> filteredProteins, Map<String, Signature> modelAccessionToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<ProSitePatternRawMatch> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            for (ProSitePatternRawMatch rawMatch : rawProtein.getMatches()) {

                Signature signature = modelAccessionToSignatureMap.get(rawMatch.getModelId());
                PatternScanMatch match = buildMatch(signature, rawMatch);
                protein.addMatch(match);
                entityManager.persist(match);
            }
        }
    }

    private PatternScanMatch buildMatch(Signature signature, ProSitePatternRawMatch rawMatch) {
        PatternScanMatch.PatternScanLocation location = new PatternScanMatch.PatternScanLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd(),
                rawMatch.getPatternLevel(),
                rawMatch.getCigarAlignment());
        return new PatternScanMatch(signature, Collections.singleton(location));
    }
}
