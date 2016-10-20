package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;

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

public interface FilteredMatchAndSiteDAO<T extends RawMatch, U extends Match, E extends RawSite, K extends Site>
        extends FilteredMatchDAO <T, U> {


    /**
     * Persists filtered protein matches.
     *
     * @param filteredProteins Filtered protein matches.
     */
    @Transactional
    void persist(Collection<RawProtein<T>> filteredProteins, Collection<E> sites);

    @Transactional
    default void persist(Collection<RawProtein<T>> rawProteins, Collection<E> sites,
                        Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        //if this class is called but not implemeneted, thow an exception
        throw new UnsupportedOperationException();
    }

}
