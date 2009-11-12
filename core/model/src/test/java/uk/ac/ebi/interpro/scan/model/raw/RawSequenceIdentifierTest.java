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
        RawSequenceIdentifier sequence = new RawSequenceIdentifier(MD5);
        // Add matches
        sequence.addMatch(new ProDomRawMatch(MD5, "PD001061", "ProDom", "2006.01", 9, 150, 763, "BlastProDom"));
        sequence.addMatch(new ProDomRawMatch(MD5, "PD001061", "ProDom", "2006.01", 151, 245, 469, "BlastProDom"));
        sequence.addMatch(
                new PfamHmmer3RawMatch(MD5, "PF02310", "Pfam", "24.0", 3, 107, 3.7E-9, 0.035, 1, 104, "[]",
                        3.7E-9, 3.0, 0, 0, 0, 0, 0, 0, 0, "HMMER 2.3.2")
        );
        // Test
        assertEquals(MD5, sequence.getSequenceIdentifier());
        assertEquals(3, sequence.getMatches().size());
        assertEquals(sequence, SerializationUtils.clone(sequence));
    }

}
