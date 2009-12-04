package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.BlastProDomMatch;

import java.util.List;

/**
 * TODO: Add class description
 *
 * @author  Manjula Thimma
 * @version $Id$
 */
public interface BlastProDomLocationDAO extends GenericDAO<BlastProDomMatch.BlastProDomLocation, Long> {

    /**
     * Retrieves a Blast ProDom Location by score.
     * @param score being the score value of one or more ProDom Location.
     * @return The BlastProDomLocation, A list of BPL objects with a particular score.
     * 
     */
     public List<BlastProDomMatch.BlastProDomLocation> getBlastProDomHitLocationByScore(Double score);




}
