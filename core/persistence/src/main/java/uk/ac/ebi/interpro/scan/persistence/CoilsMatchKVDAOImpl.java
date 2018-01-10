package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

import org.apache.commons.lang3.SerializationUtils;


import javax.persistence.Query;
import java.util.*;

/**
 * Implements the persistence method for Coils matches (as filtered matches).
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class CoilsMatchKVDAOImpl extends FilteredMatchKVDAOImpl<CoilsMatch, RawMatch> implements CoilsMatchKVDAO {

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
    public CoilsMatchKVDAOImpl(String version) {
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
        CoilsMatch repMatch = null;
        CoilsMatch theMatch = null;
        Signature coilsSignature = loadPersistedSignature();
        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(coilsMatches);
        Map<String, HashSet<CoilsMatch>> keyToMatchMap = new HashMap<>();
        Long timeNow = System.currentTimeMillis();
        for (ParseCoilsMatch parseCoilsMatch : coilsMatches) {
            final Protein persistentProtein = proteinIdToProteinMap.get(parseCoilsMatch.getProteinDatabaseIdentifier());
            if (persistentProtein == null) {
                throw new IllegalArgumentException("Attempting to store a Coils match for a protein with id " + parseCoilsMatch.getProteinDatabaseIdentifier() + ", however this does not exist in the database.");
            }
            Set<CoilsMatch.CoilsLocation> locations = Collections.singleton(
                    new CoilsMatch.CoilsLocation(parseCoilsMatch.getStartCoordinate(), parseCoilsMatch.getEndCoordinate())
            );
            CoilsMatch match = new CoilsMatch(coilsSignature, locations);
            if (repMatch == null) {
                repMatch = match;
//                Utilities.verboseLog("repMatch: " + repMatch.toString());
            }
            String key =  parseCoilsMatch.getProteinDatabaseIdentifier() + SignatureLibrary.COILS.getName();
            byte[] byteKey = SerializationUtils.serialize(key);
            byte[] byteMatch = SerializationUtils.serialize(match);
            HashSet<CoilsMatch> matchesForThisKey = keyToMatchMap.get(key);
            if (matchesForThisKey == null) {
                matchesForThisKey = new HashSet<>();
            }
            matchesForThisKey.add(match);
            keyToMatchMap.put(key, matchesForThisKey);

            //persist(key,match);
            //persist(byteKey,byteMatch);
        }
        Long timeTaken = System.currentTimeMillis() - timeNow;
        Long timeTakenSecs = timeTaken / 1000;
        Long timeTakenMins = timeTakenSecs / 60;
        Utilities.verboseLog("Time taken to process " + coilsMatches.size() + " complete Coils Matches and persist to kvStore : "
                + timeTakenSecs + " seconds ("
                + timeTakenMins + " minutes)");

        Map<byte[], byte[]> byteKeyToMatchMap = new HashMap<>();

        Iterator it = keyToMatchMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey(); //).replace(SignatureLibrary.COILS.getName(), "").trim();
            byte[] byteKey = SerializationUtils.serialize(key);
            byte[] byteMatch = SerializationUtils.serialize((HashSet<CoilsMatch>) pair.getValue());
            byteKeyToMatchMap.put(byteKey, byteMatch);
            persist(byteKey, byteMatch);
        }
        Utilities.verboseLog("Completed processing byteKeyToMatchMap ");
        //persist(byteKeyToMatchMap);

        if (coilsMatches.size() > 0) {
            //Utilities.verboseLog("addSignatureLibraryName : " + SignatureLibrary.COILS.getName());
            addSignatureLibraryName(SignatureLibrary.COILS.getName());
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
