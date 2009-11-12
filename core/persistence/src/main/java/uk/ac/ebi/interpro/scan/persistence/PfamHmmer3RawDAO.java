package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
//import uk.ac.ebi.interpro.scan.model.Protein;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 06-Nov-2009
 * Time: 14:05:56
 * To change this template use File | Settings | File Templates.
 */
public interface PfamHmmer3RawDAO extends GenericDAO<PfamHmmer3RawMatch, Long> {
     public PfamHmmer3RawMatch getPfamMatchesByModel (String methodAc);

}
