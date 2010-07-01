package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.FingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.*;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PrintsFilteredMatchDAOImpl extends GenericDAOImpl<FingerPrintsMatch, Long> implements PrintsFilteredMatchDAO {

    private static final Logger LOGGER = Logger.getLogger(PrintsFilteredMatchDAOImpl.class);

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public PrintsFilteredMatchDAOImpl() {
        super(FingerPrintsMatch.class);
    }

    /**
     * Persists filtered matches to the database that are referenced
     * from a RawProtein<PrintsRawMatch> object.
     *
     * @param rawProteins containing a Collection of filtered PrintsRawMatch objects
     */
    @Override
    public void persistFilteredMatches(Collection<RawProtein<PrintsRawMatch>> rawProteins) {


        if (rawProteins == null || rawProteins.size() == 0) {
            LOGGER.debug("No RawProtein objects have been passed into the persistFilteredMatches method, so exiting.");
            return;
        }

        String signatureLibraryVersion = null;
        int rawMatchCount = 0;
        for (RawProtein<PrintsRawMatch> rawProtein : rawProteins) {
            for (PrintsRawMatch rawMatch : rawProtein.getMatches()) {
                rawMatchCount++;
                if (signatureLibraryVersion == null) {
                    signatureLibraryVersion = rawMatch.getSignatureLibraryRelease();
                    if (signatureLibraryVersion == null) {
                        throw new IllegalStateException("Found a PRINTS raw match record that does not include the release version");
                    }
                } else if (!signatureLibraryVersion.equals(rawMatch.getSignatureLibraryRelease())) {
                    throw new IllegalStateException("Attempting to persist a collection of PRINTS matches for more than one SignatureLibraryRelease.   Not implemented.");
                }
            }
        }
        LOGGER.debug(rawMatchCount + " filtered matches have been passed in to the persistFilteredMatches method");
        if (signatureLibraryVersion == null) {
            LOGGER.debug("There are no raw matches to filter.");
            return;
        }

        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(rawProteins);
        Map<String, Signature> modelIdToSignatureMap = getModelAccessionToSignatureMap(rawProteins, signatureLibraryVersion);
        for (RawProtein<PrintsRawMatch> rawProtein : rawProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            Set<FingerPrintsMatch.FingerPrintsLocation> locations = null;
            String currentSignatureAc = null;
            Signature currentSignature = null;
            PrintsRawMatch currentRawMatch = null;
            for (PrintsRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) continue;
                currentRawMatch = rawMatch;
                if (currentSignatureAc == null || !currentSignatureAc.equals(rawMatch.getModel())) {
                    if (currentSignatureAc != null) {
                        // Not the first...
                        protein.addMatch(new FingerPrintsMatch(currentSignature, rawMatch.getEvalue(), rawMatch.getGraphscan(), locations));
                    }
                    // Reset everything
                    locations = new HashSet<FingerPrintsMatch.FingerPrintsLocation>();
                    currentSignatureAc = rawMatch.getModel();
                    currentSignature = modelIdToSignatureMap.get(currentSignatureAc);
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find PRINTS signature " + currentSignatureAc + " in the database.");
                    }
                }
                locations.add(
                        new FingerPrintsMatch.FingerPrintsLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getPvalue(),
                                rawMatch.getScore(),
                                rawMatch.getMotifNumber()
                        )
                );

            }
            // Don't forget the last one!
            if (currentRawMatch != null) {
                protein.addMatch(new FingerPrintsMatch(currentSignature, currentRawMatch.getEvalue(), currentRawMatch.getGraphscan(), locations));
            }
            entityManager.persist(protein);
        }
    }

    private Map<String, Signature> getModelAccessionToSignatureMap(Collection<RawProtein<PrintsRawMatch>> rawProteins, String signatureLibraryVersion) {
        // Check that all the PrintsRawMatches passed in are for the same PRINTS release, then get
        // all of the Signatures.

        final Query signatureQuery = entityManager.createQuery(
                "select s from Signature s " +
                        "where s.signatureLibraryRelease.version = :signatureLibraryVersion " +
                        "and s.signatureLibraryRelease.library = :signatureLibrary");
        signatureQuery.setParameter("signatureLibrary", SignatureLibrary.PRINTS);
        signatureQuery.setParameter("signatureLibraryVersion", signatureLibraryVersion);

        @SuppressWarnings("unchecked") List<Signature> signatures = signatureQuery.getResultList();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Number of signatures retrieved: " + signatures.size());
        }
        Map<String, Signature> signatureAccessionToSignatureMap = new HashMap<String, Signature>(signatures.size());
        for (Signature signature : signatures) {
            signatureAccessionToSignatureMap.put(signature.getAccession(), signature);
        }
        return signatureAccessionToSignatureMap;

    }

    /**
     * Helper method that converts a List of Protein objects retrieved from a JQL query
     * into a Map of protein IDs to Protein objects.
     *
     * @param rawProteins being the Set of PhobiusProteins containing the IDs of the Protein objects
     *                    required.
     * @return a Map of protein IDs to Protein objects.
     */
    @Transactional
    private Map<String, Protein> getProteinIdToProteinMap(Collection<RawProtein<PrintsRawMatch>> rawProteins) {
        final Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>(rawProteins.size());

        final List<Long> proteinIds = new ArrayList<Long>(rawProteins.size());
        for (RawProtein<PrintsRawMatch> rawProtein : rawProteins) {
            String proteinIdAsString = rawProtein.getProteinIdentifier();
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
