package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.SignatureDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    private Map<String, Set<Signature>> entry2SignaturesMap = null;
    private Map<String, Set<GoXref>> entry2GoXrefsMap = null;
    private Map<String, Set<PathwayXref>> entry2PathwayXrefsMap = null;

    private Set<Entry> entries = new HashSet<Entry>();

    public EntryRowCallbackHandler() {
        entry2SignaturesMap = new HashMap<String, Set<Signature>>(); // signatureDao.getSignatures();
        entry2GoXrefsMap = new HashMap<String, Set<GoXref>>();
        entry2PathwayXrefsMap = new HashMap<String, Set<PathwayXref>>();
    }

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
        Set<Signature> signatures = entry2SignaturesMap.get(entryAc);
        if (signatures == null) {
            // TODO Throw exception?
            signatures = new HashSet<Signature>();
        }

        Set<GoXref> goXrefs = entry2GoXrefsMap.get(entryAc);
        if (goXrefs == null) {
             goXrefs = new HashSet<GoXref>();
        }

        Set<PathwayXref> pathwayXrefs = entry2PathwayXrefsMap.get(entryAc);
        if (pathwayXrefs == null) {
            pathwayXrefs = new HashSet<PathwayXref>();
        }

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
