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
        /*
        The ENA parsing has been removed from I5 at the moment as part of the
        short-term nucleotide header fix  (IBU-2426)
        TODO - decide if ENA parsing should be re-implmented when the long-term fix is in place
        Assert.assertEquals("ENA", nucleotideSequenceXref.getDatabaseName());
        Assert.assertEquals("AACH01000026", nucleotideSequenceXref.getIdentifier());
        */
        //
        nucleotideSequenceXref = XrefParser.getNucleotideSequenceXref("Wilf");
        Assert.assertNull(nucleotideSequenceXref.getDatabaseName());
        Assert.assertNull(nucleotideSequenceXref.getName());
        Assert.assertEquals("Wilf", nucleotideSequenceXref.getIdentifier());
        //
        nucleotideSequenceXref = XrefParser.getNucleotideSequenceXref("reverse translation of P22298");
        Assert.assertNull(nucleotideSequenceXref.getDatabaseName());
        Assert.assertEquals("reverse", nucleotideSequenceXref.getIdentifier());
        Assert.assertEquals("translation of P22298", nucleotideSequenceXref.getName());
    }

    @Test
    public void testGetProteinXref() {
        //tr
        ProteinXref proteinXref = XrefParser.getProteinXref("tr|A2VDN9|A2VDN9_BOVIN KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1");
        Assert.assertEquals("tr", proteinXref.getDatabaseName());
        Assert.assertEquals("A2VDN9", proteinXref.getIdentifier());
        Assert.assertEquals("KIAA0020 protein", proteinXref.getName());
        Assert.assertEquals("A2VDN9_BOVIN KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1", proteinXref.getDescription());
        //sp
        proteinXref = XrefParser.getProteinXref("sp|Q8I6R7|ACN2_ACAGO Acanthoscurrin-2 (Fragment) OS=Acanthoscurria gomesiana GN=acantho2 PE=1 SV=1");
        Assert.assertEquals("sp", proteinXref.getDatabaseName());
        Assert.assertEquals("Q8I6R7", proteinXref.getIdentifier());
        Assert.assertEquals("Acanthoscurrin-2 (Fragment)", proteinXref.getName());
        Assert.assertEquals("ACN2_ACAGO Acanthoscurrin-2 (Fragment) OS=Acanthoscurria gomesiana GN=acantho2 PE=1 SV=1", proteinXref.getDescription());
        //ref
        proteinXref = XrefParser.getProteinXref("gi|6679827|ref|NP_032062.1| protein fosB [Mus musculus]");
        Assert.assertEquals("ref", proteinXref.getDatabaseName());
        Assert.assertEquals("NP_032062.1", proteinXref.getIdentifier());
        Assert.assertEquals("protein fosB", proteinXref.getName());
        Assert.assertEquals("protein fosB [Mus musculus]", proteinXref.getDescription());
        //EMBL
        proteinXref = XrefParser.getProteinXref("gi|49457155|emb|CAG46898.1| FOSB [Homo sapiens]");
        Assert.assertEquals("emb", proteinXref.getDatabaseName());
        Assert.assertEquals("CAG46898.1", proteinXref.getIdentifier());
        Assert.assertEquals("FOSB", proteinXref.getName());
        Assert.assertEquals("FOSB [Homo sapiens]", proteinXref.getDescription());
        //GeneBank
        proteinXref = XrefParser.getProteinXref("gi|351706989|gb|EHB09908.1| Protein fosB [Heterocephalus glaber]");
        Assert.assertEquals("gb", proteinXref.getDatabaseName());
        Assert.assertEquals("EHB09908.1", proteinXref.getIdentifier());
        Assert.assertEquals("Protein fosB", proteinXref.getName());
        Assert.assertEquals("Protein fosB [Heterocephalus glaber]", proteinXref.getDescription());
        //
        proteinXref = XrefParser.getProteinXref("Wilf");
        Assert.assertNull(proteinXref.getDatabaseName());
        Assert.assertNull(proteinXref.getName());
        Assert.assertEquals("Wilf", proteinXref.getIdentifier());
        //
        proteinXref = XrefParser.getProteinXref("reverse translation of P22298");
        Assert.assertNull(proteinXref.getDatabaseName());
        Assert.assertEquals("reverse", proteinXref.getIdentifier());
        Assert.assertEquals("translation of P22298", proteinXref.getName());
    }
}
