package uk.ac.ebi.interpro.scan.business.postprocessing.prints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;



import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class PrintsPostProcessingTest {

    @Resource
    private PrintsPostProcessing postProcessor;

    @Resource
    private org.springframework.core.io.Resource testData;

    @Test
    public void testPostProcessing() throws IOException {
        List<TestResult> referenceData = loadReferenceResults();
        final Map<String, RawProtein<PrintsRawMatch>> parseInput = new HashMap<String, RawProtein<PrintsRawMatch>>();
        // Build input
        for (TestResult testResult : referenceData) {
            final PrintsRawMatch rawMatch = testResult.getMatch();
            if (!parseInput.containsKey(rawMatch.getSequenceIdentifier())) {
                parseInput.put(rawMatch.getSequenceIdentifier(), new RawProtein<PrintsRawMatch>(rawMatch.getSequenceIdentifier()));
            }
            parseInput.get(rawMatch.getSequenceIdentifier()).addMatch(rawMatch);
        }
        assertEquals(20, parseInput.size());

        // Post process.
        final Map<String, RawProtein<PrintsRawMatch>> parseOutput = postProcessor.process(parseInput);
        assertNotNull(parseOutput);
        assertTrue(parseOutput.size() > 0);
        final List<TestResult> filteredTestResults = new ArrayList<TestResult>();

        // Go through reference data and check that only / all passing results are included in the output.
        for (String sequenceId : parseOutput.keySet()) {
            RawProtein<PrintsRawMatch> rawProtein = parseOutput.get(sequenceId);
            assertNotNull(rawProtein);
            assertEquals(sequenceId, rawProtein.getProteinIdentifier());
            for (PrintsRawMatch rawMatch : rawProtein.getMatches()) {
                assertNotNull(rawMatch);
                assertEquals(sequenceId, rawMatch.getSequenceIdentifier());
                boolean foundInTestSet = false;
                for (TestResult testResult : referenceData) {
                    if (testResult.getMatch().equals(rawMatch)) {
                        foundInTestSet = true;
                        assertTrue(testResult.passes());
                        filteredTestResults.add(testResult);
                    }
                }
                assertTrue(foundInTestSet);
            }
        }

        // Test that all of the TestResults that should have passed, did.
        for (TestResult testResult : referenceData) {
            if (testResult.passes()) {
                assertTrue(filteredTestResults.contains(testResult));
            } else {
                // This is kind of redundant as already tested, but why not eh?
                assertFalse(filteredTestResults.contains(testResult));
            }
        }
    }


    private List<TestResult> loadReferenceResults() throws IOException {
        List<TestResult> testResults = new ArrayList<TestResult>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(testData.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    String[] bits = line.split("\\t");
                    testResults.add(
                            new TestResult(
                                    "true".equals(bits[0]),
                                    new PrintsRawMatch(
                                            bits[1],
                                            bits[2],
                                            bits[3],
                                            Integer.parseInt(bits[4]),
                                            Integer.parseInt(bits[5]),
                                            Double.parseDouble(bits[6]),
                                            bits[7],
                                            Integer.parseInt(bits[8]),
                                            Integer.parseInt(bits[9]),
                                            Double.parseDouble(bits[10]),
                                            Double.parseDouble(bits[11])
                                    )
                            )
                    );
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return testResults;
    }

    private class TestResult {

        private boolean passes;

        private PrintsRawMatch match;

        private TestResult(boolean passes, PrintsRawMatch match) {
            this.passes = passes;
            this.match = match;
        }

        public boolean passes() {
            return passes;
        }

        public PrintsRawMatch getMatch() {
            return match;
        }
    }

}
