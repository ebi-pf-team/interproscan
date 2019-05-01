package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.MobiDBRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TMHMMRawMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

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
        Signature signature = loadPersistedSignature();  // need only be done once

        for (RawProtein<TMHMMRawMatch> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            Set<Match> proteinMatches = new HashSet();

            for (TMHMMRawMatch rawMatch : rawProtein.getMatches()) {

//                Utilities.verboseLog(rawMatch.toString());
                TMHMMMatch match = buildMatch(rawMatch, signature);

                proteinMatches.add(match);
                //entityManager.persist(match);
                //if(signatureLibraryKey == null) {
                //    signatureLibraryKey = match.getSignature().getSignatureLibraryRelease().getLibrary().getName();
                //}
            }
            if(! proteinMatches.isEmpty()) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;
                for(Match i5Match: proteinMatches){
                    //try update with cross refs etc
                    updateMatch(i5Match);
                }
                matchDAO.persist(dbKey, proteinMatches);
            }
        }
    }

    private TMHMMMatch buildMatch(TMHMMRawMatch rawMatch, Signature signature) {
        //TMHMMMatch.TMHMMLocation location = buildTmhmmLocation(start, end, prediction);

        TMHMMMatch.TMHMMLocation location = new TMHMMMatch.TMHMMLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd()
        );
        /*
        TMHMMSignature prediction = TMHMMSignature.MEMBRANE;
        Signature signature = new Signature.Builder(prediction.getAccession()).
                description(prediction.getShortDesc()).
                signatureLibraryRelease(signatureLibraryRelease).
                build();
                */
        //TEST TODO maybe remove --
        //entityManager.persist(signature);
        //--
        String signatureModel = signature.getAccession();
        return new TMHMMMatch(signature, rawMatch.getModelId(), Collections.singleton(location));
    }


    /**
     * This method retrieves the persisted Signatures for all of the
     * PhobiusFeatureTypes.  If any of them they do not exist in the database, this
     * method creates the required Signatures.
     *
     * @return the persisted Signatures for all of the
     *         PhobiusFeatureTypes.
     */
    private Signature loadPersistedSignature() {
        // Check first to see if the SignatureLibraryRelease exists.  If not, create it.
        final SignatureLibraryRelease release = loadTMHMMRelease();

        // Now try to retrieve the Signatures for Phobius.  If they do not exist, create them.
        final Query query = entityManager.createQuery("select s from Signature s where s.signatureLibraryRelease = :release");
        query.setParameter("release", release);
        @SuppressWarnings("unchecked") List<Signature> retrievedSignatures = query.getResultList();

        TMHMMSignature prediction = TMHMMSignature.MEMBRANE;
        if (retrievedSignatures.size() == 0) {
            // The Signature record does not exist yet, so create it.

            final Signature signature = new Signature.Builder(prediction.getAccession()).
                    description(prediction.getShortDesc()).
                    signatureLibraryRelease(signatureLibraryRelease).
                    build();
            //final Signature.Builder builder = new Signature.Builder("Coil");
            //final Signature signature = builder.name("Coil").signatureLibraryRelease(release).build();
            entityManager.persist(signature);
            return signature;
        } else if (retrievedSignatures.size() > 1) {
            // Error detected - more than one Signature record for this release of TMHMM
            //System.out.println("There is more than one Signature record  (found " + retrievedSignatures.size() + ")  for version " + tmhmmReleaseVersion + " of TMHMM in the database.");
            for(Signature rsignature: retrievedSignatures){
                //System.out.println("TMHMM signature: " + rsignature.toString());

                if(rsignature.getAccession().equals(prediction.getAccession())){
                    //System.out.println("Found TMHMM signature accession match : " + rsignature.getId());
                    return rsignature;
                }
            }
            throw new IllegalStateException("There is more than one Signature record  (found " + retrievedSignatures.size() + ")  for version " + tmhmmReleaseVersion + " of TMHMM in the database, but could find one that matches with the raw match prediction.");
        } else {
            // return the previously persisted Signature.
            return retrievedSignatures.get(0);
        }
    }

    /**
     * This private method is responsible for retrieving OR persisting (as appropriate)
     * a SignatureLibraryRelease method for the version of Coils being handled by this
     * DAO.
     *
     * @return the retrieved / persisted SignatureLibraryRelease object.
     */
    private SignatureLibraryRelease loadTMHMMRelease() {
        final SignatureLibraryRelease release;
        final Query releaseQuery = entityManager.createQuery("select r from SignatureLibraryRelease r where r.version = :tmhmmVersion and r.library = :tmhmmSignatureLibrary");
        releaseQuery.setParameter("tmhmmVersion", tmhmmReleaseVersion);
        releaseQuery.setParameter("tmhmmSignatureLibrary", SignatureLibrary.TMHMM);
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = releaseQuery.getResultList();
        if (releaseList.size() == 1 && releaseList.get(0) != null) {
            release = releaseList.get(0);
        } else if (releaseList.size() > 1) {
            throw new IllegalStateException("There is more than one SignatureLibraryRelease record for version " + tmhmmReleaseVersion + " of Coils in the databases.");
        } else {
            release = new SignatureLibraryRelease(SignatureLibrary.TMHMM, tmhmmReleaseVersion);
            entityManager.persist(release);
        }
        return release;
    }


}
