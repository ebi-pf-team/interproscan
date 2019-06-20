package uk.ac.ebi.interpro.scan.business.postprocessing.smart;

import org.apache.log4j.Logger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.HmmPfamParser;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;



/**
 * Tests {@link SmartPostProcessing}.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class SmartPostProcessingTest {

    private static final Logger LOGGER = Logger.getLogger(SmartPostProcessingTest.class.getName());

    @Resource
    private org.springframework.core.io.Resource hmmPfamOutput;

    @Resource
    private HmmPfamParser<SmartRawMatch> hmmPfamParser;

    @Resource
    private org.springframework.core.io.Resource hmmPfamFilteredMatches;

    @Resource
    private org.springframework.core.io.Resource rawMatches;

    @Resource
    private org.springframework.core.io.Resource filteredMatches;

    @Resource
    private SmartPostProcessing postProcessor;

    // Parse hmmerOutput using io class then run post-processing
    @Test
    @Disabled
    public void testParseAndFilter() throws IOException {

        // Read raw matches
        Set<RawProtein<SmartRawMatch>> proteins = hmmPfamParser.parse(hmmPfamOutput.getInputStream());
        final Map<String, RawProtein<SmartRawMatch>> rawProteins
                = new HashMap<String, RawProtein<SmartRawMatch>>();
        for (RawProtein<SmartRawMatch> p : proteins) {
            rawProteins.put(p.getProteinIdentifier(), p);
        }

        // Read expected filtered matches
        final Map<String, RawProtein<SmartRawMatch>> expectedFilteredProteins =
                new HashMap<String, RawProtein<SmartRawMatch>>(parseRawMatches(false, hmmPfamFilteredMatches));

        // Filter raw matches
        final Map<String, RawProtein<SmartRawMatch>> filteredProteins = postProcessor.process(rawProteins);

        // Check
        assertNotNull(expectedFilteredProteins);
        assertNotNull(filteredProteins);
        assertEquals(expectedFilteredProteins.size(), filteredProteins.size());
        for (String id : filteredProteins.keySet()) {
            RawProtein<SmartRawMatch> expectedProtein = expectedFilteredProteins.get(id);
            RawProtein<SmartRawMatch> filteredProtein = filteredProteins.get(id);
            assertEquals(expectedProtein, filteredProtein);
        }
    }

    @Test
    public void testFilter() throws IOException {

        // Read raw matches
        final Map<String, RawProtein<SmartRawMatch>> rawProteins =
                new HashMap<String, RawProtein<SmartRawMatch>>(parseRawMatches(true, rawMatches));

        // Read expected filtered matches
        final Map<String, RawProtein<SmartRawMatch>> expectedFilteredProteins =
                new HashMap<String, RawProtein<SmartRawMatch>>(parseRawMatches(false, filteredMatches));

        // Filter raw matches
        final Map<String, RawProtein<SmartRawMatch>> filteredProteins = postProcessor.process(rawProteins);

        // Check
        assertNotNull(filteredProteins);
        assertEquals(expectedFilteredProteins.size(), filteredProteins.size());
        assertEquals(expectedFilteredProteins, filteredProteins);
    }

    /**
     * When the overlapping and THRESHOLDS files are null then no filtering occurs. Raw matches become filtered matches.
     *
     * @throws IOException If a file cannot be read.
     */
    @Test
    public void testFilteringDisabled() throws IOException {

        // Read raw matches
        final Map<String, RawProtein<SmartRawMatch>> rawProteins =
                new HashMap<String, RawProtein<SmartRawMatch>>(parseRawMatches(true, rawMatches));

        // Read expected filtered matches
        final Map<String, RawProtein<SmartRawMatch>> expectedFilteredProteins =
                new HashMap<String, RawProtein<SmartRawMatch>>(parseRawMatches(false, filteredMatches));

        // Would filter raw matches, but overlapping and THRESHOLDS files are null therefore should skip post processing
        final Map<String, RawProtein<SmartRawMatch>> filteredProteins = postProcessor.process(rawProteins);


        // Check
        assertNotNull(filteredProteins);
        assertEquals(expectedFilteredProteins.size(), filteredProteins.size());
        assertEquals(expectedFilteredProteins, filteredProteins);
    }

    private Map<String, RawProtein<SmartRawMatch>> parseRawMatches(boolean isRawMatches, org.springframework.core.io.Resource f)
            throws IOException {
        final ResourceReader<SmartRawMatch> reader = new OnionResourceReader(isRawMatches);
        final Collection<SmartRawMatch> matches = reader.read(f);
        final Map<String, RawProtein<SmartRawMatch>> proteins = new HashMap<String, RawProtein<SmartRawMatch>>();
        for (SmartRawMatch m : matches) {
            String id = m.getSequenceIdentifier();
            RawProtein<SmartRawMatch> p;
            if (proteins.containsKey(id)) {
                p = proteins.get(id);
            } else {
                p = new RawProtein<SmartRawMatch>(id);
                proteins.put(id, p);
            }
            p.addMatch(m);
        }
        return proteins;
    }

    /**
     * Reads TSV exports of raw matches from Onion's SMART_ANALYSIS_C table
     */
    private static final class OnionResourceReader extends AbstractResourceReader<SmartRawMatch> {
        private final boolean isAnalysisTable;

        OnionResourceReader(boolean isAnalysisTable) {
            this.isAnalysisTable = isAnalysisTable;
        }

        @Override
        protected SmartRawMatch createRecord(String line) {
            // Remove quotes
            line = line.replaceAll("\"", "");
            // Ignore first row
            if (line.startsWith("ANALYSIS_TYPE_ID")) {
                return null;
            }
            Scanner s = new Scanner(line);
            try {

                // SMART_ANALYSIS_C:
                // "ANALYSIS_TYPE_ID"	"UPI"	"METHOD_AC"	"RELNO_MAJOR"	"RELNO_MINOR"	"SEQ_START"	"SEQ_END"	"HMM_START"	"HMM_END"	"HMM_BOUNDS"	"SCORE"	"SEQSCORE"	"TIMESTAMP"	"EVALUE"
                // 11	"UPI0000000030"	"SM00327"	6	1	1	155	1	193	"[]"	54.4	170	17-AUG-10	-11.07572078704834

                // IPRSCAN:
                // "ANALYSIS_TYPE_ID"	"UPI"	"METHOD_AC"	"RELNO_MAJOR"	"RELNO_MINOR"	"SEQ_START"	"SEQ_END"	"HMM_START"	"HMM_END"	"HMM_BOUNDS"	"SCORE"	"SEQSCORE"	"EVALUE"	"STATUS"	"TIMESTAMP"
                // 11	"UPI0000000030"	"SM00327"	6	1	537	718	1	193	"[]"	115.6	170	-29.494850158691406	"T"	01-SEP-10

                // Read columns
                String analysisId = s.next();
                String seqId = s.next();
                String modelId = s.next();
                String release = s.next() + "." + s.next();
                int locStart = s.nextInt();
                int locEnd = s.nextInt();
                int hmmStart = s.nextInt();
                int hmmEnd = s.nextInt();
                String hmmBounds = s.next();
                double locScore = s.nextDouble(); // "score" in Onion
                double score = s.nextDouble(); // "seqScore" in Onion
                if (isAnalysisTable) {
                    String timestamp = s.next();
                }
                double evalue = PersistenceConversion.get(s.nextDouble());
                //double locEvalue = PersistenceConversion.get(s.nextDouble());
                double locEvalue = evalue; // TODO: Location e-value not stored in Onion - do we need it?
                // TODO: No need for SignatureLibrary in raw match constructor
                return new SmartRawMatch(seqId, modelId, SignatureLibrary.SMART, release, locStart, locEnd, evalue, score,
                        hmmStart, hmmEnd, hmmBounds, locScore, locEvalue);
            } catch (InputMismatchException e) {
                LOGGER.error("Error reading line: " + line);
                throw e;
            }
        }
    }

}
