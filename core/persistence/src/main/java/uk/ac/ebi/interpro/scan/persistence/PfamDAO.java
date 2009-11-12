package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.raw.PfamRawMatch;
//import uk.ac.ebi.interpro.scan.model.Protein;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 06-Nov-2009
 * Time: 14:05:56
 * To change this template use File | Settings | File Templates.
 */
public interface PfamDAO extends GenericDAO<PfamRawMatch, Long> {
     public PfamRawMatch getPfamMatchesByModel (String methodAc);

}
