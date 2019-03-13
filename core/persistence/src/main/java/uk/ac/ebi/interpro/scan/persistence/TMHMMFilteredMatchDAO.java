package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.MobiDBRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TMHMMRawMatch;

import javax.persistence.Query;
import java.util.*;

/**
 * @author Gift Nuka, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class TMHMMFilteredMatchDAO extends FilteredMatchDAOImpl<TMHMMRawMatch, TMHMMMatch> {

    final SignatureLibraryRelease signatureLibraryRelease;

    private String tmhmmReleaseVersion;

    public TMHMMFilteredMatchDAO(SignatureLibraryRelease signatureLibraryRelease) {
        super(TMHMMMatch.class);
        this.signatureLibraryRelease = signatureLibraryRelease;
        this.tmhmmReleaseVersion = signatureLibraryRelease.getVersion();
    }

    public SignatureLibraryRelease getRelease() {
        return signatureLibraryRelease;
    }

    public String getTmhmmReleaseVersion() {
        return tmhmmReleaseVersion;
    }

    public void setTmhmmReleaseVersion(String tmhmmReleaseVersion) {
        this.tmhmmReleaseVersion = tmhmmReleaseVersion;
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
    public void persist(Collection<RawProtein<TMHMMRawMatch>> filteredProteins) {
//        public void persist(Collection<RawProtein<MobiDBRawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelAccessionToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {

        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(filteredProteins);
        String signatureLibraryKey = signatureLibraryRelease.getLibrary().getName();;
        //Signature signature = loadPersistedSignature();  // need only be done once

        for (RawProtein<TMHMMRawMatch> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            Set<Match> proteinMatches = new HashSet();

            for (TMHMMRawMatch rawMatch : rawProtein.getMatches()) {

//                Utilities.verboseLog(rawMatch.toString());
                TMHMMMatch match = buildMatch(rawMatch);
//                Utilities.verboseLog("MobiDb match:" + match.toString());
                proteinMatches.add(match);
                //entityManager.persist(match);
                //if(signatureLibraryKey == null) {
                //    signatureLibraryKey = match.getSignature().getSignatureLibraryRelease().getLibrary().getName();
                //}
            }
            if(! proteinMatches.isEmpty()) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;
                matchDAO.persist(dbKey, proteinMatches);
            }
        }
    }

    private TMHMMMatch buildMatch(TMHMMRawMatch rawMatch) {
        //TMHMMMatch.TMHMMLocation location = buildTmhmmLocation(start, end, prediction);

        TMHMMMatch.TMHMMLocation location = new TMHMMMatch.TMHMMLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd()
        );
        TMHMMSignature prediction = TMHMMSignature.MEMBRANE;
        Signature signature = new Signature.Builder(prediction.getAccession()).
                description(prediction.getShortDesc()).
                signatureLibraryRelease(signatureLibraryRelease).
                build();
        String signatureModel = signature.getAccession();
        return new TMHMMMatch(signature, rawMatch.getModelId(), Collections.singleton(location));
    }

}
