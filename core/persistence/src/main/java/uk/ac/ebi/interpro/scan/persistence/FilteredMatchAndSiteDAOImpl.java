package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;

import java.util.Collection;
import java.util.Map;

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

public abstract class FilteredMatchAndSiteDAOImpl<T extends RawMatch, U extends Match, E extends RawSite, K extends Site>
        extends FilteredMatchDAOImpl <T, U> implements FilteredMatchAndSiteDAO<T, U, E, K> {

    private static final Logger LOGGER = Logger.getLogger(FilteredMatchAndSiteDAOImpl.class.getName());

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
    public FilteredMatchAndSiteDAOImpl(Class<U> modelClass) {
        super(modelClass);
    }

    /**
     * Persists filtered protein matches.
     *
     * @param filteredProteins Filtered protein matches.
     */
    @Transactional
    public void persist(Collection<RawProtein<T>> filteredProteins, Collection<E> sites) {
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(rawMatchCount + " filtered matches have been passed in to the persistFilteredMatches method");
        }
        if (signatureLibraryRelease == null) {
            LOGGER.debug("There are no raw matches to filter.");
            return;
        }

        final Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(filteredProteins);
        final Map<String, Signature> modelIdToSignatureMap = getModelAccessionToSignatureMap(signatureLibrary, signatureLibraryRelease, filteredProteins);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("signatureLibrary: " + signatureLibrary
                    + " signatureLibraryRelease: " + signatureLibraryRelease
                    + " filteredProteins: " + filteredProteins.size()
                    + " modelIdToSignatureMap size: " + modelIdToSignatureMap.size());
        }


        StringBuilder signatureList = new StringBuilder();
        for (Signature signature:   modelIdToSignatureMap.values()){
            signatureList.append(signature.getModels().toString());
        }

        persist(filteredProteins, sites, modelIdToSignatureMap, proteinIdToProteinMap);

    }

    @Transactional
    public abstract void persist(Collection<RawProtein<T>> rawProteins, Collection<E> sites,
                                 Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap);
}
