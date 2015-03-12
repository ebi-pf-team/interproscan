package uk.ac.ebi.interpro.scan.business.filter;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * Tests {@link Gene3dRawMatchFilter}.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public final class Gene3dRawMatchFilterTest {

    private static final Logger LOGGER = Logger.getLogger(Gene3dRawMatchFilterTest.class.getName());

    @Resource
    private org.springframework.core.io.Resource rawMatches;

    @Resource
    private org.springframework.core.io.Resource filteredMatches;

    @Resource
    private org.springframework.core.io.Resource filteredSsf;

    @Test
    public void testFilter() throws IOException {

        // Read raw matches -- we map back to these in Gene3dRawMatchFilter.filter()
        final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins =
                new HashSet<RawProtein<Gene3dHmmer3RawMatch>>(parseRawMatches(rawMatches));

        // Read filtered matches -- this is what we expect
        final Set<RawProtein<Gene3dHmmer3RawMatch>> expectedFilteredProteins =
                new HashSet<RawProtein<Gene3dHmmer3RawMatch>>(parseRawMatches(filteredMatches));

        // Parse and filter output from DomainFinder (filtered SSF file)
        Gene3dRawMatchFilter f = new Gene3dRawMatchFilter();
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = f.filter(rawProteins, filteredSsf);

        // Check
        assertEquals(expectedFilteredProteins.size(), filteredProteins.size());

        // DomainFinder might reduce the domain match, so location.start and location.end may be different, therefore
        // we can't do: assertEquals(expectedFilteredProteins, filteredProteins)
        // Instead we'll make a unique key for the expected and actual results, and compare these
        assertEquals(createKeys(expectedFilteredProteins), createKeys(filteredProteins));

    }

    private Set<String> createKeys(Set<RawProtein<Gene3dHmmer3RawMatch>> proteins) {
        Set<String> keys = new HashSet<String>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : proteins) {
            for (Gene3dHmmer3RawMatch m : p.getMatches()) {
                String key = m.getSequenceIdentifier() + "_" +
                        m.getEnvelopeStart() + "_" +
                        m.getEnvelopeEnd() + "_" +
                        m.getDomainIeValue();
                keys.add(key);
            }
        }
        return keys;
    }

    private Collection<RawProtein<Gene3dHmmer3RawMatch>> parseRawMatches(org.springframework.core.io.Resource f)
            throws IOException {
        OnionRawMatchResourceReader reader = new OnionRawMatchResourceReader();
        final Collection<Gene3dHmmer3RawMatch> matches = reader.read(f);
        final Map<String, RawProtein<Gene3dHmmer3RawMatch>> proteins = new HashMap<String, RawProtein<Gene3dHmmer3RawMatch>>();
        for (Gene3dHmmer3RawMatch m : matches) {
            String id = m.getSequenceIdentifier();
            RawProtein<Gene3dHmmer3RawMatch> p;
            if (proteins.containsKey(id)) {
                p = proteins.get(id);
            } else {
                p = new RawProtein<Gene3dHmmer3RawMatch>(id);
                proteins.put(id, p);
            }
            p.addMatch(m);
        }
        return proteins.values();
    }

    private static final class OnionRawMatchResourceReader extends AbstractResourceReader<Gene3dHmmer3RawMatch> {
        @Override
        protected Gene3dHmmer3RawMatch createRecord(String line) {
            Scanner s = new Scanner(line);
            try {
                // For file format see: uk.ac.ebi.interpro.scan.cli.Gene3dOnionAnalysisResourceWriter
                // UPI, METHOD_AC, RELNO_MAJOR, RELNO_MINOR, SEQ_START, SEQ_END, HMM_START, HMM_END, HMM_BOUNDS,
                // ENVELOPE_START, ENVELOPE_END, SCORE, SEQSCORE, SEQEVALUE, DOMAIN_C_EVALUE, DOMAIN_I_EVALUE,
                // ACC, ALIGNMENT                
                // Example:
                // UPI000002973F	1lshA02	3	3	119	189	175	249	..	83	202	10.9	12.1	-0.36653154442041347	-4.958607314841775	0.04139268515822508	null	38M3D28M1D5M
                String seqId = s.next();
                String modelId = s.next();
                String release = s.next() + "." + s.next() + ".0";
                int locStart = s.nextInt();
                int locEnd = s.nextInt();
                int hmmStart = s.nextInt();
                int hmmEnd = s.nextInt();
                String hmmBounds = s.next();
                int envStart = s.nextInt();
                int envEnd = s.nextInt();
                double locScore = s.nextDouble();
                double score = s.nextDouble();
                double evalue = PersistenceConversion.get(s.nextDouble());
                double domCevalue = PersistenceConversion.get(s.nextDouble());
                double domIevalue = PersistenceConversion.get(s.nextDouble());
                String dummyAcc = s.next();
                String cigar = s.next();
                // Dummy values
                double accuracy = 0;
                double bias = 0;
                double domBias = 0;
                return new Gene3dHmmer3RawMatch(seqId, modelId, release, locStart, locEnd, evalue, score,
                        hmmStart, hmmEnd, hmmBounds, locScore, envStart, envEnd, accuracy, bias,
                        domCevalue, domIevalue, domBias, cigar);
            }
            catch (InputMismatchException e) {
                LOGGER.error("Error reading line: " + line);
                throw e;
            }
        }
    }

}
