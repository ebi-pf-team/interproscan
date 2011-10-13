package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads the TSV output from the web service query, each output row becomes a Java object.
 * See {@see uk.ac.ebi.interpro.scan.web.biomart.StructuralMatchDataRecord}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
public class StructuralMatchDataResourceReader extends AbstractResourceReader<StructuralMatchDataRecord> {

    private static final Logger LOGGER = Logger.getLogger(StructuralMatchDataResourceReader.class.getName());

    private static final String HEADER_LINE = "PROTEIN_ACCESSION\tPROTEIN_ID\tPROTEIN_LENGTH\tMD5\tCRC64\tdatabase_name\tdomain_id\tclass_id\tpos_from\tpos_to";

    @Override
    protected StructuralMatchDataRecord createRecord(String line) {
        if (line == null || line.isEmpty())   {
            return null;
        }
        if (line.startsWith(HEADER_LINE)) {
            // Ignore first line of the output (column headers)
            return null;
        }
        else if (line.startsWith("PROTEIN_ACCESSION\t")) {
            // Looks like the column headers from the web service have changed to an un-expected format
            throw new IllegalStateException("Column heading line in un-expected format: " + line);
        }

        String proteinAc;
        String proteinId;
        int proteinLength;
        String md5;
        String crc64;
        String databaseName;
        String domainId;
        String classId;
        int posFrom;
        int posTo;

        Pattern pattern = Pattern.compile("\t");
        Scanner scanner = new Scanner(line).useDelimiter(pattern);

        proteinAc = scanner.next();
        proteinId = scanner.next();
        proteinLength = scanner.nextInt();
        md5 = scanner.next();
        crc64 = scanner.next();
        databaseName = scanner.next();
        domainId = scanner.next();
        classId = scanner.next();
        posFrom = scanner.nextInt();
        posTo = scanner.nextInt();

        return new StructuralMatchDataRecord(proteinAc, proteinId, proteinLength, md5, crc64,
                databaseName, domainId, classId,
                posFrom, posTo);
    }

}
