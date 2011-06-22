package uk.ac.ebi.interpro.scan.model.raw;

import junit.framework.TestCase;
import org.junit.Test;
import org.apache.commons.lang.SerializationUtils;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

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
        protein.addMatch(new ProDomRawMatch(PROTEIN_ID, "PD400414", "2006.01", 1, 198, "U689_HUMAN_Q6UX39", 1, 206, 426, 1E-41, 3, "PRECURSOR SIGNAL UNQ689/PRO1329 HOMOLOG DIRECT SEQUENCING EO-017 SECRETED"));
        protein.addMatch(new ProDomRawMatch(PROTEIN_ID, "PD400414", "2006.01", 1, 212, "U689_HUMAN_Q6UX39", 1, 206, 501, 2E-50, 3, "PRECURSOR SIGNAL UNQ689/PRO1329 HOMOLOG DIRECT SEQUENCING EO-017 SECRETED"));
        protein.addMatch(
                new PfamHmmer3RawMatch(PROTEIN_ID, "PF02310", SignatureLibrary.PFAM, "24.0", 3, 107, 3.7E-9, 0.035, 1, 104, "[]", 3.0,
                        0, 0, 0, 0, 0, 0, 0)
        );
        // Test
        assertEquals(PROTEIN_ID, protein.getProteinIdentifier());
        assertEquals(3, protein.getMatches().size());
        assertEquals(protein, SerializationUtils.clone(protein));
    }

}
