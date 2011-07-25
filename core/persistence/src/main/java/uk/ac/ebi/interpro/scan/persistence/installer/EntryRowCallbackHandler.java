package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.EntryDAO;
import uk.ac.ebi.interpro.scan.persistence.ReleaseDAO;
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

    private final int BATCH_COMMIT_SIZE = 5;

    private int entryCounter = 0;
    private Set<Entry> entries = new HashSet<Entry>();

    /* InterPro release Id */
    private Release interProRelease;
    private Long interProReleaseId;

    // Populated from the InterPro database
    private Map<String, Collection<String>> entry2SignaturesMap;
    private Map<String, Collection<GoXref>> entry2GoXrefsMap;
    private Map<String, Collection<PathwayXref>> entry2PathwayXrefsMap;

    // I5 model Hibernate DAOs
    private EntryDAO entryDAO;
    private SignatureDAO signatureDAO;
    private ReleaseDAO releaseDAO;

    //Installer JDBC DAOs
    private Entry2SignaturesDAO entry2SignaturesDAO;
    private Entry2GoDAO entry2GoDAO;
    private Entry2PathwayDAO entry2PathwayDAO;

    @Required
    public void setEntryDAO(EntryDAO entryDAO) {
        this.entryDAO = entryDAO;
    }

    @Required
    public void setSignatureDAO(SignatureDAO signatureDAO) {
        this.signatureDAO = signatureDAO;
    }

    @Required
    public void setReleaseDAO(ReleaseDAO releaseDAO) {
        this.releaseDAO = releaseDAO;
    }

    @Required
    public void setEntry2SignaturesDAO(Entry2SignaturesDAO entry2SignaturesDAO) {
        this.entry2SignaturesDAO = entry2SignaturesDAO;
    }

    @Required
    public void setEntry2GoDAO(Entry2GoDAO entry2GoDAO) {
        this.entry2GoDAO = entry2GoDAO;
    }

    @Required
    public void setEntry2PathwayDAO(Entry2PathwayDAO entry2PathwayDAO) {
        this.entry2PathwayDAO = entry2PathwayDAO;
    }

    protected EntryRowCallbackHandler() {
    }

    @Override
    public void processRow(ResultSet resultSet) throws SQLException {
        // Get query row result
        final String entryAc = resultSet.getString(1);
        final String entryType = resultSet.getString(2);
        final String description = resultSet.getString(3);
//        final String checked = resultSet.getString(4);
//        final Date created = resultSet.getDate(5);
//        final Date updated = resultSet.getDate(6);
//        final String userStamp = resultSet.getString(7);
        final String name = resultSet.getString(8);

        EntryType type = null;
        if (entryType != null && entryType.length() > 0) {
            type = EntryType.parseCode(entryType.charAt(0));
        }

        // Prepare entry 2 GO cross references
        Set<GoXref> goXrefs = (Set<GoXref>) getEntry2GoXrefsMap().get(entryAc);
        if (goXrefs == null) {
            goXrefs = new HashSet<GoXref>();
        }

        // Prepare entry 2 pathway cross references
        Set<PathwayXref> pathwayXrefs = (Set<PathwayXref>) getEntry2PathwayXrefsMap().get(entryAc);
        if (pathwayXrefs == null) {
            pathwayXrefs = new HashSet<PathwayXref>();
        }

        // Now create the entry and attach the signatures, GO xrefs and pathway xrefs
        Entry entry = buildEntry(entryAc, name, type, description, goXrefs, pathwayXrefs);
        Release release = getInterProRelease();
        entry.addRelease(release);

        // Prepare entry signatures
        Set<String> signatureAcs = (Set<String>) getEntry2SignaturesMap().get(entryAc);
        if (signatureAcs == null) {
            throw new IllegalStateException("Could not load any signature accession for entry accession - " + entryAc + " from external database!");
        }
        // Lookup signatures (already in I5 database) from the signature accessions
        // Attach them to the entry
        // Afterwards update signature in database
        Set<Signature> signatures = (Set<Signature>) signatureDAO.getSignaturesAndMethodsDeep(signatureAcs);
        if (signatures == null || signatures.size() < 1) {
            throw new IllegalStateException("Signatures could not be found in the database: " + signatureAcs.toString() + "No signatures for entry " + entryAc + " could be found in the database.");
        } else {
            for (Signature signature : signatures) {
                entry.addSignature(signature);
                signatureDAO.update(signature);
            }
        }
        entries.add(entry);

        //persistence step
        if (entries.size() == BATCH_COMMIT_SIZE) {
            log.info("Reached entry batch size.");
            log.info("Persisting entries...");
            // Batch insert into DB
            entryDAO.insert(entries);
            entries.clear(); // Free up some resources now these results have been inserted
            entryCounter += BATCH_COMMIT_SIZE;
            log.info("Processed already " + entryCounter + " entries.");
        }
    }

    private boolean checkIfDAOAreUsable() {
        if (entryDAO == null || signatureDAO == null || releaseDAO == null
                || entry2GoDAO == null || entry2PathwayDAO == null || entry2SignaturesDAO == null) {
            throw new IllegalStateException("One or some DAOs are not initialised successfully!");
        }
        return true;
    }

    private Entry buildEntry(String entryAc, String name, EntryType type, String description,
                             Set<GoXref> goXrefs, Set<PathwayXref> pathwayXrefs) {
        return new Entry.Builder(entryAc)
                .name(name)
                .type(type)
                .description(description)
                .goCrossReferences(goXrefs)
                .pathwayCrossReferences(pathwayXrefs)
                .build();
    }

    /**
     * Insert any entries currently in memory into the database (and clear the list of entries).
     */
    public void processFinalRows() {
        if (entries != null && entries.size() > 0) {
            // Batch insert the remaining entries into the DB
//            entryDAO.insert(entries);
            entries.clear(); // Clear now these results have been inserted (just incase this method is accidentally called again)!
        }
    }

    public void setInterProReleaseId(Long interProReleaseId) {
        this.interProReleaseId = interProReleaseId;
    }

    public Release getInterProRelease() {
        if (interProRelease == null && interProReleaseId != null) {
            interProRelease = releaseDAO.readDeep(interProReleaseId, "entries");
        }
        return interProRelease;
    }

    public Map<String, Collection<String>> getEntry2SignaturesMap() {
        if (entry2SignaturesMap == null) {
            if (checkIfDAOAreUsable()) {
                entry2SignaturesMap = entry2SignaturesDAO.getAllSignatures();
            } else {
                entry2SignaturesMap = new HashMap<String, Collection<String>>();
            }
        }
        return entry2SignaturesMap;
    }


    public Map<String, Collection<GoXref>> getEntry2GoXrefsMap() {
        if (entry2GoXrefsMap == null) {
            if (checkIfDAOAreUsable()) {
                entry2GoXrefsMap = entry2GoDAO.getAllGoXrefs();
                if (entry2GoXrefsMap == null) {
                    throw new RuntimeException("Could not load any entry 2 go mappings from external database!");
                }
            }
        }
        return entry2GoXrefsMap;
    }

    public Map<String, Collection<PathwayXref>> getEntry2PathwayXrefsMap() {
        if (entry2PathwayXrefsMap == null) {
            if (checkIfDAOAreUsable()) {
                entry2PathwayXrefsMap = entry2PathwayDAO.getAllPathwayXrefs();
            } else {
                entry2PathwayXrefsMap = new HashMap<String, Collection<PathwayXref>>();
            }
        }
        return entry2PathwayXrefsMap;
    }
}