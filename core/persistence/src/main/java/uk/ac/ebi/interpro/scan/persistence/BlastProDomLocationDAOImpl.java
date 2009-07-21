package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.BlastProDomLocation;
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 21-Jul-2009
 * Time: 14:25:10
 * To change this template use File | Settings | File Templates.
 */
public class BlastProDomLocationDAOImpl extends GenericDAOImpl<BlastProDomLocation, Long> implements BlastProDomLocationDAO{

    /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public BlastProDomLocationDAOImpl(){
        super(BlastProDomLocation.class);
    }

     /**
     * Retrieves a Protein object by primary key and also retrieves any associated cross references.
     * @param id being the primary key of the required Protein.
     * @return The Protein, with cross references loaded. (Xrefs are LAZY by default) or null if the
     * primary key is not present in the database.
     */
    @Transactional(readOnly = true)
    public Protein getProteinAndCrossReferencesByProteinId(Long id){
        Query query = entityManager.createQuery("select p from Protein p left outer join fetch p.crossReferences where p.id = :id");
        query.setParameter("id", id);
        return (Protein) query.getSingleResult();
    }

    /**
     * Retrieves a Protein object by primary key and also retrieves any associated matches.
     *
     * @param id being the primary key of the required Protein.
     * @return The Protein, with matches loaded. (matches are LAZY by default) or null if the
     *         primary key is not present in the database.
     */
    public Protein getProteinAndMatchesById(Long id) {
        Query query = entityManager.createQuery("select p from Protein p left outer join fetch p.filteredMatches left outer join fetch p.rawMatches where p.id = :id");
        query.setParameter("id", id);
        return (Protein) query.getSingleResult();
    }

    /**
     * Retrieves a List of Proteins that are part of the TransactionSlice passed in as argument.
     * TODO - Consider this very carefully.  If the TransactionSlice includes all the proteins in the database, this will make a nasty mess.
     * @param slice defining a Transaction.
     * @return a List of Proteins that are part of the TransactionSlice passed in as argument.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Protein> getProteinsInTransactionSlice(TransactionSlice slice) {
        Query query = entityManager.createQuery("select p from Protein p where p.id >= :bottom and p.id <= :top");
        query.setParameter("bottom", slice.getBottom());
        query.setParameter("top", slice.getTop());
        return (List<Protein>) query.getResultList();
    }

    public List<BlastProDomLocation> getBlastProDomHitLocationByScore(Double score) {
        
    }
    
}
