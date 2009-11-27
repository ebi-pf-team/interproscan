package uk.ac.ebi.interpro.scan.model.raw;

import junit.framework.TestCase;
import org.junit.Test;
import org.apache.commons.lang.SerializationUtils;

/**
 * Tests cases for {@link RawProtein}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public final class RawProteinTest extends TestCase {

    @Test
    public void testGetMatches() {
        final String PROTEIN_ID = "1";  // We'll use primary key to identify protein
        RawProtein protein = new RawProtein(PROTEIN_ID);
        // Add matches
        protein.addMatch(new ProDomRawMatch(PROTEIN_ID, "PD001061", "ProDom", "2006.01", 9, 150, 763, "BlastProDom"));
        protein.addMatch(new ProDomRawMatch(PROTEIN_ID, "PD001061", "ProDom", "2006.01", 151, 245, 469, "BlastProDom"));
        protein.addMatch(
                new PfamHmmer3RawMatch(PROTEIN_ID, "PF02310", "Pfam", "24.0", 3, 107, 3.7E-9, 0.035, 1, 104, "[]", 3.0,
                        0, 0, 0, 0, 0, 0, 0, "HMMER 2.3.2")
        );
        // Test
        assertEquals(PROTEIN_ID, protein.getProteinIdentifier());
        assertEquals(3, protein.getMatches().size());
        assertEquals(protein, SerializationUtils.clone(protein));
    }

}
