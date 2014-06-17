package uk.ac.ebi.interpro.scan.io.pirsf.hmmer3;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Test the parsing of the output from the Hmmer3 based pirsfl.pl binary.
 */
public class PirsfPlResultParserTest  extends TestCase {

    private static final String TEST_FILE_NAME = "src/test/resources/data/pirsf/hmmer3/pirsl.pl.out";

    private static final String TEST_RELEASE_VERSION = "2.85";

    @Test
    public void testParsePirsfPlResults() throws IOException {

        // Set expected values
        Map<String, Integer> numMatchesMap = new HashMap<String, Integer>();
        numMatchesMap.put("A0B5N6", 2);
        numMatchesMap.put("A0B649", 1);
        numMatchesMap.put("A0B6J9", 1);

        PirsfHmmer3RawMatchParser parser = new PirsfHmmer3RawMatchParser();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(TEST_FILE_NAME);

            // Run test method
            Set<RawProtein<PirsfHmmer3RawMatch>> actualResults = parser.parse(inputStream);
            // Compare actual result with expected result
            Assert.assertEquals(3, actualResults.size());
            for (RawProtein<PirsfHmmer3RawMatch> a : actualResults) {
                String proteinId = a.getProteinIdentifier();
                if (numMatchesMap.containsKey(proteinId)) {
                    Assert.assertEquals((int)numMatchesMap.get(proteinId), (int)a.getMatches().size());
                }
                else {
                    Assert.fail("Unkown protein " + proteinId + " found in input file " + TEST_FILE_NAME);
                }
                System.out.println("Protein ID: " + a.getProteinIdentifier() +
                        ", DB ID: " + a.getProteinDatabaseId() + ", number of matches: " + a.getMatches().size());
                for (PirsfHmmer3RawMatch match : a.getMatches()) {
                    System.out.println(match.toString());
                }
            }
        }
        catch (FileNotFoundException e) {
            Assert.fail("Test input file not found: " + TEST_FILE_NAME);
        }
        catch (IOException e) {
            Assert.fail("I/O exception reading test input file: " + TEST_FILE_NAME);
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }
}
