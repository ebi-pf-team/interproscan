package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

/**
 * TODO: Add class description
 *
 * @author  Manjula Thimma
 * @version $Id$
 */
public interface PfamHmmer3RawMatchDAO extends GenericDAO<PfamHmmer3RawMatch, Long> {

     public PfamHmmer3RawMatch getPfamMatchesByModel (String methodAc);

}
