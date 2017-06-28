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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the whole entry loading process for the I5 installer (uk.ac.ebi.interpro.scan.jms.installer.Installer).
 * <p/>
 * Please note: This class might be not thread save!
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryRowCallbackHandler implements RowCallbackHandler {

    private static Logger log = Logger.getLogger(EntryRowCallbackHandler.class);

//    private final int BATCH_COMMIT_SIZE = 60;
    private final int BATCH_COMMIT_SIZE = 500;


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
        final String name = resultSet.getString(3);
        final String checked = resultSet.getString(4);
        if (!checked.equalsIgnoreCase("Y")) {
            log.warn("Entry " + entryAc + " is unchecked!");
        }
//        final Date created = resultSet.getDate(5);
//        final Date updated = resultSet.getDate(6);
//        final String userStamp = resultSet.getString(7);
        final String shortName = resultSet.getString(8);

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
        Entry entry = buildEntry(entryAc, shortName, type, name, goXrefs, pathwayXrefs);
        Release release = getInterProRelease();
        entry.addRelease(release);

        entries.add(entry);

        //persistence step
        if (entries.size() == BATCH_COMMIT_SIZE) {
            if (log.isInfoEnabled()) {
                log.info("Reached entry batch size of " + BATCH_COMMIT_SIZE + ".");
                log.info("Persisting batch of entries...");
            }
            insertEntries();
        }
    }

    private void insertEntries() {
        // Batch insert into DB
        int numEntries = entries.size();
        if (entryDAO == null) {
            throw new IllegalStateException("One or some DAOs are not initialised successfully!");
        }
        entries = (HashSet<Entry>) entryDAO.insert(entries);
        if (log.isInfoEnabled()) {
            log.info(numEntries + " entries persisted.");
        }
        addSignatures(entries);
        entries.clear(); // Free up some resources now these results have been inserted
        entryCounter += numEntries;
        printMemory();
        if (log.isInfoEnabled()) {
            log.info("----------------- Processed already " + entryCounter + " entries -----------------------");
        }
    }

    private void addSignatures(Set<Entry> entries) {
        log.info("Adding signatures to entries...");
        if (signatureDAO == null) {
            throw new IllegalStateException("One or some DAOs are not initialised successfully!");
        }
        Collection<Signature> batchOfSignatures = new HashSet<Signature>();
        for (Entry entry : entries) {
            // Prepare entry signatures
            Set<String> signatureAcs = (Set<String>) getEntry2SignaturesMap().get(entry.getAccession());
            if (signatureAcs == null) {
                throw new IllegalStateException("Could not load any signature accession for entry accession - " + entry.getAccession() + " from external database!");
            }
            // Lookup signatures (already in I5 database) from the signature accessions
            // Attach them to the entry
            // Afterwards update signature in database
            Set<Signature> signatures = signatureDAO.getSignatures(signatureAcs);
            if (signatures == null || signatures.size() < 1) {
                // Perhaps new signatures have been added to the InterPro database since the I5 signature database was populated?
                // Therefore the signature accession (from the InterPro db) cannot be found in the I5 db!?
                log.warn("Signatures could not be found in the database: " + signatureAcs.toString() + " No signatures for entry " + entry.getAccession() + " could be found in the database.");
                // TODO throw new IllegalStateException("Signatures could not be found in the database: " + signatureAcs.toString() + "No signatures for entry " + entry.getAccession() + " could be found in the database.");
            } else {
                for (Signature signature : signatures) {
                    entry.addSignature(signature);
                }
                batchOfSignatures.addAll(signatures);
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Updating the batch of added signatures (batch size: " + batchOfSignatures.size() + ")");
        }
        signatureDAO.update(batchOfSignatures);
        log.info("Finished adding of signatures.");
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
            log.info("Persisting the final few entries...");
            insertEntries();
        }
    }

    public void setInterProReleaseId(Long interProReleaseId) {
        this.interProReleaseId = interProReleaseId;
    }

    public Release getInterProRelease() {
        if (interProRelease == null && interProReleaseId != null) {
            if (releaseDAO == null) {
                throw new IllegalStateException("One or some DAOs are not initialised successfully!");
            }
//            interProRelease = releaseDAO.readDeep(interProReleaseId, "entries");
            interProRelease = releaseDAO.read(interProReleaseId);
        }
        return interProRelease;
    }

    public Map<String, Collection<String>> getEntry2SignaturesMap() {
        if (entry2SignaturesMap == null) {
            if (entry2SignaturesDAO == null) {
                throw new IllegalStateException("One or some DAOs are not initialised successfully!");
            }
            else {
                log.info("Loading entry to signature mappings...");
                entry2SignaturesMap = entry2SignaturesDAO.getAllSignatures();
                if (entry2SignaturesMap == null) {
                    throw new RuntimeException("Could not load any entry to signature mappings from external database!");
                }
                if (log.isInfoEnabled()) {
                    log.info(entry2SignaturesMap.size() + " mappings loaded.");
                }
                printMemory();
            }
        }
        return entry2SignaturesMap;
    }


    public Map<String, Collection<GoXref>> getEntry2GoXrefsMap() {
        if (entry2GoXrefsMap == null) {
            if (entry2GoDAO == null) {
                throw new IllegalStateException("One or some DAOs are not initialised successfully!");
            }
            else {
                log.info("Loading entry to go xrefs...");
                entry2GoXrefsMap = entry2GoDAO.getAllGoXrefs();
                if (entry2GoXrefsMap == null) {
                    throw new RuntimeException("Could not load any entry to go mappings from external database!");
                }
                if (log.isInfoEnabled()) {
                    log.info(entry2GoXrefsMap.size() + " mappings loaded.");
                }
                printMemory();
            }
        }
        return entry2GoXrefsMap;
    }

    public Map<String, Collection<PathwayXref>> getEntry2PathwayXrefsMap() {
        if (entry2PathwayXrefsMap == null) {
            if (entry2PathwayDAO == null) {
                throw new IllegalStateException("One or some DAOs are not initialised successfully!");
            }
            else {
                log.info("Loading entry to pathway xrefs...");
                entry2PathwayXrefsMap = entry2PathwayDAO.getAllPathwayXrefs();
                if (entry2PathwayXrefsMap == null) {
                    throw new RuntimeException("Could not load any entry to pathway mappings from external database!");
                }
                if (log.isInfoEnabled()) {
                    log.info(entry2PathwayXrefsMap.size() + " mappings loaded.");
                }
                printMemory();
            }
        }
        return entry2PathwayXrefsMap;
    }

    private void printMemory() {
        if (log.isDebugEnabled()) {
            long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            log.debug("Current memory usage: " + heap + " bytes (" + (heap / 131072 * 0.125) + " MB)");
        }
    }
}
