/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests cases for {@link Protein}.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinTest extends AbstractTest<Protein> {

    private static final Logger LOGGER = Logger.getLogger(ProteinTest.class.getName());

    // http://www.uniprot.org/uniparc/UPI0000000001.fasta
    private static final String MULTILINE =
            "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD\n" +
                    "ADAQLDAVLSAATETYSGLTPEQKAYVPAMFTAALNIQTSVNTVVRDFENYVKQTCNSSA\n" +
                    "VVDNKLKIQNVIIDECYGAPGSPTNLEFINTGSSKGNCAIKALMQLTTKATTQIAPKQVA\n" +
                    "GTGVQFYMIVIGVIILAALFMYYAKRMLFTSTNDKIKLILANKENVHWTTYMDTFFRTSP\n" +
                    "MVIATTDMQN";
    private static final String SINGLELINE =
            "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD" +
                    "ADAQLDAVLSAATETYSGLTPEQKAYVPAMFTAALNIQTSVNTVVRDFENYVKQTCNSSA" +
                    "VVDNKLKIQNVIIDECYGAPGSPTNLEFINTGSSKGNCAIKALMQLTTKATTQIAPKQVA" +
                    "GTGVQFYMIVIGVIILAALFMYYAKRMLFTSTNDKIKLILANKENVHWTTYMDTFFRTSP" +
                    "MVIATTDMQN";

    // First line of UPI0000000001.fasta
    private static final String GOOD = "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    // echo -n "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD" | md5sum
    private static final String GOOD_MD5 = "9d380adca504b0b1a2654975c340af78";


    // Contains "." so should fail when create protein
    private static final String BAD = "MGAAASIQTTVNTLSERISSKLEQE.ANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    /**
     * Tests that protein can be instantiated with amino acids sequences with and without whitespace
     */
    @Test
    public void testGetSequence() {
        // Should be OK
        Protein protein = new Protein(GOOD);
        assertEquals("Should be correct amino acid sequence", GOOD, protein.getSequence());
        protein = new Protein(MULTILINE);
        assertEquals("Should be correct amino acid sequence without whitespace", SINGLELINE, protein.getSequence());
        // Should fail
        try {
            new Protein(BAD);
        } catch (Exception e) {
            assertTrue("Should be IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testCrossReferences() {
        final String AC = "test";
        Protein protein = new Protein(GOOD);
        ProteinXref xref = protein.addCrossReference(new ProteinXref(AC));
        assertEquals(1, protein.getCrossReferences().size());
        assertNotNull(xref);
        assertEquals(AC, xref.getIdentifier());
        protein.removeCrossReference(xref);
        assertEquals(0, protein.getCrossReferences().size());
    }

    /**
     * Tests the equivalent() method works as expected
     */
    @Test
    @Ignore
    public void testEquals() throws IOException {
        Protein original = new Protein(GOOD);
        Protein copy = (Protein) SerializationUtils.clone(original);
        // Original should equal itself
        assertEquals(original, original);
        // Original and copy should be equal
        assertEquals(original, copy);
        // Original and copy should not be equal
        ProteinXref ProteinXref = original.addCrossReference(new ProteinXref("A0A000_9ACTO"));
        assertFalse("Original and copy should not be equal", original.equals(copy));
        //  Original and copy should be equal again
        copy.addCrossReference((ProteinXref) SerializationUtils.clone(ProteinXref));
        assertEquals(original, copy);
        // Try with locations
        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<Hmmer2Match.Hmmer2Location>();
        locations.add(new Hmmer2Match.Hmmer2Location(3, 107, 3.0, 3.7e-9, 1, 104, 104,HmmBounds.N_TERMINAL_COMPLETE));
        Match match = original.addMatch(new Hmmer2Match(new Signature("PF02310", "B12-binding"), "PF02310", 0.035, 3.7e-9, locations));
        assertFalse("Original and copy should not be equal", original.equals(copy));
        copy.addMatch((Hmmer2Match) SerializationUtils.clone(match));
        assertEquals(original, copy);
        // Print
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(original);
            LOGGER.debug(super.marshal(original));
        }
    }

    /**
     * Tests the serialization and de-serialization works.
     */
    @Test
    public void testSerialization() throws IOException {

        Protein original = new Protein(GOOD);
        original.addCrossReference(new ProteinXref("A0A000_9ACTO"));

        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<>();
        locations.add(new Hmmer2Match.Hmmer2Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.N_TERMINAL_COMPLETE));
        original.addMatch(new Hmmer2Match(new Signature("PF02310", "B12-binding"), "PF02310",0.035, 3.7e-9, locations));

        Set<ProfileScanMatch.ProfileScanLocation> l = new HashSet<>();
        // Sequence is 60 chars, so make up a CIGAR string that adds up to 60 (10+10+30):
        l.add(new ProfileScanMatch.ProfileScanLocation(1, 60, 15.158, "10M10D10I30M"));
        original.addMatch(new ProfileScanMatch(new Signature("PS50206"), "PS50206", l));

        byte[] data = SerializationUtils.serialize(original);
        String originalXML = marshal(original);
        Object retrieved = SerializationUtils.deserialize(data);
        String unserializedXML = marshal((Protein) retrieved);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(unserializedXML);
            LOGGER.debug("Data lengths serialized: " + data.length + " original xml: " + originalXML.length() + " unserialized xml: " + unserializedXML.length());
        }

        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setNormalizeWhitespace(true);
        assertEquals(originalXML, unserializedXML);
    }

    /**
     * Tests that MD5 checksum can be calculated for the protein sequence
     */
    @Test
    public void testGetMd5() {
        Protein ps = new Protein(GOOD);
        assertEquals("MD5 checksums should be same", GOOD_MD5, ps.getMd5());
    }

    @Test
    public void testRemoveMatch() {
        Protein protein = new Protein(GOOD);
        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<Hmmer2Match.Hmmer2Location>();
        locations.add(new Hmmer2Match.Hmmer2Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.N_TERMINAL_COMPLETE));
        Match match = protein.addMatch(new Hmmer2Match(new Signature("PF00155"), "PF00155", 0.035, 4.3e-61, locations));
        assertEquals("Protein should have one match", 1, protein.getMatches().size());
        protein.removeMatch(match);
        assertEquals("Protein should have no matches", 0, protein.getMatches().size());
    }

    // Note: The following does not work perhaps because IllegalArgumentException is a runtime exception, and only
    //       checked exception such as IOException can be used with @Text(expected)
    /*
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testGetSequenceBad()   {
        Protein ps;
        ps = new Protein(BAD);
    }
    */

    @Test
    public void testXml() throws IOException, SAXException {
        super.testSupportsMarshalling(Protein.class);
        super.testXmlRoundTrip();
    }

    // TODO: Re-enable when JPA works OK

    @Test
    @Ignore("Fails due to problems with retrievel of match data")
    public void testJpa() {
        super.testJpaXmlObjects(new ObjectRetriever<Protein>() {
            public Protein getObjectByPrimaryKey(GenericDAO<Protein, Long> dao, Long primaryKey) {
                return dao.readDeep(primaryKey, "matches", "crossReferences");
            }

            public Long getPrimaryKey(Protein protein) {
                return protein.getId();
            }
        });
    }

}
