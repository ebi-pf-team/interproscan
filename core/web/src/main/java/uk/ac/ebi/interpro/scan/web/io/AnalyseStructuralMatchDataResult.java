package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.model.MatchDataSource;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase;

import java.io.IOException;
import java.util.*;


/**
 * Analyse query results and construct a more understandable list of
 * {@link uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase} objects.
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
public class AnalyseStructuralMatchDataResult {

    private static final Logger LOGGER = Logger.getLogger(AnalyseStructuralMatchDataResult.class.getName());

    private final ResourceReader<StructuralMatchDataRecord> reader;

    private StructuralMatchDataRecord sampleStructuralMatch;

    public AnalyseStructuralMatchDataResult() {
        this(null);
    }

    public AnalyseStructuralMatchDataResult(ResourceReader<StructuralMatchDataRecord> reader) {
        this.reader = reader;
    }

    /**
     * Convert a collection of {@link StructuralMatchDataRecord} objects
     * into a list of {@link uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase} objects using
     * necessary business logic.
     *
     * @param resource Resource to parse
     * @return The list of simple structural matches, or NULL if nothing found
     */
    public Collection<SimpleStructuralDatabase> parseStructuralMatchDataOutput(Resource resource) {

        /*
         * Example output:
         *
         * PROTEIN_ACCESSION	PROTEIN_ID	PROTEIN_LENGTH	CRC64	database_name	domain_id	class_id	pos_from	pos_to	PROTEIN_FRAGMENT
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	CATH	1w91A01	2.60.40.1500	4	14	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	CATH	1w91A01	2.60.40.1500	362	446	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	CATH	1w91A01	2.60.40.1500	449	483	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	CATH	1w91A02	3.20.20.80	15	248	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	CATH	1w91A02	3.20.20.80	251	361	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	PDB	1w91B	1w91	1	248	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	PDB	1w91B	1w91	251	446	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	PDB	1w91B	1w91	449	504	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	PDB	1w91C	1w91	1	248	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	PDB	1w91C	1w91	251	446	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	PDB	1w91C	1w91	449	504	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	MODBASE	MB_Q9ZFM2	MB_Q9ZFM2	1	502	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	SCOP	d1w91a1	b.71.1.2	4	13	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	SCOP	d1w91a1	b.71.1.2	363	446	N
         * Q9ZFM2	XYNB_GEOSE	504	59518E75200A18B1	SCOP	d1w91a1	b.71.1.2	449	503	N
         * ...
         */
        Collection<StructuralMatchDataRecord> records;

        try {
            records = reader.read(resource);
        } catch (IOException e) {
            LOGGER.error("Could not read from query resource: " + resource.getDescription());
            e.printStackTrace();
            return null;
        }

        // Assumption: Query results are for one specific protein accession!
        // Therefore all output relates to the same protein.

        if (records == null || records.size() < 1) {
            LOGGER.info("No matches found in resource: " + resource.getDescription());
            return null;
        }

        return createStructuralMatchData(records);
    }

    /**
     * Convert a collection of {@link StructuralMatchDataRecord} objects into a collection of
     * {@link SimpleStructuralDatabase} objects.
     * @param records the structural match raw data
     * @return resulting collection that associates each member database with it's structural matches
     */
    public Collection<SimpleStructuralDatabase> createStructuralMatchData(Collection<StructuralMatchDataRecord> records) {
        String queryOutputText = "";
        String line = "";
        sampleStructuralMatch = null;
        String proteinAc = null;

        Map<String, SimpleStructuralDatabase> structuralMatchDatabases = new HashMap<String, SimpleStructuralDatabase>();
        for (StructuralMatchDataRecord record : records) {
            // Loop through query output one line at a time

            if (sampleStructuralMatch == null) {
                // Store an example of a structural match record for future use
                sampleStructuralMatch = record;
            }

            if (proteinAc == null) {
                // First line of the query results, initialise the protein information
                proteinAc = record.getProteinAc();
                //proteinId = record.getProteinId();
                //proteinLength = record.getProteinLength();
                //md5 = "N/A"; // TODO May need to get this from UAREAD in the future
                //crc64 = record.getCrc64();
            }

            String databaseName = record.getDatabaseName();
            String domainId = record.getDomainId();
            String classId = record.getClassId();
            Integer posFrom = record.getPosFrom();
            Integer posTo = record.getPosTo();
            SimpleLocation location = new SimpleLocation(posFrom, posTo);

            if (structuralMatchDatabases.containsKey(databaseName)) {
                // Structural match database already exists, just add to the collection of structural matches for the database
                SimpleStructuralDatabase structuralDatabase = structuralMatchDatabases.get(databaseName);
                structuralDatabase.addStructuralMatch(classId, domainId, location);
            }
            else {
                // New structural match database that needs initialising and adding to the map
                MatchDataSource databaseMetadata = MatchDataSource.parseName(databaseName);
                if (databaseMetadata != null) {
                    SimpleStructuralDatabase structuralDatabase = new SimpleStructuralDatabase(databaseMetadata);
                    structuralDatabase.addStructuralMatch(classId, domainId, location);
                    structuralMatchDatabases.put(databaseName, structuralDatabase);
                }
                else {
                    LOGGER.warn("No match data source found with name " + databaseName);
                }
            }

            queryOutputText += line + "\n";

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Query returned:\n" + queryOutputText);
        }
        return Collections.unmodifiableCollection(structuralMatchDatabases.values());
    }

    public StructuralMatchDataRecord getSampleStructuralMatch() {
        return sampleStructuralMatch;
    }
}
