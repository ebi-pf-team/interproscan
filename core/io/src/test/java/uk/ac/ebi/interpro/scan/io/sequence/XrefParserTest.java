package uk.ac.ebi.interpro.scan.io.sequence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

/**
 * Test class for XrefParser.
 *
 * @author Maxim Scheremetjew
 * @since 1.0
 */
public class XrefParserTest {

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
        assertNull(nucleotideSequenceXref.getDatabaseName());
        assertEquals("Wilf", nucleotideSequenceXref.getName());
        assertEquals("Wilf", nucleotideSequenceXref.getIdentifier());
        assertNull(nucleotideSequenceXref.getDatabaseName());
        //
        nucleotideSequenceXref = XrefParser.getNucleotideSequenceXref("reverse translation of P22298");
        assertNull(nucleotideSequenceXref.getDatabaseName());
        assertEquals("reverse", nucleotideSequenceXref.getIdentifier());
        assertEquals("reverse translation of P22298", nucleotideSequenceXref.getName());
    }

    @Test
    public void testGetProteinXref() {
        //tr
        ProteinXref proteinXref = XrefParser.getProteinXref("tr|A2VDN9|A2VDN9_BOVIN KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1");
        assertNull(proteinXref.getDatabaseName());
        assertEquals("tr|A2VDN9|A2VDN9_BOVIN", proteinXref.getIdentifier());
        assertEquals("tr|A2VDN9|A2VDN9_BOVIN KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1", proteinXref.getName());
        assertEquals("KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1", proteinXref.getDescription());
        //sp
        proteinXref = XrefParser.getProteinXref("sp|Q8I6R7|ACN2_ACAGO Acanthoscurrin-2 (Fragment) OS=Acanthoscurria gomesiana GN=acantho2 PE=1 SV=1");
        assertNull(proteinXref.getDatabaseName());
        assertEquals("sp|Q8I6R7|ACN2_ACAGO", proteinXref.getIdentifier());
        assertEquals("sp|Q8I6R7|ACN2_ACAGO Acanthoscurrin-2 (Fragment) OS=Acanthoscurria gomesiana GN=acantho2 PE=1 SV=1", proteinXref.getName());
        assertEquals("Acanthoscurrin-2 (Fragment) OS=Acanthoscurria gomesiana GN=acantho2 PE=1 SV=1", proteinXref.getDescription());
        //ref
        proteinXref = XrefParser.getProteinXref("gi|6679827|ref|NP_032062.1| protein fosB [Mus musculus]");
        assertNull(proteinXref.getDatabaseName());
        assertEquals("gi|6679827|ref|NP_032062.1|", proteinXref.getIdentifier());
        assertEquals("gi|6679827|ref|NP_032062.1| protein fosB [Mus musculus]", proteinXref.getName());
        assertEquals("protein fosB [Mus musculus]", proteinXref.getDescription());
        //EMBL
        proteinXref = XrefParser.getProteinXref("gi|49457155|emb|CAG46898.1| FOSB [Homo sapiens]");
        assertNull(proteinXref.getDatabaseName());
        assertEquals("gi|49457155|emb|CAG46898.1|", proteinXref.getIdentifier());
        assertEquals("gi|49457155|emb|CAG46898.1| FOSB [Homo sapiens]", proteinXref.getName());
        assertEquals("FOSB [Homo sapiens]", proteinXref.getDescription());
        //GeneBank
        proteinXref = XrefParser.getProteinXref("gi|351706989|gb|EHB09908.1| Protein fosB [Heterocephalus glaber]");
        assertNull(proteinXref.getDatabaseName());
        assertEquals("gi|351706989|gb|EHB09908.1|", proteinXref.getIdentifier());
        assertEquals("gi|351706989|gb|EHB09908.1| Protein fosB [Heterocephalus glaber]", proteinXref.getName());
        assertEquals("Protein fosB [Heterocephalus glaber]", proteinXref.getDescription());
        //
        proteinXref = XrefParser.getProteinXref("Wilf");
        assertNull(proteinXref.getDatabaseName());
        assertEquals("Wilf", proteinXref.getName());
        assertEquals("Wilf", proteinXref.getIdentifier());
        //
        proteinXref = XrefParser.getProteinXref("reverse translation of P22298");
        assertNull(proteinXref.getDatabaseName());
        assertEquals("reverse", proteinXref.getIdentifier());
        assertEquals("reverse translation of P22298", proteinXref.getName());
    }
}
