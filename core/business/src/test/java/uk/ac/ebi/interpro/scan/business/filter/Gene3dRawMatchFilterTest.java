package uk.ac.ebi.interpro.scan.business.filter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;
import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;

import javax.annotation.Resource;
import java.util.*;
import java.io.IOException;

/**
 * Tests {@link Gene3dRawMatchFilter}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public final class Gene3dRawMatchFilterTest {
    
//    @Resource
//    private BinaryRunner binaryRunner;

    @Resource
    private org.springframework.core.io.Resource rawMatches;

    @Resource
    private org.springframework.core.io.Resource filteredMatches;

    @Resource
    private org.springframework.core.io.Resource filteredSsf;

    @Test
    public void testFilter() throws IOException {

        // Read raw matches
        final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins =
                new HashSet<RawProtein<Gene3dHmmer3RawMatch>>(parseRawMatches(rawMatches));        

        // Read filtered matches
        final Set<RawProtein<Gene3dHmmer3RawMatch>> expectedFilteredProteins =
                new HashSet<RawProtein<Gene3dHmmer3RawMatch>>(parseRawMatches(filteredMatches));

        // Parse and filter SSF file
        Gene3dRawMatchFilter f = new Gene3dRawMatchFilter();
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins =  f.filter(rawProteins, filteredSsf);

        // Check
        assertEquals(expectedFilteredProteins.size(), filteredProteins.size());
        assertEquals(expectedFilteredProteins, filteredProteins);

    }

    private Collection<RawProtein<Gene3dHmmer3RawMatch>> parseRawMatches(org.springframework.core.io.Resource f)
            throws IOException {
        OnionRawMatchResourceReader reader = new OnionRawMatchResourceReader();
        final Collection<Gene3dHmmer3RawMatch> matches = reader.read(f);
        final Map<String, RawProtein<Gene3dHmmer3RawMatch>> proteins = new HashMap<String, RawProtein<Gene3dHmmer3RawMatch>>();
        for (Gene3dHmmer3RawMatch m : matches)  {
            String id = m.getSequenceIdentifier();
            RawProtein<Gene3dHmmer3RawMatch> p;
            if (proteins.containsKey(id))    {
                p = proteins.get(id);
            }
            else    {
                p = new RawProtein<Gene3dHmmer3RawMatch>(id);
                proteins.put(id, p);
            }
            p.addMatch(m);
        }
        return proteins.values();
    }

    private static final class OnionRawMatchResourceReader extends AbstractResourceReader<Gene3dHmmer3RawMatch>   {
        @Override protected Gene3dHmmer3RawMatch createRecord(String line) {
            Scanner s = new Scanner(line);
            try {
                // UPI000002973F	1lshA02	3	3	119	189	175	249	..	83	202	10.9	12.1	-0.36653154442041347	-4.958607314841775	0.04139268515822508	null	38M3D28M1D5M
                String seqId    = s.next();
                String modelId  = s.next();
                String release  = s.next() + "." + s.next() + ".0";
                int locStart    = s.nextInt();
                int locEnd      = s.nextInt();
                int hmmStart    = s.nextInt();
                int hmmEnd      = s.nextInt();
                String hmmBounds = s.next();
                int envStart    = s.nextInt();
                int envEnd      = s.nextInt();
                double locScore = s.nextDouble();
                double score    = s.nextDouble();
                double evalue = PersistenceConversion.get(s.nextDouble());
                double domCevalue = PersistenceConversion.get(s.nextDouble());
                double domIevalue = PersistenceConversion.get(s.nextDouble());
                String dummyAcc = s.next();
                String cigar    = s.next();
                // Dummy values
                double accuracy = 0;
                double bias     = 0;
                double domBias  = 0;
                return new Gene3dHmmer3RawMatch(seqId, modelId, release, locStart, locEnd, evalue, score,
                        hmmStart, hmmEnd, hmmBounds, locScore, envStart, envEnd, accuracy, bias,
                        domCevalue, domIevalue, domBias, cigar);
            }
            catch (InputMismatchException e) {
                System.err.println("Error reading line: " + line);
                throw e;
            }
        }
    }


}
