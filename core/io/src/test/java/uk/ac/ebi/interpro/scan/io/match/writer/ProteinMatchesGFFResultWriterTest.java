package uk.ac.ebi.interpro.scan.io.match.writer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Test for ProteinMatchesGFFResultWriter.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesGFFResultWriterTest {

    @Test
    public void testGetValidGFF3SeqId() throws IOException {
        String expected = "tr|A2VDN9|A2VDN9_BOVIN_KIAA0020_protein_OSBos_taurus_GNKIAA0020_PE2_SV1";
        String actual = GFFResultWriterForNucSeqs.getValidGFF3SeqId("tr|A2VDN9|A2VDN9_BOVIN KIAA0020 protein OS=Bos taurus GN=KIAA0020 PE=2 SV=1");
        Assert.assertEquals(expected, actual);
        //
        expected = "ENA|AACH01000026|AACH01000026.1_Saccharomyces_mikatae_IFO_1815_YM4906-Contig2858_whole_genome_shotgun_sequence.";
        actual = GFFResultWriterForNucSeqs.getValidGFF3SeqId("ENA|AACH01000026|AACH01000026.1 Saccharomyces mikatae IFO 1815 YM4906-Contig2858, whole genome shotgun sequence.");
        Assert.assertEquals(expected, actual);
        //
        expected = "P22298";
        actual = GFFResultWriterForNucSeqs.getValidGFF3SeqId("P22298");
        Assert.assertEquals(expected, actual);
        //
        expected = "reverse_translation_of_A2VDN9";
        actual = GFFResultWriterForNucSeqs.getValidGFF3SeqId("reverse translation of A2VDN9");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetValidGFF3AttributeName() throws IOException {
        String expected = "test";
        String actual = GFFResultWriterForNucSeqs.getValidGFF3AttributeName("test");
        Assert.assertEquals(expected, actual);
        //
        expected = "test%2Ctest";
        actual = GFFResultWriterForNucSeqs.getValidGFF3AttributeName("test,test");
        Assert.assertEquals(expected, actual);
        //
        expected = "test%3Dtest";
        actual = GFFResultWriterForNucSeqs.getValidGFF3AttributeName("test=test");
        Assert.assertEquals(expected, actual);
        //
        expected = "test%3Btest";
        actual = GFFResultWriterForNucSeqs.getValidGFF3AttributeName("test;test");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPattern() {
        String message = "'seqId' should only have characters which ARE IN the set [a-zA-Z0-9.:^*$@!+_?-|]";
        String expected = "test";
        //
        String actual = expected.replaceAll(GFFResultWriterForNucSeqs.SEQID_FIELD_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        expected = "test12121test";
        actual = expected.replaceAll(GFFResultWriterForNucSeqs.SEQID_FIELD_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        expected = "test12121.:^*$@!+_?-|";
        actual = expected.replaceAll(GFFResultWriterForNucSeqs.SEQID_FIELD_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //Not allowed characters check
        expected = "test";
        actual = "test%".replaceAll(GFFResultWriterForNucSeqs.SEQID_FIELD_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        actual = "test[]".replaceAll(GFFResultWriterForNucSeqs.SEQID_FIELD_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        actual = "test ".replaceAll(GFFResultWriterForNucSeqs.SEQID_FIELD_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
    }
}