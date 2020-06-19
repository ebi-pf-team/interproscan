package uk.ac.ebi.interpro.scan.io.match.prints;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Jun 21, 2010
 * Time: 1:30:14 PM
 *
 */


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests the PhobiusMatchParser, specifically looking at memory usage.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class PrintsMatchParserTest  {

    private static final Logger LOGGER = LogManager.getLogger(PrintsMatchParserTest.class.getName());

    private static final String TEST_FILE_PATH = "data/prints/5ht_mouse_results.txt";

    private static final String CUTOFF_FILE_PATH = "data/prints/FingerPRINTShierarchy.db";

    private double defaultCutOff = Math.log10(1e-04);

    /**
     * Parses a (largish) file and outputs memory usage at the end of the parse.
     *
     * @throws java.io.IOException
     */

    @Test
    @Disabled("Needs to be reimplemented")
    public void testParserEfficiency() throws IOException {
        /*
        logMemUsage("BeforeAll parse: ");
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
        LOGGER.debug("Protein count: " + results.size());
        for (PrintsProtein protein : results){
            LOGGER.debug(protein.getMotifName() + " | " + protein.getEValue().toString() + " | " + protein.getGraphScan() + " | " + protein.getSeqEndPos() + " | " + protein.getSeqStartPos());
        }
        */
    }

    private void logMemUsage(String prefix) {
        if (LOGGER.isDebugEnabled()) {
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            LOGGER.debug(prefix + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB.");
        }
    }

    public static Map<String, Object> readPrintsParsingFile(InputStream is, float defaultCutOff) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String printsFileCommentCharacter = "#";
        String line;
        Map<String, Object> ret = new HashMap<String, Object>();
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith(printsFileCommentCharacter)) {
                String[] splitLine = line.split("\\|");
                double checkCutoff = Math.log10(Double.parseDouble(splitLine[2]));
                if (checkCutoff != defaultCutOff) {
                    ret.put(splitLine[0], checkCutoff);
                }
            }
        }
        return ret;
    }
}
