package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.MobiDBRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.Query;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

class MobiDBFilteredMatchDAO extends FilteredMatchDAOImpl<MobiDBRawMatch, MobiDBMatch> {

    final SignatureLibraryRelease release;

    private String mobidbReleaseVersion;

    public MobiDBFilteredMatchDAO(SignatureLibraryRelease release) {
        super(MobiDBMatch.class);
        this.release = release;
        this.mobidbReleaseVersion = release.getVersion();
    }

    public SignatureLibraryRelease getRelease() {
        return release;
    }

    public String getMobidbReleaseVersion() {
        return mobidbReleaseVersion;
    }

    public void setMobidbReleaseVersion(String mobidbReleaseVersion) {
        this.mobidbReleaseVersion = mobidbReleaseVersion;
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
    public void persist(Collection<RawProtein<MobiDBRawMatch>> filteredProteins, Map<String, Signature> modelAccessionToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {

        for (RawProtein<MobiDBRawMatch> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            for (MobiDBRawMatch rawMatch : rawProtein.getMatches()) {
                Signature signature = loadPersistedSignature();
//                Utilities.verboseLog(rawMatch.toString());
                MobiDBMatch match = buildMatch(signature, rawMatch);
//                Utilities.verboseLog("MobiDb match:" + match.toString());
                protein.addMatch(match);
                entityManager.persist(match);

            }
        }
    }

    private MobiDBMatch buildMatch(Signature signature, MobiDBRawMatch rawMatch) {
        MobiDBMatch.MobiDBLocation location = new MobiDBMatch.MobiDBLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd());
        return new MobiDBMatch(signature, Collections.singleton(location));
    }

    /**
     * This method retrieves the persisted Signatures for all of the
     * Disordered featues.  If any of them they do not exist in the database, this
     * method creates the required Signatures.
     *
     * @return the persisted Signatures for all of the
     *         Disordered features.
     */
    private Signature loadPersistedSignature() {
        final SignatureLibraryRelease release = loadMobiDBRelease();

        final Signature.Builder mobiSignatureBuilder = new Signature.Builder("mobidb-lite");
        final Signature signature = mobiSignatureBuilder.name("mobidb-lite").signatureLibraryRelease(release).build();

//        if (release != null) {
//            return signature;
//        }

        // Check first to see if the SignatureLibraryRelease exists.  If not, create it.

        // Now try to retrieve the Signatures for Phobius.  If they do not exist, create them.
        final Query query = entityManager.createQuery("select s from Signature s where s.signatureLibraryRelease = :release");
        query.setParameter("release", release);
        @SuppressWarnings("unchecked") List<Signature> retrievedSignatures = query.getResultList();
//        Utilities.verboseLog("retrievedSignatures size: " + retrievedSignatures.size());

        if (retrievedSignatures.size() == 0) {
            // The Signature record does not exist yet, so create it.

            entityManager.persist(signature);
            return signature;
        } else if (retrievedSignatures.size() > 1) {
            // Error detected - more than one Signature record for this release of Coils
            throw new IllegalStateException("There is more than one Signature record for version " + mobidbReleaseVersion + " of Coils in the database.");
        } else {
            // return the previously persisted Signature.
            return retrievedSignatures.get(0);
        }
    }

    /**
     * This private method is responsible for retrieving OR persisting (as appropriate)
     * a SignatureLibraryRelease method for the version of MobiDB being handled by this
     * DAO.
     *
     * @return the retrieved / persisted SignatureLibraryRelease object.
     */
    private SignatureLibraryRelease loadMobiDBRelease() {
        final SignatureLibraryRelease release;
        final Query releaseQuery = entityManager.createQuery("select r from SignatureLibraryRelease r where r.version = :coilsVersion and r.library = :coilsSignatureLibrary");
        releaseQuery.setParameter("coilsVersion", mobidbReleaseVersion);
        releaseQuery.setParameter("coilsSignatureLibrary", SignatureLibrary.MOBIDB_LITE);
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = releaseQuery.getResultList();
        if (releaseList.size() == 1 && releaseList.get(0) != null) {
            release = releaseList.get(0);
        } else if (releaseList.size() > 1) {
            throw new IllegalStateException("There is more than one SignatureLibraryRelease record for version " + mobidbReleaseVersion + " of MobiDB in the databases.");
        } else {
            release = new SignatureLibraryRelease(SignatureLibrary.MOBIDB_LITE, mobidbReleaseVersion);
            entityManager.persist(release);
        }
        return release;
    }



}
