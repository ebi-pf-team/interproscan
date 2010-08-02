package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.*;

/**
 * Class factoring out most of the commmon code required to persist a Collection of RawProtein objects that have
 * been filtered, ready to be persisted as "proper" matches.
 * <p/>
 * Implementations just have to implement a method where the Protein objects and Signature objects
 * for these raw matches have already been achieved - implementations just need to link them together properly!?
 *
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public abstract class FilteredMatchDAOImpl<T extends RawMatch, U extends Match> extends GenericDAOImpl<U, Long> implements FilteredMatchDAO<T, U> {

    private static final Logger LOGGER = Logger.getLogger(FilteredMatchDAOImpl.class.getName());

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     *
     * @param modelClass the model that the DOA instance handles.
     */
    public FilteredMatchDAOImpl(Class<U> modelClass) {
        super(modelClass);
    }

    /**
     * Persists filtered protein matches.
     *
     * @param filteredProteins Filtered protein matches.
     */
    public void persist(Collection<RawProtein<T>> filteredProteins) {
        if (filteredProteins == null || filteredProteins.size() == 0) {
            LOGGER.debug("No RawProtein objects have been passed into the persistFilteredMatches method, so exiting.");
            return;
        }

        String signatureLibraryRelease = null;
        SignatureLibrary signatureLibrary = null;
        int rawMatchCount = 0;
        for (RawProtein<T> rawProtein : filteredProteins) {
            for (T rawMatch : rawProtein.getMatches()) {
                rawMatchCount++;
                if (signatureLibraryRelease == null) {
                    signatureLibraryRelease = rawMatch.getSignatureLibraryRelease();
                    if (signatureLibraryRelease == null) {
                        throw new IllegalStateException("Found a raw match record that does not include the release version");
                    }
                } else if (!signatureLibraryRelease.equals(rawMatch.getSignatureLibraryRelease())) {
                    throw new IllegalStateException("Attempting to persist a collection of filtered matches for more than one SignatureLibraryRelease.   Not implemented.");
                }
                if (signatureLibrary == null) {
                    signatureLibrary = rawMatch.getSignatureLibrary();
                    if (signatureLibrary == null) {
                        throw new IllegalStateException("Found a raw match record that does not include the SignatureLibrary.");
                    }
                } else if (signatureLibrary != (rawMatch.getSignatureLibrary())) {
                    throw new IllegalStateException("Attempting to persist a Collection of filtered matches for more than one SignatureLibrary.");
                }
            }
        }
        LOGGER.debug(rawMatchCount + " filtered matches have been passed in to the persistFilteredMatches method");
        if (signatureLibraryRelease == null) {
            LOGGER.debug("There are no raw matches to filter.");
            return;
        }

        final Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(filteredProteins);
        final Map<String, Signature> modelIdToSignatureMap = getModelAccessionToSignatureMap(signatureLibrary, signatureLibraryRelease);
        persist(filteredProteins, modelIdToSignatureMap, proteinIdToProteinMap);
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins             being the Collection of filtered RawProtein objects to persist
     * @param modelAccessionToSignatureMap a Map of model accessions to Signature objects.
     * @param proteinIdToProteinMap        a Map of Protein IDs to Protein objects
     */
    @Transactional
    protected abstract void persist(Collection<RawProtein<T>> filteredProteins,
                                    final Map<String, Signature> modelAccessionToSignatureMap,
                                    final Map<String, Protein> proteinIdToProteinMap);

    /**
     * Helper method to retrieve a Map for lookup of Signature
     * objects by signature accession.
     *
     * @param signatureLibrary        being the SignatureLibrary in this analysis.
     * @param signatureLibraryRelease the current version of the signature library in this analysis.
     * @return
     */
    @Transactional
    private Map<String, Signature> getModelAccessionToSignatureMap(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        final Query query =
                entityManager.createQuery(
                        "select r from SignatureLibraryRelease r " +
                                "where r.version = :version " +
                                "and r.library = :signatureLibrary");
        query.setParameter("signatureLibrary", signatureLibrary);
        query.setParameter("version", signatureLibraryRelease);
        @SuppressWarnings("unchecked") List<SignatureLibraryRelease> releaseList = query.getResultList();
        if (releaseList.size() == 0) {
            throw new IllegalStateException("No SignatureLibraryRelease found for "
                    + signatureLibrary.getName() + " " + signatureLibraryRelease);
        }
        if (releaseList.size() > 1) {
            throw new IllegalStateException("More than one SignatureLibraryRelease found for "
                    + signatureLibrary.getName() + " " + signatureLibraryRelease);
        }
        Set<Signature> signatures = releaseList.get(0).getSignatures();
        Map<String, Signature> map = new HashMap<String, Signature>(signatures.size());
        for (Signature s : signatures) {
            for (Model m : s.getModels().values()) {
                map.put(m.getAccession(), s);
            }
        }
        return map;
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
    private Map<String, Protein> getProteinIdToProteinMap(Collection<RawProtein<T>> rawProteins) {
        final Map<String, Protein> proteinIdToProteinMap = new HashMap<String, Protein>(rawProteins.size());

        final List<Long> proteinIds = new ArrayList<Long>(rawProteins.size());
        for (RawProtein<T> rawProtein : rawProteins) {
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
