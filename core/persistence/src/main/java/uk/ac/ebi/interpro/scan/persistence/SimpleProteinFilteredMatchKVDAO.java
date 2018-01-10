package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMProtein;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.util.Set;

/**
 * Interface defining the persistence method for TMHMM, Phobus and other  proteins.
 *
 * @author Gift Nuka
 * @since 1.0
 */
public interface SimpleProteinFilteredMatchKVDAO<P, U extends Match> extends FilteredMatchKVDAO<U, RawMatch>  {

    void persist(Set<P> proteins);
}
