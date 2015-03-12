package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.model.PhobiusFeatureType;
import uk.ac.ebi.interpro.scan.model.PhobiusMatch;

import java.util.Set;

/**
 * Interface defining the persistence method for PhobiusProtein objects
 * (the temporary objects used in Phobius file parsing)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public interface PhobiusFilteredMatchDAO extends GenericDAO<PhobiusMatch,  Long> {

    /**
     * the peristence method for PhobiusProtein objects
     * (the temporary objects used in Phobius file parsing)
     *
     * As there are only a restricted number of Phobius Signatures / Models
     * (as defined in the PhobiusFeatureType enum) all of the signature objects
     * are retrieved from the database prior to commencing parsing.
     *
     * If these are not found in the database, they are created and then used.
     *
     * @see PhobiusFeatureType
     * @param phobiusProteins being the Set of PhobiusProtein objects to
     * be transformed and persisted.
     */
    void persist (Set<PhobiusProtein> phobiusProteins);
}
