package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.BlastProDomLocation;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 21-Jul-2009
 * Time: 13:43:42
 * To change this template use File | Settings | File Templates.
 */
public interface BlastProdomLocationDAO extends GenericDAO<BlastProDomLocation, Long> {

    /**
     * Retrieves a Blast ProDom Location by score.
     * @param score being the score value of one or more ProDom Location.
     * @return The BlastProDomLocation, A list of BPL objects with a particular score.
     * 
     */
     public List<BlastProDomLocation> getBlastProDomHitLocationByScore(Double score);




}
