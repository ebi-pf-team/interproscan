package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;

import javax.persistence.Query;
import java.util.List;

/**
 * Implementation of DAO Interface for data access to the Xref table
 * (which contains nucleotide sequence IDs).
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public class NucleotideSequenceXrefDAOImpl extends GenericDAOImpl<NucleotideSequenceXref, Long> implements NucleotideSequenceXrefDAO {

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public NucleotideSequenceXrefDAOImpl() {
        super(NucleotideSequenceXref.class);
    }

    /**
     * Returns a List of Xrefs that are not unique.
     *
     * @return a List of Xrefs that are not unique.
     */
    @Transactional(readOnly = true)
    public List<String> getNonUniqueXrefs() {
        Query query = entityManager.createQuery(
                "select distinct a.identifier from NucleotideSequenceXref a, NucleotideSequenceXref b where a.id <> b.id and a.identifier = b.identifier"
        );
        return query.getResultList();
    }
}
