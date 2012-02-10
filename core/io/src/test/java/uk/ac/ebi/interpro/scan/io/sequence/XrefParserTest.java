package uk.ac.ebi.interpro.scan.io.sequence;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

/**
 * Test class for XrefParser.
 *
 * @author Maxim Scheremetjew
 * @since 1.0
 */
public class XrefParserTest extends TestCase {

    @Test
    public void testGetNucleotideSequenceXref() {
        NucleotideSequenceXref nucleotideSequenceXref = XrefParser.getNucleotideSequenceXref("ENA|AACH01000026|AACH01000026.1 Saccharomyces mikatae IFO 1815 YM4906-Contig2858, whole genome shotgun sequence.");
        Assert.assertEquals("ENA", nucleotideSequenceXref.getDatabaseName());
        Assert.assertEquals("AACH01000026", nucleotideSequenceXref.getIdentifier());
        Assert.assertEquals("AACH01000026.1 Saccharomyces mikatae IFO 1815 YM4906-Contig2858, whole genome shotgun sequence.", nucleotideSequenceXref.getName());
    }

    @Test
    public void testGetProteinXref() {
        ProteinXref proteinXref = XrefParser.getProteinXref("tr|A2VDN9|A2VDN9_BOVIN KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1");
        Assert.assertEquals("tr", proteinXref.getDatabaseName());
        Assert.assertEquals("A2VDN9", proteinXref.getIdentifier());
        Assert.assertEquals("A2VDN9_BOVIN KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1", proteinXref.getName());
    }
}