package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatch;

import java.io.IOException;
import java.util.*;


/**
* Analyse query results and construct a more understandable list of
* {@link uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatch} objects.
*
* @author  Matthew Fraser
* @version $Id$
*/
public class AnalyseStructuralMatchDataResult {

    private static final Logger LOGGER = Logger.getLogger(AnalyseStructuralMatchDataResult.class.getName());

    private final ResourceReader<StructuralMatchDataRecord> reader;

    public AnalyseStructuralMatchDataResult(ResourceReader<StructuralMatchDataRecord> reader) {
        this.reader = reader;
    }

    /**
     * Convert a collection of {@link StructuralMatchDataRecord} objects
     * into a list of {@link uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatch} objects using
     * necessary business logic.
     *
     * @param resource Resource to parse
     * @return The list of simple structural matches, or NULL if nothing found
     */
    public List<SimpleStructuralMatch> parseStructuralMatchDataOutput(Resource resource) {
        List<SimpleStructuralMatch> structuralMatches = null;
        String queryOutputText = "";
        String line = "";

        /*
         * Example output:
         *
         * PROTEIN_ACCESSION	PROTEIN_ID	PROTEIN_LENGTH	MD5	CRC64	database_name	domain_id	class_id	pos_from	pos_to
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	PDB	1jm7A	1jm7	1	110
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	CATH	1jm7A00	3.30.40.10	1	103
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	CATH	1oqaA00	3.40.50.10190	1755	1863
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	CATH	1t15A01	3.40.50.10190	1649	1755
         * ...
         */

        String proteinAc = null;
        //String proteinId;
        //int proteinLength;
        //String md5;
        //String crc64;
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

        structuralMatches = new ArrayList<SimpleStructuralMatch>();
        for (StructuralMatchDataRecord record : records) {
            // Loop through query output one line at a time

            if (proteinAc == null) {
                // First line of the query results, initialise the protein information
                proteinAc = record.getProteinAc();
                //proteinId = record.getProteinId();
                //proteinLength = record.getProteinLength();
                //md5 = record.getMd5();
                //crc64 = record.getCrc64();
            }

            String databaseName = record.getDatabaseName();
            String domainId = record.getDomainId();
            String classId = record.getClassId();
            Integer posFrom = record.getPosFrom();
            Integer posTo = record.getPosTo();

            SimpleStructuralMatch newStructuralMatch = new SimpleStructuralMatch(databaseName, domainId, classId);
            SimpleLocation newLocation = new SimpleLocation(posFrom, posTo);

            // Has this structural match already been added to the list?
            if (structuralMatches.contains(newStructuralMatch)) {
                // Structural match already exists, just add the location
                SimpleStructuralMatch structuralMatch = structuralMatches.get(structuralMatches.indexOf(newStructuralMatch));
                structuralMatch.addLocation(newLocation);
            }
            else {
                // New structural match that needs creating and adding to the list
                newStructuralMatch.addLocation(newLocation);
                structuralMatches.add(newStructuralMatch);
            }

            queryOutputText += line + "\n";

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Query returned:\n" + queryOutputText);
        }

        return structuralMatches;
    }

}
