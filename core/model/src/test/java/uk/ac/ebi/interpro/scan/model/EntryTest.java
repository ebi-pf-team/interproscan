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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Test cases for {@link uk.ac.ebi.interpro.scan.model.Entry}
 *
 * @author Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EntryTest extends AbstractXmlTest<Entry> {

    private static final Log LOGGER = LogFactory.getLog(EntryTest.class);

//    @Test
//    public void testXmlWhitespace() throws IOException, SAXException {
//
//        // Can use \n or XML escape character (&#10;) for new line
//        String expectedXml =
//                "<signature ac='PF00001' xmlns='http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5'>\n" +
//                "    <abstract>This family contains, amongst other G-protein-coupled receptors (GPCRs), " +
//                "members of the opsin family, which have been considered to be typical members " +
//                "of the rhodopsin superfamily. " +
//                "&#10;&#10;" +
//                "They share several motifs, mainly the seven transmembrane helices, " +
//                "GPCRs of the rhodopsin superfamily. All opsins bind a chromophore, " +
//                "such as 11-cis-retinal. " +
//                "\n\n" +
//                "The function of most opsins other than the photoisomerases is split into two steps: " +
//                "light absorption and G-protein activation. Photoisomerases, on the other hand, are " +
//                "not coupled to G-proteins - they are thought to generate and supply the chromophore " +
//                "that is used by visual opsins [pubmed:15774036]</abstract>\n" +
//                "</signature>";
//
//        Signature actual = unmarshal(expectedXml);
//
//        Signature expected = new Signature
//                .Builder("PF00001")
//                .abstractText(
//                "This family contains, amongst other G-protein-coupled receptors (GPCRs), " +
//                "members of the opsin family, which have been considered to be typical members " +
//                "of the rhodopsin superfamily. " +
//                "\n\n" +
//                "They share several motifs, mainly the seven transmembrane helices, " +
//                "GPCRs of the rhodopsin superfamily. All opsins bind a chromophore, " +
//                "such as 11-cis-retinal. " +
//                "\n\n" +
//                "The function of most opsins other than the photoisomerases is split into two steps: " +
//                "light absorption and G-protein activation. Photoisomerases, on the other hand, are " +
//                "not coupled to G-proteins - they are thought to generate and supply the chromophore " +
//                "that is used by visual opsins [pubmed:15774036]")
//                .build();
//
//        assertEquals(expected, actual);
//
//        if (LOGGER.isDebugEnabled())    {
//            LOGGER.debug(actual);
//        }
//
//        String actualXml = marshal(actual);
//        assertXmlEquals("Whitespace test", expectedXml, actualXml);
//
//        if (LOGGER.isDebugEnabled())    {
//            LOGGER.debug(actualXml);
//        }
//
//    }

    @Test
    public void testBuilder() throws IOException, ParseException {

        final String AC = "IPR011364";
        final String NAME = "BRCA1";
        final EntryType TYPE = EntryType.FAMILY;
        final String DESC = "BRCA1";
        final String ABSTRACT = "This group represents a DNA-damage repair protein, BRCA1";
        final Date CREATED = DateAdapter.toDate("2005-12-25");
        final Date UPDATED = DateAdapter.toDate("2010-10-18");

        Release release = new Release("32.0");
        PathwayXref pathwayXref = new PathwayXref("identifier", "name", "databaseName");

        Entry entry = new Entry.Builder(AC)
                .name(NAME)
                .type(TYPE)
                .description(DESC)
                .abstractText(ABSTRACT)
                .release(release)
                .created(CREATED)
                .updated(UPDATED)
                .signature(new Signature.Builder("PIRSF001734").name("BRCA1").build())
                .signature(new Signature.Builder("PTHR13763").name("BRCA1").signatureLibraryRelease(new SignatureLibraryRelease(SignatureLibrary.PANTHER, "7.0")).build())
                .goCrossReference(new GoXref("GO:0006281", "DNA repair", GoCategory.BIOLOGICAL_PROCESS))
                .goCrossReference(new GoXref("GO:0003677", "DNA binding", GoCategory.MOLECULAR_FUNCTION))
                .goCrossReference(new GoXref("GO:0008270", "zinc ion binding", GoCategory.MOLECULAR_FUNCTION))
                .goCrossReference(new GoXref("GO:0005634", "nucleus", GoCategory.CELLULAR_COMPONENT))
                .pathwayCrossReference(pathwayXref)
                .build();

        assertEquals(AC, entry.getAccession());
        assertEquals(NAME, entry.getName());
        assertEquals(TYPE, entry.getType());
        assertEquals(DESC, entry.getDescription());
        assertEquals(ABSTRACT, entry.getAbstract());
        assertEquals(CREATED, entry.getCreated());
        assertEquals(UPDATED, entry.getUpdated());
        //
        assertNotNull(entry.getReleases());
        assertEquals(1, entry.getReleases().size());
        //TODO: Correct the following test
        assertFalse(entry.getReleases().contains(release));
        //
        assertNotNull(entry.getPathwayXRefs());
        assertEquals(1, entry.getPathwayXRefs().size());
        //TODO: Correct the following test
        assertFalse(entry.getPathwayXRefs().contains(pathwayXref));
        //
        assertNotNull(entry.getSignatures());
        assertEquals(2, entry.getSignatures().size());
        //
        assertNotNull(entry.getGoXRefs());
        assertEquals(4, entry.getGoXRefs().size());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(entry);
            LOGGER.debug(super.marshal(entry));
        }

    }

//    @Test public void testRemoveModel()   {
//        Signature signature = new Signature("SIG001");
//        Model m1 = signature.addModel(new Model("MOD001"));
//        Model m2 = signature.addModel(new Model("MOD002"));
//        // Should be OK
//        assertEquals(2, signature.getModels().size());
//        // Should be OK (key not recognised, so just ignores)
//        signature.removeModel(new Model("UNKNOWN"));
//        assertEquals(2, signature.getModels().size());
//        // Should fail
//        try {
//            signature.removeModel(null);
//        }
//        catch (Exception e)    {
//            assertTrue(e instanceof NullPointerException);
//        }
//        // Should be OK
//        signature.removeModel(m1);
//        assertEquals(1, signature.getModels().size());
//        signature.removeModel(m2);
//        assertEquals(0, signature.getModels().size());
//    }

}
