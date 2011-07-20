package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.EntryDAO;
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

    private Set<Entry> entries = new HashSet<Entry>();

    // Populated from the InterPro database
    private Map<String, Collection<String>> entry2SignaturesMap = null;
    private Map<String, Collection<GoXref>> entry2GoXrefsMap = null;
    private Map<String, Collection<PathwayXref>> entry2PathwayXrefsMap = null;

    // I5 model DAOs
    private EntryDAO entryDAO;
    private SignatureDAO signatureDAO;

    public EntryRowCallbackHandler(Entry2SignaturesDAO entry2SignaturesDAO,
                                   Entry2GoDAO entry2GoDAO,
                                   Entry2PathwayDAO entry2PathwayDAO,
                                   EntryDAO entryDAO,
                                   SignatureDAO signatureDAO) {
        this.entry2SignaturesMap = entry2SignaturesDAO.getAllSignatures();
        this.entry2GoXrefsMap = entry2GoDAO.getAllGoXrefs();
        this.entry2PathwayXrefsMap = entry2PathwayDAO.getAllPathwayXrefs();
        this.entryDAO = entryDAO;
        this.signatureDAO = signatureDAO;
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
        Set<Signature> signatures = null;
        Set<String> signatureAcs = (Set<String>)entry2SignaturesMap.get(entryAc);
        if (signatureAcs == null) {
            // TODO Throw exception?
            signatureAcs = new HashSet<String>();
        }
        else {
            // Lookup signatures (already in I5 database) from the signature accessions
            signatures = (Set<Signature>)signatureDAO.getSignaturesAndMethodsDeep(signatureAcs);
            if (signatures == null || signatures.size() < 1) {
                log.error("Signatures could not be found in the database: " + signatureAcs.toString());
                throw new IllegalStateException("No signatures for entry " + entryAc + " could be found in the database.");
            }
        }

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
            //entryDAO.insert(entries);
            entries.clear(); // Free up some resources now these results have been inserted

        }

    }

    /**
     * Insert any entries currently in memory into the database (and clear the list of entries).
     */
    public void processFinalRows() {
        if (entries != null && entries.size() > 0) {
            // Batch insert the remaining entries into the DB
            //entryDAO.insert(entries);
            entries.clear(); // Clear now these results have been inserted (just incase this method is accidentally called again)!
        }
    }
}
