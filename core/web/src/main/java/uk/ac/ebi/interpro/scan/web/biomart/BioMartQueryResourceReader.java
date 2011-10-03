package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads the TSV output from the BioMart query, each output row becomes a Java object.
 * See {@see BioMartQueryRecord}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
public class BioMartQueryResourceReader extends AbstractResourceReader<BioMartQueryRecord> {

    private static final Logger LOGGER = Logger.getLogger(BioMartQueryResourceReader.class.getName());

    @Override protected BioMartQueryRecord createRecord(String line) {
        if (line == null || line.isEmpty())   {
            return null;
        }
        String proteinAc;
        String proteinName;
        String methodAc;
        String methodName;
        String methodType;
        int posFrom;
        int posTo;
        String entryAc;
        String entryName;
        String entryType;

        Pattern pattern = Pattern.compile("\t");
        Scanner scanner = new Scanner(line).useDelimiter(pattern);

        proteinAc = scanner.next();
        proteinName = scanner.next();
        scanner.next(); // md5
        methodAc = scanner.next();
        methodName = scanner.next();
        methodType = scanner.next();
        posFrom = scanner.nextInt();
        posTo = scanner.nextInt();
            try {
        scanner.nextDouble(); // match score
            }
            catch (InputMismatchException e) {
                // Sometimes score is empty (e.g. PROSITE profiles)
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Signature " + methodAc + " has no match score, so ignoring");
                }
            }

        entryAc = null;
        try {
            entryAc = scanner.next();
            scanner.next(); // entry short name
            entryName = scanner.next();
            entryType = scanner.next();
        }
        catch (NoSuchElementException e) {
            // Un-integerated signature, has no associated InterPro entry
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Signature " + methodAc + " for protein " + proteinAc + " is un-integrated");
            }
            entryAc = "Unintegrated";
            entryName = null;
            entryType = null;
        }

        return new BioMartQueryRecord(proteinAc, proteinName,
                methodAc, methodName, methodType,
                posFrom, posTo,
                entryAc, entryName, entryType);
    }

}
