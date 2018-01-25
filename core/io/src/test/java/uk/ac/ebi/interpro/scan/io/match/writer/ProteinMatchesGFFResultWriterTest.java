package uk.ac.ebi.interpro.scan.io.match.writer;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

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
        String actual = expected.replaceAll(GFFResultWriterForNucSeqs.SEQID_DISALLOWED_CHAR_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        expected = "test12121test";
        actual = expected.replaceAll(GFFResultWriterForNucSeqs.SEQID_DISALLOWED_CHAR_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        expected = "test12121.:^*$@!+_?-|";
        actual = expected.replaceAll(GFFResultWriterForNucSeqs.SEQID_DISALLOWED_CHAR_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //Not allowed characters check
        expected = "test";
        actual = "test%".replaceAll(GFFResultWriterForNucSeqs.SEQID_DISALLOWED_CHAR_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        actual = "test[]".replaceAll(GFFResultWriterForNucSeqs.SEQID_DISALLOWED_CHAR_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
        //
        actual = "test ".replaceAll(GFFResultWriterForNucSeqs.SEQID_DISALLOWED_CHAR_PATTERN.pattern(), "");
        Assert.assertEquals(message, expected, actual);
    }

    /**
     * This test is specifically looking for boundary errors
     * on the generation of sequence fragments based upon
     * feature coordinates, so ignores quite a lot of the implementation detail.
     */
    @Test
    public void testProcessMatches() throws IOException {
        ProteinMatchesGFFResultWriter writer = new ProteinMatchesGFFResultWriter(Paths.get("./target/test.gff"), "5.15-64.0") {
            @Override
            public int write(Protein protein) throws IOException {
                // no implementation - this method is not being tested here.
                return 0;
            }
        };
        Signature signature = new Signature("PF00001", "PF00001", "Family", "Test signature", "Abstract",
                new SignatureLibraryRelease(SignatureLibrary.PFAM, "1.1"), Collections.<Model>emptySet());
        Protein protein = new Protein("ABCDEFGHIJKLMNOP"); // 16 AA long.

        Set<Match> matches = new HashSet<>();
        Set<Location> locations = new HashSet<>();
        Match match = new Match(signature, locations) {
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        };
        matches.add(match);

        match.addLocation(new Location(new LocationFragment(1, 16) {
            @Override
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        }) {
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        });
        match.addLocation(new Location(new LocationFragment(1, 17) {
            @Override
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        }) {
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        });
        match.addLocation(new Location(new LocationFragment(1, 15) {
            @Override
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        }) {
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        });
        match.addLocation(new Location(new LocationFragment(14, 15) {
            @Override
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        }) {
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        });
        match.addLocation(new Location(new LocationFragment(14, 17) {
            @Override
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        }) {
            public Object clone() throws CloneNotSupportedException {
                return null;
            }
        });
        writer.processMatches(matches, "target", "12 May 2012", protein, "P00000");

        Map<String, String> identifierToSequence = writer.getIdentifierToSeqMap();
        Assert.assertEquals(5, identifierToSequence.size());
        Map<String, String> unseenIdentifiers = new HashMap<>();
        unseenIdentifiers.put("match$1_1_16", "ABCDEFGHIJKLMNOP");
        unseenIdentifiers.put("match$1_1_17", "ABCDEFGHIJKLMNOP");
        unseenIdentifiers.put("match$1_1_15", "ABCDEFGHIJKLMNO");
        unseenIdentifiers.put("match$1_14_15", "NO");
        unseenIdentifiers.put("match$1_14_17", "NOP");

        for (String identifier : identifierToSequence.keySet()) {
            String expectedSequence = unseenIdentifiers.get(identifier);
            Assert.assertNotNull(expectedSequence);
            Assert.assertFalse(expectedSequence.isEmpty());
            Assert.assertEquals(expectedSequence, identifierToSequence.get(identifier));
            unseenIdentifiers.remove(identifier);
        }
        Assert.assertEquals("Not all of the expected matches have been seen.", 0, unseenIdentifiers.size());
    }
}
