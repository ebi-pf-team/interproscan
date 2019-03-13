package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMProtein;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;

import java.util.Set;

/**
 * Interface defining the persistence method for TMHMM proteins.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public interface TMHMMFilteredMatchDAOOld extends GenericDAO<TMHMMMatch, Long> {

    void persist(Set<TMHMMProtein> proteins);
}
