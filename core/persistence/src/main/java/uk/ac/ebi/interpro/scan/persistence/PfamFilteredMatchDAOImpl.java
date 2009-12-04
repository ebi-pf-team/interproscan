package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.HmmerMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: Dec 3, 2009
 * Time: 4:09:39 PM
 */
public class PfamFilteredMatchDAOImpl extends GenericDAOImpl<HmmerMatch,  Long> implements PfamFilteredMatchDAO{


    private static final Logger LOGGER = Logger.getLogger(PfamFilteredMatchDAOImpl.class);
    /**
     * Sets the class of the model that the DOA instance handles.
     *
     */
    public PfamFilteredMatchDAOImpl() {
        super(HmmerMatch.class);
    }

    /**
     * To understand this class, START HERE - uses private 'helper' methods to simplify this very
     * complex transaction.
     *
     * Persists filtered matches to the database that are referenced
     * from a RawProtein<PfamHmmer3RawMatch> object.
     *
     * NOTE: While complex, this example is relatively simple as the Signatures and the Models have
     * the same accession in Pfam, so shortcuts can be taken.
     *
     * @param rawProteins             containing a Collection of filtered PfamHmmer3RawMatch objects
     */
    @Transactional
    public void persistFilteredMatches(Collection<RawProtein<PfamHmmer3RawMatch>> rawProteins) {
        // First of all, retrieve the appropriate Signatures for all of the (filtered) raw matches,
        // and all of the appropriate Protein objects.

        Query signatureQuery = entityManager.createQuery(
                "select s from Signature s " +
                        "where s.accession in (:modelId) " +
                        "and s.signatureLibraryRelease.version = :signatureLibraryVersion " +
                        "and s.signatureLibraryRelease.library.name = :signatureLibraryName");



        Query proteinQuery = entityManager.createQuery(
                "select p from Protein p where p.id = :proteinId"
        );

        // Iterate over the matches in the Collection of RawProtein objects
        // to complete building the queries for the appropriate Signatures and Proteins.
        String signatureLibraryName = null;
        String signatureLibraryVersion = null;
        List<String> modelAccessions = new ArrayList<String>();
        List<Long> proteinIds = new ArrayList<Long>();

        for (RawProtein<PfamHmmer3RawMatch> rawProtein : rawProteins){
            for (PfamHmmer3RawMatch match : rawProtein.getMatches()){
                if (signatureLibraryName == null){
                    signatureLibraryName = match.getSignatureLibraryName();
                    signatureLibraryVersion = match.getSignatureLibraryRelease();
                }
                else if (! signatureLibraryName.equals(match.getSignatureLibraryName()) ||
                        ! signatureLibraryVersion.equals(match.getSignatureLibraryRelease())){
                    throw new IllegalArgumentException ("The filtered matches that you are attempting to store using the persistFileredMatches method come from different signature library versions.");
                }

                modelAccessions.add(match.getModel());
                proteinIds.add(Long.parseLong(match.getSequenceIdentifier()));
            }
        }

        if (modelAccessions.size() == 0){
            LOGGER.debug("No filtered matches to store.");
            return;     // Nothing to do.
        }
        else if (signatureLibraryName == null || signatureLibraryVersion == null){
            throw new IllegalArgumentException("The filtered matches that you are attempting to store do not have signature library name / version information.");
        }

        // Set all of the query parameters.
        signatureQuery.setParameter("signatureLibraryName", signatureLibraryName);
        signatureQuery.setParameter("signatureLibraryVersion", signatureLibraryVersion);
        signatureQuery.setParameter("modelId", modelAccessions);
        proteinQuery.setParameter("proteinId", proteinIds);

        // Retrieve the signatures and proteins and place them in Maps for quick lookup.
        final Map<String, Signature> signatureAccessionToSignatureMap = getSignatureAccessionToSignatureMap (signatureQuery);
        final Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap (proteinQuery);

        // Finally build and store the HmmerMatch objects.
        buildHmmerMatches(rawProteins, signatureAccessionToSignatureMap, proteinIdToProteinMap);
    }

    /**
     * Helper method to simplify a very complex transaction - creates Matches between Protein and Signature
     * objects based upon (filtered) raw match data.
     * @param rawProteins being the raw match objects to use as templates for the required HmmerMatch objects.
     * @param signatureAccessionToSignatureMap being a lookup Map of Signature objects.
     * @param proteinIdToProteinMap being a Lookup Map of Protein objects.
     */
    @SuppressWarnings("unchecked")
    private void buildHmmerMatches(Collection<RawProtein<PfamHmmer3RawMatch>> rawProteins,
                                   Map<String, Signature> signatureAccessionToSignatureMap,
                                   Map<String, Protein> proteinIdToProteinMap){
        // Iterate over the filtered matches again and link the appropriate Signature and Protein objects.
        for (RawProtein<PfamHmmer3RawMatch> rawProtein : rawProteins){
            for (PfamHmmer3RawMatch rawMatchObject : rawProtein.getMatches()){
                Signature signature = signatureAccessionToSignatureMap.get(rawMatchObject.getModel());
                Protein protein = proteinIdToProteinMap.get(rawMatchObject.getSequenceIdentifier());
                // Throw an Exception if either the Protein or the Signature are not in the databse.
                // TODO - This may not be the correct course of action!!  Consider further.
                if (signature == null){
                    throw new IllegalStateException ("Attempting to store a match to a Pfam Signature that is not in the database. Model / Signature accession " + rawMatchObject.getModel());
                }
                // TODO - This is DEFINITELY not the right course of action for InterProScan - may be a Protein that has not been seen before in I5.
                if (protein == null){
                    throw new IllegalStateException ("Attempting to store a match to a Protein that is not in the database (The missing protein has priamry key " + rawMatchObject.getSequenceIdentifier());
                }
                // Now link the Signature and Protein with a HmmerMatch object.
                HmmerMatch match = new HmmerMatch(signature, rawMatchObject);
                // ... and store the match
                // (This may be the wrong way of persisting this - perhaps need to persist the Protein object in the next loop out.
                entityManager.persist(match);
            }
        }
    }

    /**
     * Helper method that converts a List of Signature objects retrieved from a JQL query
     * into a Map of signature accession to Signature object.
     * @param signatureQuery being the query to retrieve the Signatures.
     * @return a Map of signature accessions to Signature objects.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Signature> getSignatureAccessionToSignatureMap (Query signatureQuery){
        List<Signature> signatures = signatureQuery.getResultList();
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("Number of signatures retrieved: " + signatures.size());
            LOGGER.debug("Signatures retrieved from database: " + signatures);
        }
        Map<String, Signature> signatureAccessionToSignatureMap = new HashMap<String, Signature>(signatures.size());
        for (Signature signature : signatures){
            signatureAccessionToSignatureMap.put(signature.getAccession(), signature);
        }
        return signatureAccessionToSignatureMap;
    }


    /**
     * Helper method that converts a List of Protein objects retrieved from a JQL query
     * into a Map of protein IDs to Protein objects.
     * @param proteinQuery being the query to retrieve the Proteins
     * @return a Map of protein IDs to Protein objects.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Protein> getProteinIdToProteinMap (Query proteinQuery){
        List<Protein> proteins = proteinQuery.getResultList();
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("Number of proteins retrieved: " + proteins.size());
            LOGGER.debug("Proteins retrieved from database: " + proteins);
        }
        Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>(proteins.size());
        for (Protein protein : proteins){
            proteinIdToProteinMap.put(protein.getId().toString(), protein);
        }
        return proteinIdToProteinMap;
    }
}
