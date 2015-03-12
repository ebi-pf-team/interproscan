package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import java.util.Collection;
import java.util.List;

/**
 * DAO Interface for data access to the Xref table
 * (which contains nucleotide IDs).
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public interface NucleotideSequenceXrefDAO extends GenericDAO<NucleotideSequenceXref, Long> {

    /**
     * Returns a List of Xrefs that are not unique.
     *
     * @return a List of Xrefs that are not unique.
     */
    public List<String> getNonUniqueXrefs();
}
