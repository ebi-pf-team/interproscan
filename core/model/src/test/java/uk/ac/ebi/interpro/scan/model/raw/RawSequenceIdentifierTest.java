package uk.ac.ebi.interpro.scan.model.raw;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Set;
import java.util.HashSet;

/**
 * Tests cases for {@link RawSequenceIdentifier}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public final class RawSequenceIdentifierTest extends TestCase {

    @Test public void testGetMatches() {
        final String MD5 = "9d380adca504b0b1a2654975c340af78";
        Set<RawMatch> matches = new HashSet<RawMatch>();
        // ProDom
        matches.add(getProDomMatch(MD5, "PD001061", 9, 150, 763));
        matches.add(getProDomMatch(MD5, "PD001061", 151, 245, 469));
        // Pfam
        matches.add(getHmmMatch(MD5, "PF02310", 3, 107, 3.7E-9, 0.035, 1, 104, "[]", 3.7E-9, 3.0));
        RawSequenceIdentifier identifier = new RawSequenceIdentifier(MD5, matches);
        assertEquals(MD5, identifier.getSequenceIdentifier());
        assertEquals(3, identifier.getMatches().size());
        // TODO: Add hashCode() and equals() to RawMatch implementation so can test collection equality
        //assertEquals(matches, identifier.getMatches());
    }

    private HmmRawMatch getHmmMatch(String id, String model, long start, long end, double evalue, double score,
                                    long hmmStart, long hmmEnd, String hmmBounds,
                                    double locationEvalue, double locationScore) {
        HmmRawMatch m = new HmmRawMatch();
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

    private BlastProDomRawMatch getProDomMatch(String id, String model, long start, long end, double score) {
        BlastProDomRawMatch m = new BlastProDomRawMatch();
        addRawMatchData(m, id, model, start, end, "2006.01", "BlastProDom");
        m.setScore(score);
        return m;
    }

    private void addRawMatchData(RawMatch m, String id, String model,
                                 long start, long end, String dbversion, String generator)  {
        m.setSequenceIdentifier(id);
        m.setModel(model);
        m.setStart(start);
        m.setEnd(end);
        m.setDbversion(dbversion);
        m.setGenerator(generator);
    }

}
