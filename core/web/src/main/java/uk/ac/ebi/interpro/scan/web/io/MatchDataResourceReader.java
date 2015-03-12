package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads the TSV output from the web service query, each output row becomes a Java object.
 * See {@see uk.ac.ebi.interpro.scan.web.biomart.MatchDataRecord}
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public class MatchDataResourceReader extends AbstractResourceReader<MatchDataRecord> {

    private static final Logger LOGGER = Logger.getLogger(MatchDataResourceReader.class.getName());

    private static final String HEADER_LINE = "PROTEIN_ACCESSION\tPROTEIN_ID\tPROTEIN_LENGTH\tCRC64\tMETHOD_AC\tMETHOD_NAME\tMETHOD_DATABASE_NAME\tPOS_FROM\tPOS_TO\tMATCH_SCORE\tENTRY_AC\tENTRY_SHORT_NAME\tENTRY_NAME\tENTRY_TYPE\tTAXONOMY_ID\tTAXONOMY_SCIENCE_NAME\tTAXONOMY_FULL_NAME";
    private static final String NO_RESULTS = "No results found";

    @Override
    protected MatchDataRecord createRecord(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        if (line.startsWith(HEADER_LINE)) {
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
        String methodAc;
        String methodName;
        String methodDatabase;
        int posFrom;
        int posTo;
        Double score;
        String entryAc;
        String entryShortName;
        String entryName;
        String entryType;
        int taxId;
        String taxScienceName;
        String taxFullName;
        boolean isProteinFragment = false;

        Pattern pattern = Pattern.compile("\t");
        Scanner scanner = new Scanner(line).useDelimiter(pattern);

        proteinAc = scanner.next();
        proteinId = scanner.next();
        proteinLength = scanner.nextInt();
        crc64 = scanner.next();
        methodAc = scanner.next();
        methodName = scanner.next();
        methodDatabase = scanner.next();
        posFrom = scanner.nextInt();
        posTo = scanner.nextInt();

        score = null;
        try {
            score = scanner.nextDouble();
        } catch (InputMismatchException e) {
            // Sometimes score is empty (e.g. PROSITE profiles)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Signature " + methodAc + " has no match score, so ignoring");
            }
        }

        entryAc = null;
        try {
            entryAc = scanner.next();
            entryShortName = scanner.next();
            entryName = scanner.next();
            entryType = scanner.next();
        } catch (NoSuchElementException e) {
            // Un-integerated signature, has no associated InterPro entry
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Signature " + methodAc + " for protein " + proteinAc + " is un-integrated");
            }
            entryShortName = SimpleEntry.UNINTEGRATED;
            entryName = SimpleEntry.UNINTEGRATED;
            entryType = null;
        }

        taxId = scanner.nextInt();
        taxScienceName = scanner.next();
        taxFullName = scanner.next();
        String isProteinFragmentString = scanner.next();
        if (isProteinFragmentString.equalsIgnoreCase("Y")) {
            isProteinFragment = true;
        }

        return new MatchDataRecord(proteinAc, proteinId, proteinDescription, proteinLength, crc64,
                methodAc, methodName, methodDatabase,
                posFrom, posTo, score,
                entryAc, entryShortName, entryName, entryType,
                taxId, taxScienceName, taxFullName, isProteinFragment);
    }

}
