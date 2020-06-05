package uk.ac.ebi.interpro.scan.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMProtein;
import uk.ac.ebi.interpro.scan.model.*;

import javax.persistence.Query;
import java.util.*;

/**
 * Implements the persistence method for TMHMMProtein objects.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public class TMHMMFilteredMatchDAOImplOld extends GenericDAOImpl<TMHMMMatch, Long> implements TMHMMFilteredMatchDAOOld {

    private static final Logger LOGGER = LogManager.getLogger(TMHMMFilteredMatchDAOImplOld.class.getName());

    private final SignatureLibraryRelease signatureLibraryRelease;

    public TMHMMFilteredMatchDAOImplOld(SignatureLibraryRelease signatureLibraryRelease) {
        super(TMHMMMatch.class);
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Transactional
    public void persist(Set<TMHMMProtein> proteins) {
        //load TMHMM signatures from database and map them by accession
        Map<String, Signature> sigAccToDbSignatureMap = loadPersistedSignatures();
        //load proteins from database and map them by protein identifier
        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(proteins);
        for (TMHMMProtein protein : proteins) {
            final Protein persistentProtein = proteinIdToProteinMap.get(protein.getProteinIdentifier());
            if (persistentProtein == null) {
                throw new IllegalArgumentException("Attempting to store a TMHMM match for a protein with id " + protein.getProteinIdentifier() + ", however this does not exist in the database.");
            }
            for (TMHMMMatch match : protein.getMatches()) {
                final Signature signature = sigAccToDbSignatureMap.get(match.getSignature().getAccession());
                TMHMMMatch newMatch = new TMHMMMatch(signature, signature.getAccession(), match.getLocations());
                persistentProtein.addMatch(newMatch);
                entityManager.persist(newMatch);
            }
        }
    }

    /**
     * This method retrieves the persisted Signatures for all of the
     * TMHMMSignatures.  If any of them they do not exist in the database, this
     * method creates the required Signatures.
     *
     * @return the persisted Signatures for all of the TMHMMSignatures.
     */
    private Map<String, Signature> loadPersistedSignatures() {
        Map<String, Signature> signatures = new HashMap<String, Signature>(TMHMMSignature.values().length);

        // Check first to see if the SignatureLibraryRelease exists.  If not, create it.
        final SignatureLibraryRelease release = loadTMHMMRelease();

        // Now try to retrieve the Signatures for TMHMM.  If they do not exist, create them.
        final Query query = entityManager.createQuery("select s from Signature s where s.signatureLibraryRelease = :release");
        query.setParameter("release", release);
        @SuppressWarnings("unchecked") List<Signature> retrievedSignatures = query.getResultList();
        for (final TMHMMSignature tmhmmSignature : TMHMMSignature.values()) {
            boolean found = false;
            String signatureAcc = tmhmmSignature.getAccession();
            for (final Signature retrievedSignature : retrievedSignatures) {
                if (tmhmmSignature.getAccession().equals(retrievedSignature.getAccession())
                        &&
                        tmhmmSignature.getAccession().equals(retrievedSignature.getAccession())) {
                    signatures.put(signatureAcc, retrievedSignature);
                    found = true;
                    break;  // Found the correct signature, don't carry on looking.
                }
            }
            if (!found) {
                // Create and persist a new Signature.
                Signature.Builder builder = new Signature.Builder(tmhmmSignature.getAccession());
                final Signature signature = builder
                        .description(tmhmmSignature.getShortDesc())
                        .signatureLibraryRelease(release)
                        .build();
                entityManager.persist(signature);
                signatures.put(signatureAcc, signature);
            }
        }
        return signatures;
    }

    /**
     * This private method is responsible for retrieving OR persisting (as appropriate)
     * a SignatureLibraryRelease method for the version of TMHMM being handled by this
     * DAO.
     *
     * @return the retrieved / persisted SignatureLibraryRelease object.
     */
    private SignatureLibraryRelease loadTMHMMRelease() {
        SignatureLibraryRelease release = null;
        final Query releaseQuery = entityManager.createQuery("select r from SignatureLibraryRelease r where r.version = :version and r.library = :signatureLibrary");
        releaseQuery.setParameter("version", signatureLibraryRelease.getVersion());
        releaseQuery.setParameter("signatureLibrary", signatureLibraryRelease.getLibrary());
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = releaseQuery.getResultList();
        if (releaseList.size() == 1 && releaseList.get(0) != null) {
            release = releaseList.get(0);
        } else if (releaseList.size() > 1) {
            throw new IllegalStateException("There is more than one SignatureLibraryRelease record for version " + signatureLibraryRelease.getVersion() + " of TMHMM in the databases.");
        } else {
            entityManager.persist(new SignatureLibraryRelease(signatureLibraryRelease.getLibrary(), signatureLibraryRelease.getVersion()));
        }
        return release;
    }

    /**
     * Helper method that converts a List of Protein objects retrieved from a JQL query
     * into a Map of protein IDs to Protein objects.
     *
     * @return a Map of protein IDs to Protein objects.
     */

    private Map<String, Protein> getProteinIdToProteinMap(Set<TMHMMProtein> tmhmmProteins) {
        final Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>(tmhmmProteins.size());

        final List<Long> proteinIds = new ArrayList<Long>(tmhmmProteins.size());
        for (TMHMMProtein tmhmmProtein : tmhmmProteins) {
            String proteinIdAsString = tmhmmProtein.getProteinIdentifier();
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
            @SuppressWarnings("unchecked") List<Protein> dbProteins = proteinQuery.getResultList();
            for (Protein protein : dbProteins) {
                proteinIdToProteinMap.put(protein.getId().toString(), protein);
            }
        }
        return proteinIdToProteinMap;
    }
}
