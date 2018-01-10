package uk.ac.ebi.interpro.scan.persistence;

import com.mchange.v1.identicator.IdHashSet;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import org.apache.commons.lang3.SerializationUtils;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

/**
 * @author Gift Nuka
 *
 * @since 1.0
 */

public class ProfileFilteredMatchKVDAO<T extends ProfileScanRawMatch>
        extends FilteredMatchKVDAOImpl<ProfileScanMatch, T>
        implements FilteredMatchKVDAO<ProfileScanMatch, T> {

    public ProfileFilteredMatchKVDAO() {
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

        SignatureLibrary signatureLibraryRep = null;
        for (RawProtein<T> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());

            Set<ProfileScanMatch> filteredMatches =  new HashSet<>();
            for (T rawMatch : rawProtein.getMatches()) {
                Signature signature = modelAccessionToSignatureMap.get(rawMatch.getModelId());
                ProfileScanMatch match = buildMatch(signature, rawMatch);
                filteredMatches.add(match);
                if (signatureLibraryRep == null){
                    //ProfileScanRawMatch repRawMatch =  (ProfileScanRawMatch) new ArrayList(protein.getMatches()).get(0);
                    signatureLibraryRep = rawMatch.getSignatureLibrary();
                }
            }
            if (filteredMatches.size() > 0) {
                String key = Long.toString(protein.getId()) + signatureLibraryRep.getName();
                byte[] byteKey = SerializationUtils.serialize(key);
                byte[] byteMatches = SerializationUtils.serialize((HashSet<ProfileScanMatch>) filteredMatches);
                Utilities.verboseLog("To persist key: " + key + " matches: " + filteredMatches.toString());
                if(byteMatches ==  null){
                    Utilities.verboseLog("Something is wrong here ... ");
                }
                persist(byteKey, byteMatches);
            }
        }
        if (signatureLibraryRep != null ){
            Utilities.verboseLog("SignatureLibrary  to add: " + signatureLibraryRep.getName());
            addSignatureLibraryName(signatureLibraryRep.getName());
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
