package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.model.*;

import javax.persistence.Query;
import java.util.*;

/**
 * Implements the persistence method for Coils matches (as filtered matches).
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class CoilsFilteredMatchDAOImpl extends GenericDAOImpl<CoilsMatch, Long> implements CoilsFilteredMatchDAO {

    private String coilsReleaseVersion;

    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public CoilsFilteredMatchDAOImpl(String version) {
        super(CoilsMatch.class);
        this.coilsReleaseVersion = version;
    }

    /**
     * Persists a set of ParseCoilsMatch objects as filtered matches:
     * there is no filtering step with Coils.
     *
     * @param coilsMatches being a Set of ParseCoilsMatch objects to be persisted.
     */
    @Transactional
    public void persist(Set<ParseCoilsMatch> coilsMatches) {
        Signature coilsSignature = loadPersistedSignature();
        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(coilsMatches);
        for (ParseCoilsMatch parseCoilsMatch : coilsMatches) {
            final Protein persistentProtein = proteinIdToProteinMap.get(parseCoilsMatch.getProteinDatabaseIdentifier());
            if (persistentProtein == null) {
                throw new IllegalArgumentException("Attempting to store a Coils match for a protein with id " + parseCoilsMatch.getProteinDatabaseIdentifier() + ", however this does not exist in the database.");
            }
            Set<CoilsMatch.CoilsLocation> locations = Collections.singleton(
                    new CoilsMatch.CoilsLocation(parseCoilsMatch.getStartCoordinate(), parseCoilsMatch.getEndCoordinate())
            );
            CoilsMatch match = new CoilsMatch(coilsSignature, coilsSignature.getAccession(), locations);
            persistentProtein.addMatch(match);
            entityManager.persist(match);
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
