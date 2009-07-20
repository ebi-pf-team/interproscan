package uk.ac.ebi.interpro.scan.persistence;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.FilteredFingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.transactiontracking.RawTransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 20-Jul-2009
 * Time: 15:13:18
 * To change this template use File | Settings | File Templates.
 */
public interface FilteredFingerPrintsMatchDAO  extends GenericDAO<Match, Long> {


           /**
     * Retrieves a Protein object by primary key and also retrieves any associated cross references.
     * @param m being the query match hitting proteins.
     * @return The Protein List, with filtered finger print matches hitting the proteins
     */
    public List<Protein> getProteinsWithMatch (Match m);

}
