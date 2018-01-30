package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusFeature;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.model.*;

import javax.persistence.Query;
import java.util.*;

/**
 * Implements the persistence method for PhobiusProtein objects
 * (the temporary objects used in Phobius file parsing)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class PhobiusFilteredMatchDAOImpl extends GenericDAOImpl<PhobiusMatch, Long> implements PhobiusFilteredMatchDAO {

    private static final Logger LOGGER = Logger.getLogger(PhobiusFilteredMatchDAOImpl.class.getName());

    /**
     * The version number of Phobius being run.
     */
    private final String phobiusVersion;


    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     *
     * @param phobiusVersion being the current version number of Phobius.
     */
    public PhobiusFilteredMatchDAOImpl(String phobiusVersion) {
        super(PhobiusMatch.class);
        this.phobiusVersion = phobiusVersion;
    }

    /**
     * the persistence method for PhobiusProtein objects
     * (the temporary objects used in Phobius file parsing)
     * <p/>
     * As there are only a restricted number of Phobius Signatures / Models
     * (as defined in the PhobiusFeatureType enum) all of the signature objects
     * are retrieved from the database prior to commencing parsing.
     * <p/>
     * If these are not found in the database, they are created and then used.
     *
     * @param phobiusProteins being the Set of PhobiusProtein objects to
     *                        be transformed and persisted.
     * @see PhobiusFeatureType
     */
    @Transactional
    public void persist(Set<PhobiusProtein> phobiusProteins) {
        // Retrieve (or create and persist) PhobiusSignatures.
        Map<PhobiusFeatureType, Signature> featureTypeToSignatureMap = loadPersistedSignatures();
        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(phobiusProteins);
        for (PhobiusProtein phobiusProtein : phobiusProteins) {
            final Protein persistentProtein = proteinIdToProteinMap.get(phobiusProtein.getProteinIdentifier());
            if (persistentProtein == null) {
                throw new IllegalArgumentException("Attempting to store a Phobius match for a protein with id " + phobiusProtein.getProteinIdentifier() + ", however this does not exist in the database.");
            }
            for (PhobiusFeature feature : phobiusProtein.getFeatures()) {
                final Signature signature = featureTypeToSignatureMap.get(feature.getFeatureType());
                Set<PhobiusMatch.PhobiusLocation> locations = Collections.singleton(
                        new PhobiusMatch.PhobiusLocation(feature.getStart(), feature.getStop())
                );
                PhobiusMatch match = new PhobiusMatch(signature, signature.getAccession(), locations);
                persistentProtein.addMatch(match);
                entityManager.persist(match);
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
    private Map<PhobiusFeatureType, Signature> loadPersistedSignatures() {
        Map<PhobiusFeatureType, Signature> signatures = new HashMap<PhobiusFeatureType, Signature>(PhobiusFeatureType.values().length);

        // Check first to see if the SignatureLibraryRelease exists.  If not, create it.
        final SignatureLibraryRelease release = loadPhobiusRelease();

        // Now try to retrieve the Signatures for Phobius.  If they do not exist, create them.
        final Query query = entityManager.createQuery("select s from Signature s where s.signatureLibraryRelease = :release");
        query.setParameter("release", release);
        @SuppressWarnings("unchecked") List<Signature> retrievedSignatures = query.getResultList();
        for (final PhobiusFeatureType type : PhobiusFeatureType.values()) {
            boolean found = false;
            for (final Signature retrievedSignature : retrievedSignatures) {
                if (type.getName().equals(retrievedSignature.getName())
                        &&
                        type.getAccession().equals(retrievedSignature.getAccession())
                        &&
                        type.getDescription().equals(retrievedSignature.getDescription())) {
                    signatures.put(type, retrievedSignature);
                    found = true;
                    break;  // Found the correct signature, don't carry on looking.
                }
            }
            if (!found) {
                // Create and persist a new Signature.
                Signature.Builder builder = new Signature.Builder(type.getAccession());
                final Signature signature = builder
                        .name(type.getName())
                        .description(type.getDescription())
                        .signatureLibraryRelease(release)
                        .build();
                entityManager.persist(signature);
                signatures.put(type, signature);
            }
        }
        return signatures;
    }

    /**
     * This private method is responsible for retrieving OR persisting (as appropriate)
     * a SignatureLibraryRelease method for the version of Phobius being handled by this
     * DAO.
     *
     * @return the retrieved / persisted SignatureLibraryRelease object.
     */
    private SignatureLibraryRelease loadPhobiusRelease() {
        final SignatureLibraryRelease release;
        final Query releaseQuery = entityManager.createQuery("select r from SignatureLibraryRelease r where r.version = :phobiusVersion and r.library = :phobiusSignatureLibrary");
        releaseQuery.setParameter("phobiusVersion", phobiusVersion);
        releaseQuery.setParameter("phobiusSignatureLibrary", SignatureLibrary.PHOBIUS);
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = releaseQuery.getResultList();
        if (releaseList.size() == 1 && releaseList.get(0) != null) {
            release = releaseList.get(0);
        } else if (releaseList.size() > 1) {
            throw new IllegalStateException("There is more than one SignatureLibraryRelease record for version " + phobiusVersion + " of Phobius in the databases.");
        } else {
            release = new SignatureLibraryRelease(SignatureLibrary.PHOBIUS, phobiusVersion);
            entityManager.persist(release);
        }
        return release;
    }

    /**
     * Helper method that converts a List of Protein objects retrieved from a JQL query
     * into a Map of protein IDs to Protein objects.
     *
     * @param phobiusProteins being the Set of PhobiusProteins containing the IDs of the Protein objects
     *                        required.
     * @return a Map of protein IDs to Protein objects.
     */

    private Map<String, Protein> getProteinIdToProteinMap(Set<PhobiusProtein> phobiusProteins) {
        final Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>(phobiusProteins.size());

        final List<Long> proteinIds = new ArrayList<Long>(phobiusProteins.size());
        for (PhobiusProtein phobProt : phobiusProteins) {
            String proteinIdAsString = phobProt.getProteinIdentifier();
            proteinIds.add(new Long(proteinIdAsString));
        }
        final int proteinIdCount = proteinIds.size();
        for (int index = 0; index < proteinIdCount; index += MAXIMUM_IN_CLAUSE_SIZE) {
            int endIndex = index + MAXIMUM_IN_CLAUSE_SIZE;
            if (endIndex > proteinIdCount) {
                endIndex = proteinIdCount;
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
