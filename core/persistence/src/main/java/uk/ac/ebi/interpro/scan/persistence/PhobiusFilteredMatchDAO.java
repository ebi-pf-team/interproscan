package uk.ac.ebi.interpro.scan.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.PhobiusRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.Query;
import java.util.*;

/**
 * class defining the persistence method for PhobiusProtein objects
 * (the temporary objects used in Phobius file parsing)
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class PhobiusFilteredMatchDAO extends FilteredMatchDAOImpl<PhobiusRawMatch, PhobiusMatch> {

    private static final Logger LOGGER = LogManager.getLogger(PhobiusFilteredMatchDAO.class.getName());

    final SignatureLibraryRelease signatureLibraryRelease;

    private String phobiusReleaseVersion;

    public PhobiusFilteredMatchDAO(SignatureLibraryRelease signatureLibraryRelease) {
        super(PhobiusMatch.class);
        this.signatureLibraryRelease = signatureLibraryRelease;
        this.phobiusReleaseVersion = signatureLibraryRelease.getVersion();
    }

    public SignatureLibraryRelease getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public String getPhobiusReleaseVersion() {
        return phobiusReleaseVersion;
    }

    public void setPhobiusReleaseVersion(String phobiusReleaseVersion) {
        this.phobiusReleaseVersion = phobiusReleaseVersion;
    }


    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins             being the Collection of filtered RawProtein objects to persist
     */
    @Override
    @Transactional
    public void persist(Collection<RawProtein<PhobiusRawMatch>> filteredProteins) {
        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(filteredProteins);
        String signatureLibraryKey = signatureLibraryRelease.getLibrary().getName();;

        Map<PhobiusFeatureType, Signature> featureTypeToSignatureMap = loadPersistedSignatures();

        Utilities.verboseLog(1100, "protein size: " + filteredProteins.size());
        Utilities.verboseLog(1100, "featureTypeToSignatureMap size: " + featureTypeToSignatureMap.size());
        for (RawProtein<PhobiusRawMatch> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            Set<Match> proteinMatches = new HashSet();

            for (PhobiusRawMatch rawMatch : rawProtein.getMatches()) {
                final Signature signature = featureTypeToSignatureMap.get(rawMatch.getFeatureType());
                Set<PhobiusMatch.PhobiusLocation> locations = Collections.singleton(
                        new PhobiusMatch.PhobiusLocation(rawMatch.getLocationStart(), rawMatch.getLocationEnd())
                );
                PhobiusMatch match = new PhobiusMatch(signature, signature.getAccession(), locations);
//                if (match == null){
//                    LOGGER.warn("match is NULL");
//                }
//                if (signature == null){
//                    LOGGER.warn("signature is NULL");
//                }
//                Utilities.verboseLog(1100, "Phobius match - seqId: " + rawMatch.getSequenceIdentifier() +
//                        " signature: " + signature.getAccession() + " lib: " + signature.getSignatureLibraryRelease().getLibrary().getName() +
//                        " start: " + rawMatch.getLocationStart() +
//                        " end: " + rawMatch.getLocationEnd());
//                Utilities.verboseLog(1100, "match: " + match);
                proteinMatches.add(match);
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
        releaseQuery.setParameter("phobiusVersion", phobiusReleaseVersion);
        releaseQuery.setParameter("phobiusSignatureLibrary", SignatureLibrary.PHOBIUS);
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = releaseQuery.getResultList();
        if (releaseList.size() == 1 && releaseList.get(0) != null) {
            release = releaseList.get(0);
        } else if (releaseList.size() > 1) {
            throw new IllegalStateException("There is more than one SignatureLibraryRelease record for version " + phobiusReleaseVersion + " of Phobius in the databases.");
        } else {
            release = new SignatureLibraryRelease(SignatureLibrary.PHOBIUS, phobiusReleaseVersion);
            entityManager.persist(release);
        }
        return release;
    }


}
