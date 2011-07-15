package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import uk.ac.ebi.interpro.scan.model.*;

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

    private Entry2SignaturesDAO entry2methodDAO;

    private Entry2GoDao entry2goDAO;

    private Entry2PathwayDAO entry2pathwayDAO;

    //Hibernate DAOs for persistence in the in-memory database
    private JdbcEntryDaoImpl entryDAO;
      */

//    private Entry2SignaturesDAO entry2signaturesDAO;

    private Map<String, Collection<String>> entry2SignaturesMap = null;
    private Map<String, Collection<GoXref>> entry2GoXrefsMap = null;
    private Map<String, Collection<PathwayXref>> entry2PathwayXrefsMap = null;

    private Set<Entry> entries = new HashSet<Entry>();

    public EntryRowCallbackHandler(Entry2SignaturesDAO entry2SignaturesDAO, Entry2GoDAO entry2GoDAO, Entry2PathwayDAO entry2PathwayDAO) {
        entry2SignaturesMap = entry2SignaturesDAO.getAllSignatures();
        entry2GoXrefsMap = entry2GoDAO.getAllGoXrefs();
        entry2PathwayXrefsMap = entry2PathwayDAO.getAllPathwayXrefs();
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

        // Prepare entry signatures
        Set<String> signatureAcs = (Set<String>)entry2SignaturesMap.get(entryAc);
        if (signatureAcs == null) {
            // TODO Throw exception?
            signatureAcs = new HashSet<String>();
        }
        // Lookup signatures (already in I5 database) from the signature accessions
        Set<Signature> signatures = new HashSet<Signature>(); // TODO

        // Prepare entry GO cross references
        Set<GoXref> goXrefs = (Set<GoXref>)entry2GoXrefsMap.get(entryAc);
        if (goXrefs == null) {
             goXrefs = new HashSet<GoXref>();
        }

        // Prepare entry pathway cross references
        Set<PathwayXref> pathwayXrefs = (Set<PathwayXref>)entry2PathwayXrefsMap.get(entryAc);
        if (pathwayXrefs == null) {
            pathwayXrefs = new HashSet<PathwayXref>();
        }

        // Now create the entry and attach the signatures, GO xrefs and pathway xrefs
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
