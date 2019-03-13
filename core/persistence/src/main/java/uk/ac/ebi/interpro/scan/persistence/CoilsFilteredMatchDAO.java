package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.CoilsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.*;

/**
 * @author gift Nuka
 *
 */
public class CoilsFilteredMatchDAO extends  FilteredMatchDAOImpl<CoilsRawMatch, CoilsMatch> {

    final SignatureLibraryRelease signatureLibraryRelease;

    private String coilsReleaseVersion;

    public CoilsFilteredMatchDAO(SignatureLibraryRelease signatureLibraryRelease) {
        super(CoilsMatch.class);
        this.signatureLibraryRelease = signatureLibraryRelease;
        this.coilsReleaseVersion = signatureLibraryRelease.getVersion();
    }

    public SignatureLibraryRelease getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public String getCoilsReleaseVersion() {
        return coilsReleaseVersion;
    }

    public void setCoilsReleaseVersion(String coilsReleaseVersion) {
        this.coilsReleaseVersion = coilsReleaseVersion;
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins             being the Collection of filtered RawProtein objects to persist
     */
    @Override
    @Transactional
    public void persist(Collection<RawProtein<CoilsRawMatch>> filteredProteins) {
        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(filteredProteins);
        String signatureLibraryKey = signatureLibraryRelease.getLibrary().getName();;
        Signature coilsSignature = loadPersistedSignature();  // need only be done once

        for (RawProtein<CoilsRawMatch> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            Set<Match> proteinMatches = new HashSet();

            for (CoilsRawMatch rawMatch : rawProtein.getMatches()) {
                Set<CoilsMatch.CoilsLocation> locations = Collections.singleton(
                        new CoilsMatch.CoilsLocation(rawMatch.getLocationStart(), rawMatch.getLocationEnd())
                );
                CoilsMatch match = new CoilsMatch(coilsSignature, coilsSignature.getAccession(), locations);
                proteinMatches.add(match);
            }
            if(! proteinMatches.isEmpty()) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;
                matchDAO.persist(dbKey, proteinMatches);
            }
        }
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
        final SignatureLibraryRelease release = loadCoilsRelease();

        // Now try to retrieve the Signatures for Phobius.  If they do not exist, create them.
        final Query query = entityManager.createQuery("select s from Signature s where s.signatureLibraryRelease = :release");
        query.setParameter("release", release);
        @SuppressWarnings("unchecked") List<Signature> retrievedSignatures = query.getResultList();

        if (retrievedSignatures.size() == 0) {
            // The Signature record does not exist yet, so create it.
            final Signature.Builder builder = new Signature.Builder("Coil");
            final Signature signature = builder.name("Coil").signatureLibraryRelease(release).build();
            entityManager.persist(signature);
            return signature;
        } else if (retrievedSignatures.size() > 1) {
            // Error detected - more than one Signature record for this release of Coils
            throw new IllegalStateException("There is more than one Signature record for version " + coilsReleaseVersion + " of Coils in the database.");
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
    private SignatureLibraryRelease loadCoilsRelease() {
        final SignatureLibraryRelease release;
        final Query releaseQuery = entityManager.createQuery("select r from SignatureLibraryRelease r where r.version = :coilsVersion and r.library = :coilsSignatureLibrary");
        releaseQuery.setParameter("coilsVersion", coilsReleaseVersion);
        releaseQuery.setParameter("coilsSignatureLibrary", SignatureLibrary.COILS);
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = releaseQuery.getResultList();
        if (releaseList.size() == 1 && releaseList.get(0) != null) {
            release = releaseList.get(0);
        } else if (releaseList.size() > 1) {
            throw new IllegalStateException("There is more than one SignatureLibraryRelease record for version " + coilsReleaseVersion + " of Coils in the databases.");
        } else {
            release = new SignatureLibraryRelease(SignatureLibrary.COILS, coilsReleaseVersion);
            entityManager.persist(release);
        }
        return release;
    }

    /**
     * Helper method that converts a List of Protein objects retrieved from a JQL query
     * into a Map of protein IDs to Protein objects.
     *
     * @param coilsMatches being the Set of PhobiusProteins containing the IDs of the Protein objects
     *                     required.
     * @return a Map of protein IDs to Protein objects.
     */

    private Map<String, Protein> getProteinIdToProteinMap(Set<ParseCoilsMatch> coilsMatches) {
        final Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>(coilsMatches.size());

        final List<Long> proteinIds = new ArrayList<Long>(coilsMatches.size());
        for (ParseCoilsMatch parseCoilsMatch : coilsMatches) {
            String proteinIdAsString = parseCoilsMatch.getProteinDatabaseIdentifier();
            proteinIds.add(new Long(proteinIdAsString));
        }

        for (int index = 0; index < proteinIds.size(); index += MAXIMUM_IN_CLAUSE_SIZE) {
            int endIndex = index + MAXIMUM_IN_CLAUSE_SIZE;
            if (endIndex > proteinIds.size()) {
                endIndex = proteinIds.size();
            }
            final List<Long> proteinIdSlice = proteinIds.subList(index, endIndex);
            final Query proteinQuery = entityManager.createQuery(
                    "select p from Protein p where p.id in (:proteinId)"
            );
            proteinQuery.setParameter("proteinId", proteinIdSlice);
            @SuppressWarnings("unchecked") List<Protein> proteins = proteinQuery.getResultList();
            for (Protein protein : proteins) {
                proteinIdToProteinMap.put(protein.getId().toString(), protein);
            }
        }
        return proteinIdToProteinMap;
    }
}
