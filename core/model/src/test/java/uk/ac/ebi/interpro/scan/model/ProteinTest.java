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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Tests cases for {@link Protein}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinTest extends AbstractTest<Protein> {

    // http://www.uniprot.org/uniparc/UPI0000000001.fasta
    static final String MULTILINE  =
                              "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD\n" +
                              "ADAQLDAVLSAATETYSGLTPEQKAYVPAMFTAALNIQTSVNTVVRDFENYVKQTCNSSA\n" +
                              "VVDNKLKIQNVIIDECYGAPGSPTNLEFINTGSSKGNCAIKALMQLTTKATTQIAPKQVA\n" +
                              "GTGVQFYMIVIGVIILAALFMYYAKRMLFTSTNDKIKLILANKENVHWTTYMDTFFRTSP\n" +
                              "MVIATTDMQN";
    static final String SINGLELINE  =
                               "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD" +
                               "ADAQLDAVLSAATETYSGLTPEQKAYVPAMFTAALNIQTSVNTVVRDFENYVKQTCNSSA" +
                               "VVDNKLKIQNVIIDECYGAPGSPTNLEFINTGSSKGNCAIKALMQLTTKATTQIAPKQVA" +
                               "GTGVQFYMIVIGVIILAALFMYYAKRMLFTSTNDKIKLILANKENVHWTTYMDTFFRTSP" +
                               "MVIATTDMQN";

    // First line of UPI0000000001.fasta
    public static final String GOOD = "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    // echo -n "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD" | md5sum
    static final String GOOD_MD5 = "9d380adca504b0b1a2654975c340af78";


    // Contains "." so should fail when create protein
    static final String BAD = "MGAAASIQTTVNTLSERISSKLEQE.ANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    /**
     * Tests that protein can be instantiated with amino acids sequences with and without whitespace
     */
    @Test public void testGetSequence()   {
        // Should be OK
        Protein protein = new Protein(GOOD);
        assertEquals("Should be correct amino acid sequence", GOOD, protein.getSequence());
        protein = new Protein(MULTILINE);
        assertEquals("Should be correct amino acid sequence without whitespace", SINGLELINE, protein.getSequence());
        // Should fail
        try {
            new Protein(BAD);
        }
        catch (Exception e)    {
            assertTrue("Should be IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test public void testCrossReferences() {
        final String id = "test";
        Protein protein = new Protein(GOOD);
        XrefSequenceIdentifier xref = protein.addCrossReference(new XrefSequenceIdentifier(id));
        assertEquals(1, protein.getCrossReferences().size());
        assertNotNull(xref);
        assertEquals(id, xref.getIdentifier());
        protein.removeCrossReference(xref);
        assertEquals(0, protein.getCrossReferences().size());
    }

    /**
     * Tests that MD5 checksum can be calculated for the protein sequence
     */
    @Test public void testGetMd5()   {
        Protein ps = new Protein(GOOD);
        assertEquals("MD5 checksums should be same", GOOD_MD5, ps.getMd5());
    }

    @Test public void testRemoveMatch()    {
        Protein protein = new Protein(GOOD);
        RawMatch match = protein.addRawMatch(new RawHmmMatch(new Model("PF00155"), 0.035, 4.3e-61));
        assertEquals("Protein should have one match", 1, protein.getRawMatches().size());
        protein.removeRawMatch(match);
        assertEquals("Protein should have no matches", 0, protein.getRawMatches().size());
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

    @Test public void testXml() throws IOException, SAXException {
        super.testSupportsMarshalling(Protein.class);
        super.testXmlRoundTrip();
    }

    // TODO: Fails doing dao.insert() with :
    // TODO: "org.hibernate.id.IdentifierGenerationException: ids for this class must be manually assigned before calling save(): uk.ac.ebi.interpro.scan.model.FilteredMatch"
//    @Test public void testJpa() {
//        super.testJpaXmlObjects();
//    }

}
