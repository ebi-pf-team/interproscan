package uk.ac.ebi.interpro.scan.web.io;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads the TSV output from the web service query, each output row becomes a Java object.
 * See {@see uk.ac.ebi.interpro.scan.web.biomart.StructuralMatchDataRecord}
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public class StructuralMatchDataResourceReader extends AbstractResourceReader<StructuralMatchDataRecord> {

    private static final Logger LOGGER = LogManager.getLogger(StructuralMatchDataResourceReader.class.getName());

    private static final String HEADER_LINE = "PROTEIN_ACCESSION\tPROTEIN_ID\tPROTEIN_LENGTH\tCRC64\tdatabase_name\tdomain_id\tclass_id\tpos_from\tpos_to";
    private static final String NO_RESULTS = "No results found";

    @Override
    protected StructuralMatchDataRecord createRecord(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        } else if (line.startsWith(HEADER_LINE)) {
            // Ignore first line of the output (column headers)
            return null;
        } else if (line.startsWith("PROTEIN_ACCESSION\t")) {
            // Looks like the column headers from the web service have changed to an un-expected format
            throw new IllegalStateException("Column heading line in un-expected format: " + line);
        } else if (line.startsWith(NO_RESULTS)) {
            // No result to parse
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Do not parse line: " + line);
            }
            return null;
        }

        String proteinAc;
        String proteinId;
        String proteinDescription = "Name not available"; // Default when field not present
        int proteinLength;
        String crc64;
        String databaseName;
        String domainId;
        String classId;
        int posFrom;
        int posTo;
        boolean isProteinFragment = false;

        Pattern pattern = Pattern.compile("\t");
        Scanner scanner = new Scanner(line).useDelimiter(pattern);

        proteinAc = scanner.next();
        proteinId = scanner.next();
        proteinLength = scanner.nextInt();
        crc64 = scanner.next();
        databaseName = scanner.next();
        domainId = scanner.next();
        classId = scanner.next();
        posFrom = scanner.nextInt();
        posTo = scanner.nextInt();
        String isProteinFragmentString = scanner.next();
        if (isProteinFragmentString.equalsIgnoreCase("Y")) {
            isProteinFragment = true;
        }

        return new StructuralMatchDataRecord(proteinAc, proteinId, proteinDescription, proteinLength, crc64,
                databaseName, domainId, classId,
                posFrom, posTo, isProteinFragment);
    }

}
