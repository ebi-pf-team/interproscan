package uk.ac.ebi.interpro.scan.persistence.installer;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.persistence.ModelDAO;

/**
 * TODO: Description
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryDAOImpl extends GenericDAOImpl<Entry, Long> implements EntryDAO {


    public EntryDAOImpl() {
        super(Entry.class);
    }
}