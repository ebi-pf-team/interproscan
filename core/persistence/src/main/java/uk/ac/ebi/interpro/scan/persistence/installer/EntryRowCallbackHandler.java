package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.SignatureDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryRowCallbackHandler implements RowCallbackHandler {

    private static Logger log = Logger.getLogger(EntryRowCallbackHandler.class);

    public static final int BATCH_COMMIT_SIZE = 50;

    //InterPro JDBC DAOs
    /*
    private JDBCEntryDao jdbcEntryDAO;

    private Entry2MethodDAO entry2methodDAO;

    private Entry2GoDao entry2goDAO;

    private Entry2PathwayDAO entry2pathwayDAO;

    //Hibernate DAOs for persistence in the in-memory database
    private EntryDaoImpl entryDAO;
      */

    private SignatureDAO signatureDAO;

    private Set<Entry> entries = new HashSet<Entry>();

    @Override
    public void processRow(ResultSet resultSet) throws SQLException {

        // Get query row result
        final String entryAc = resultSet.getString(1);
        final String entryType = resultSet.getString(2);
        final String description = resultSet.getString(3);
        final String checked = resultSet.getString(4);
        final Date created = resultSet.getDate(5);
        final Date updated = resultSet.getDate(6);
        final String userStamp = resultSet.getString(7);
        final String name = resultSet.getString(8);

        EntryType type = null;
        if(entryType!=null && entryType.length()>0){
            type = EntryType.parseCode(entryType.charAt(0));
        }

        // Create Entry object
        Set<Signature> signatures = new HashSet<Signature>(); // TODO Populate for this entry!
        Set<GoXref> goXrefs = new HashSet<GoXref>(); // TODO Populate for this entry!
        Set<PathwayXref> pathwayXrefs = new HashSet<PathwayXref>(); // TODO Populate for this entry!
        final Entry entry = new Entry(entryAc, name, type, description, null, null, signatures, goXrefs, pathwayXrefs);

        entries.add(entry);

        //persistence step
        if (entries.size() == BATCH_COMMIT_SIZE) {
            // Batch insert into DB

        }

    }

    public void processFinalRows() {
        // TODO
    }
}
