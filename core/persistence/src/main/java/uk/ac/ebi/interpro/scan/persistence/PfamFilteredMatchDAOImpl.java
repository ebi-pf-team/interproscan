package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import javax.persistence.Query;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;
import org.apache.log4j.Logger;

/**
 * Persists filtered Pfam HMMER3 matches to the database.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
public class PfamFilteredMatchDAOImpl extends GenericDAOImpl<Hmmer3Match,  Long> implements PfamFilteredMatchDAO{


    private static final Logger LOGGER = Logger.getLogger(PfamFilteredMatchDAOImpl.class);

    private static final int MAXIMUM_IN_CLAUSE_SIZE = 100;

    /**
     * Sets the class of the model that the DOA instance handles.
     *
     */
    public PfamFilteredMatchDAOImpl() {
        super(Hmmer3Match.class);
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
        try{
            // First of all, retrieve the appropriate Signatures for all of the (filtered) raw matches,
            // and all of the appropriate Protein objects.
            final Map<String, Signature> signatureAccessionToSignatureMap = new HashMap<String, Signature>();
            final Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>();









            // Iterate over the matches in the Collection of RawProtein objects
            // to complete building the queries for the appropriate Signatures and Proteins.
            SignatureLibrary signatureLibraryName = null;
            String signatureLibraryVersion = null;
            List<String> modelAccessions = new ArrayList<String>();
            List<Long> proteinIds = new ArrayList<Long>();
            LOGGER.debug("In PfamFilteredMatchDAOImpl.persistFilteredMatches() method.");
            for (RawProtein<PfamHmmer3RawMatch> rawProtein : rawProteins){
                for (PfamHmmer3RawMatch match : rawProtein.getMatches()){
                    if (signatureLibraryName == null){
                        signatureLibraryName = match.getSignatureLibrary();
                        signatureLibraryVersion = match.getSignatureLibraryRelease();
                    }
                    else if (! signatureLibraryName.equals(match.getSignatureLibrary()) ||
                            ! signatureLibraryVersion.equals(match.getSignatureLibraryRelease())){
                        throw new IllegalArgumentException ("Filtered matches are from different signature library versions (more than one library version found)");
                    }

                    modelAccessions.add(match.getModel());
                    proteinIds.add(Long.parseLong(match.getSequenceIdentifier()));
                }
            }
            LOGGER.debug("In PfamFilteredMatchDAOImpl.persistFilteredMatches() method.");
            if (modelAccessions.size() == 0){
                LOGGER.debug("No filtered matches to store.");
                return;     // Nothing to do.
            }
            else if (signatureLibraryName == null || signatureLibraryVersion == null){
                throw new IllegalArgumentException("Filtered matches do not have signature library name or version information.");
            }
            LOGGER.debug("In PfamFilteredMatchDAOImpl.persistFilteredMatches() method. Have matches to score.");
            // Set all of the query parameters.
            // Get the Protein objects - maximum 100 at a go
            for (int index = 0; index < proteinIds.size(); index += MAXIMUM_IN_CLAUSE_SIZE){
                int endIndex = index + MAXIMUM_IN_CLAUSE_SIZE;
                if (endIndex > proteinIds.size()){
                    endIndex = proteinIds.size();
                }
                final List<Long> proteinIdSlice = proteinIds.subList(index, endIndex);
                final Query proteinQuery = entityManager.createQuery(
                        "select p from Protein p where p.id in (:proteinId)"
                );
                proteinQuery.setParameter("proteinId", proteinIdSlice);
                proteinIdToProteinMap.putAll (getProteinIdToProteinMap (proteinQuery));
            }

            // Get the Signature objects - maximum 100 at a go.
            for (int index = 0; index < modelAccessions.size(); index += MAXIMUM_IN_CLAUSE_SIZE){
                int endIndex = index + MAXIMUM_IN_CLAUSE_SIZE;
                if (endIndex > modelAccessions.size()){
                    endIndex = modelAccessions.size();
                }
                final List<String> modelAccessionSlice = modelAccessions.subList(index, endIndex);
                final Query signatureQuery = entityManager.createQuery(
                        "select s from Signature s " +
                                "where s.accession in (:modelId) " +
                                "and s.signatureLibraryRelease.version = :signatureLibraryVersion " +
                                "and s.signatureLibraryRelease.library = :signatureLibraryName");
                signatureQuery.setParameter("signatureLibraryName", signatureLibraryName);
                signatureQuery.setParameter("signatureLibraryVersion", signatureLibraryVersion);
                signatureQuery.setParameter("modelId", modelAccessionSlice);

                // Retrieve the signatures and proteins and place them in Maps for quick lookup.
                signatureAccessionToSignatureMap.putAll (getSignatureAccessionToSignatureMap (signatureQuery));
            }

            LOGGER.debug("Retrieved Signatures and Proteins to link by matches");

            // Finally build and store the HmmerMatch objects.
            buildHmmerMatches(rawProteins, signatureAccessionToSignatureMap, proteinIdToProteinMap);
        }
        catch (Exception e){
            LOGGER.error("Exception thrown when attempting to store the post-processed matches for Pfam-A.", e);
            throw new IllegalStateException ("Caught Exception when storing post-processed matches", e);
        }
    }

    /**
     * Helper method to simplify a very complex transaction - creates Matches between Protein and Signature
     * objects based upon (filtered) raw match data.
     *
     * @param rawProteins   Raw analysis results
     * @param signatures    Signature accessions with corresponding Signature objects
     * @param proteins      Proteins accessions with corresponding Protein objects
     */
    private void buildHmmerMatches(Collection<RawProtein<PfamHmmer3RawMatch>> rawProteins,
                                   final Map<String, Signature> signatures,
                                   final Map<String, Protein> proteins){
        // Iterate over the filtered matches again and link the appropriate Signature and Protein objects.
        LOGGER.debug("In buildHmmerMatches method.");
        for (RawProtein<PfamHmmer3RawMatch> rawProtein : rawProteins){
            Protein protein = proteins.get(rawProtein.getProteinIdentifier());
            // TODO - This is DEFINITELY not the right course of action for InterProScan - may be a Protein that has not been seen before in I5.
            if (protein == null){
                throw new IllegalStateException ("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            // Convert raw matches to filtered matches
            LOGGER.debug ("About to call RawMatch.Listener()");
            Collection<Hmmer3Match> matches = Hmmer3RawMatch.getMatches(rawProtein.getMatches(),
                    new RawMatch.Listener() {
                        @Override public Signature getSignature(String modelAccession,
                                                                SignatureLibrary signatureLibrary,
                                                                String signatureLibraryRelease) {
                            Signature signature = signatures.get(modelAccession);
                            if (signature == null){
                                throw new NullPointerException ("Cannot store match with a signature that is " +
                                        "not in database [model accession=" + modelAccession + "]");
                            }
                            return signature;
                        }
                    }
            );
            // TODO: Add "addMatches()" method to Protein
            LOGGER.debug ("Added matches to protein object");
            for (Hmmer3Match m : matches)   {
                protein.addMatch(m);
            }
            entityManager.persist(protein);
        }
    }

    /**
     * Helper method that converts a List of Signature objects retrieved from a JQL query
     * into a Map of signature accession to Signature object.
     *
     * @param signatureQuery being the query to retrieve the Signatures.
     * @return a Map of signature accessions to Signature objects.
     */

    private Map<String, Signature> getSignatureAccessionToSignatureMap (Query signatureQuery){
        @SuppressWarnings("unchecked") List<Signature> signatures = signatureQuery.getResultList();
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("Number of signatures retrieved: " + signatures.size());
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

    private Map<String, Protein> getProteinIdToProteinMap (Query proteinQuery){
        @SuppressWarnings("unchecked") List<Protein> proteins = proteinQuery.getResultList();
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("Number of proteins retrieved: " + proteins.size());
        }
        Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>(proteins.size());
        for (Protein protein : proteins){
            proteinIdToProteinMap.put(protein.getId().toString(), protein);
        }
        return proteinIdToProteinMap;
    }
}
