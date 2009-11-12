package uk.ac.ebi.interpro.scan.model.raw;

import junit.framework.TestCase;
import org.junit.Test;
import org.apache.commons.lang.SerializationUtils;

import java.util.HashSet;
import java.util.Collection;

/**
 * Tests cases for {@link RawSequenceIdentifier}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public final class RawSequenceIdentifierTest extends TestCase {

    @Test
    public void testGetMatches() {
        final String MD5 = "9d380adca504b0b1a2654975c340af78";
        Collection<RawMatch> matches = new HashSet<RawMatch>();
        // ProDom
        matches.add(getProDomMatch(MD5, "PD001061", 9, 150, 763));
        matches.add(getProDomMatch(MD5, "PD001061", 151, 245, 469));
        // Pfam
        matches.add(getHmmMatch(MD5, "PF02310", 3, 107, 3.7E-9, 0.035, 1, 104, "[]", 3.7E-9, 3.0));
        RawSequenceIdentifier identifier = new RawSequenceIdentifier(MD5, matches);
        assertEquals(MD5, identifier.getSequenceIdentifier());
        assertEquals(3, identifier.getMatches().size());
        assertEquals(identifier, (RawSequenceIdentifier)SerializationUtils.clone(identifier));
    }

    private Hmmer3RawMatch getHmmMatch(String id, String model, long start, long end, double evalue, double score,
                                    long hmmStart, long hmmEnd, String hmmBounds,
                                    double locationEvalue, double locationScore) {
        // TODO: Require mandatory constructor args and/or use Builder Pattern
        Hmmer3RawMatch m = new PfamHmmer3RawMatch();
        addRawMatchData(m, id, model, start, end, "23.0", "HMMER 2.3.2");
        m.setEvalue(evalue);
        m.setScore(score);
        m.setHmmStart(hmmStart);
        m.setHmmEnd(hmmEnd);
        m.setHmmBounds(hmmBounds);
        m.setLocationEvalue(locationEvalue);
        m.setLocationScore(locationScore);
        return m;
    }

    private ProDomRawMatch getProDomMatch(String id, String model, long start, long end, double score) {
        ProDomRawMatch m = new ProDomRawMatch();
        addRawMatchData(m, id, model, start, end, "2006.01", "BlastProDom");
        m.setScore(score);
        return m;
    }

    private void addRawMatchData(RawMatch m, String id, String model,
                                 long start, long end, String dbversion, String generator)  {
        m.setSequenceIdentifier(id);
        m.setModel(model);
        m.setLocationStart(start);
        m.setLocationEnd(end);
        m.setSignatureLibraryRelease(dbversion);
        m.setGenerator(generator);
    }

}
