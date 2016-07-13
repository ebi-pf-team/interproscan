package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

abstract class ProfileFilteredMatchDAO<T extends ProfileScanRawMatch>
        extends FilteredMatchDAOImpl<T, ProfileScanMatch>
        implements FilteredMatchDAO<T, ProfileScanMatch> {

    public ProfileFilteredMatchDAO() {
        super(ProfileScanMatch.class);
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
    @Transactional
    public void persist(Collection<RawProtein<T>> filteredProteins, Map<String, Signature> modelAccessionToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {

        for (RawProtein<T> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            for (T rawMatch : rawProtein.getMatches()) {
                Signature signature = modelAccessionToSignatureMap.get(rawMatch.getModelId());
                ProfileScanMatch match = buildMatch(signature, rawMatch);
                protein.addMatch(match);
                entityManager.persist(match);
            }
        }
    }

    private ProfileScanMatch buildMatch(Signature signature, T rawMatch) {
        ProfileScanMatch.ProfileScanLocation location = new ProfileScanMatch.ProfileScanLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd(),
                rawMatch.getScore(),
                rawMatch.getCigarAlignment());
        return new ProfileScanMatch(signature, Collections.singleton(location));
    }
}
