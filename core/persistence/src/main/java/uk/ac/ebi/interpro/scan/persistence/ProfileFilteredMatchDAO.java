package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;

import java.util.*;

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
    public void persist(Collection<RawProtein<T>> filteredProteins, Map<String, SignatureModelHolder> modelAccessionToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {

        SignatureLibrary signatureLibrary = null;
        for (RawProtein<T> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());

            for (T rawMatch : rawProtein.getMatches()) {
                SignatureModelHolder holder = modelAccessionToSignatureMap.get(rawMatch.getModelId());
                Signature signature = holder.getSignature();
                if(signatureLibrary == null) {
                    signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                }
                ProfileScanMatch match = buildMatch(signature, rawMatch);
                //hibernateInitialise
                hibernateInitialise(match);
                protein.addMatch(match);
                entityManager.persist(match);
            }
            Set<Match> proteinMatches = protein.getMatches();
            if (! proteinMatches.isEmpty()) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibrary.getName();
                Set<Match> matchSet = new HashSet<>(proteinMatches);
                matchDAO.persist(dbKey, matchSet);
            }
        }
    }

    private ProfileScanMatch buildMatch(Signature signature, T rawMatch) {
        ProfileScanMatch.ProfileScanLocation location = new ProfileScanMatch.ProfileScanLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd(),
                rawMatch.getScore(),
                rawMatch.getCigarAlignment());
        return new ProfileScanMatch(signature, rawMatch.getModelId(), Collections.singleton(location));
    }
}
