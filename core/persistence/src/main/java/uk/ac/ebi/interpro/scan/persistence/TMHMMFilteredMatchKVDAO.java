package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMProtein;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.util.Set;

/**
 * Interface defining the persistence method for TMHMM proteins.
 *
 * @author Gift Nuka
 *
 */
public interface TMHMMFilteredMatchKVDAO extends FilteredMatchKVDAO<TMHMMMatch, RawMatch>{

    void persist(Set<TMHMMProtein> proteins);
}
