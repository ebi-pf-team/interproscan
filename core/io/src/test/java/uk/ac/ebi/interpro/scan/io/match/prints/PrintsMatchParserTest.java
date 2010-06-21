package uk.ac.ebi.interpro.scan.io.match.prints;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Jun 21, 2010
 * Time: 1:30:14 PM
 *
 */

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.io.match.prints.parsemodel.PrintsProtein;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tests the PhobiusMatchParser, specifically looking at memory usage.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class PrintsMatchParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(PrintsMatchParserTest.class);

    private static final String TEST_FILE_PATH = "data/prints/5ht_mouse_results.txt";

    private static final String CUTOFF_FILE_PATH = "data/prints/FingerPRINTShierarchy.db";

    private float defaultCutOff = log10(1e-04);

    /**
     * Parses a (largish) file and outputs memory usage at the end of the parse.
     * @throws java.io.IOException
     */
    @Test
    public void testParserEfficiency() throws IOException {
        logMemUsage("Before parse: ");
        InputStream is = PrintsMatchParserTest.class.getClassLoader().getResourceAsStream(CUTOFF_FILE_PATH);
        Map evalCutoffs;
        try {
            evalCutoffs = readPrintsParsingFile(is, defaultCutOff);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to initialise Prints hierarchy file to determine cutoff values.");
        }
        is.close();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new IllegalStateException ("InterruptedException thrown by ParsePrintsOutputStep while having a snooze to allow NFS to catch up.");
        }
        InputStream isParse = PrintsMatchParserTest.class.getClassLoader().getResourceAsStream(TEST_FILE_PATH);
        PrintsMatchParser parser = new PrintsMatchParser();
        Set<PrintsProtein> results = parser.parse(isParse, TEST_FILE_PATH, evalCutoffs);
        isParse.close();
        logMemUsage("After parse: ");
        System.out.println("Protein count: " + results.size());
        for (PrintsProtein protein : results){
            System.out.println(protein.getMotifName() + " | " + protein.geteValue().toString() + " | " + protein.getGraphScan() + " | " + protein.getSeqEndPos() + " | " + protein.getSeqStartPos());
        }
    }

    private void logMemUsage(String prefix){
        if (LOGGER.isDebugEnabled()){
            System.gc();System.gc();System.gc();System.gc();
            System.gc();System.gc();System.gc();System.gc();
            System.gc();System.gc();System.gc();System.gc();
            System.gc();System.gc();System.gc();System.gc();
            System.gc();System.gc();System.gc();System.gc();
            LOGGER.debug(prefix + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024) + " MB.");
        }
    }

    public static Map<String, Object> readPrintsParsingFile(InputStream is, float defaultCutOff) throws IOException {
        BufferedReader fReader = new BufferedReader(new InputStreamReader(is));
        String printsFileCommentCharacter = "#";
        String in;
        Map<String, Object> ret = new HashMap<String, Object>();
        while ((in = fReader.readLine()) != null) {
             if (!in.startsWith(printsFileCommentCharacter)) {
                String[] line = in.split("\\|");
                float checkCutoff = log10(Double.parseDouble(line[2]));
                if (checkCutoff != defaultCutOff) {
                    ret.put(line[0], checkCutoff);
                }
            }
        }
        return ret;
    }

    public static float log10(double x) {
		return (float) (Math.log(x) / Math.log(10.0));
	}

}
