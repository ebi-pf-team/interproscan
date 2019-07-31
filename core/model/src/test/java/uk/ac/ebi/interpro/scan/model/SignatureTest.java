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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link Signature}
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class SignatureTest extends AbstractXmlTest<Signature> {

    // TODO: Add Xrefs and Entry to Signature
    // TODO: Add Model MD5?

    private static final Log LOGGER = LogFactory.getLog(SignatureTest.class);

    @Test
    public void testXmlWhitespace() throws IOException, SAXException {

        // Can use \n or XML escape character (&#10;) for new line
        String expectedXml =
                "<signature ac='PF00001' xmlns='http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5'>\n" +
                "    <abstract>This family contains, amongst other G-protein-coupled receptors (GPCRs), " +
                "members of the opsin family, which have been considered to be typical members " +
                "of the rhodopsin superfamily. " +
                "&#10;&#10;" +
                "They share several motifs, mainly the seven transmembrane helices, " +
                "GPCRs of the rhodopsin superfamily. All opsins bind a chromophore, " +
                "such as 11-cis-retinal. " +
                "\n\n" +
                "The function of most opsins other than the photoisomerases is split into two steps: " +
                "light absorption and G-protein activation. Photoisomerases, on the other hand, are " +
                "not coupled to G-proteins - they are thought to generate and supply the chromophore " +
                "that is used by visual opsins [pubmed:15774036]</abstract>\n" +
                "</signature>";

        Signature actual = unmarshal(expectedXml);

        Signature expected = new Signature
                .Builder("PF00001")
                .abstractText(
                "This family contains, amongst other G-protein-coupled receptors (GPCRs), " +
                "members of the opsin family, which have been considered to be typical members " +
                "of the rhodopsin superfamily. " +
                "\n\n" +
                "They share several motifs, mainly the seven transmembrane helices, " +
                "GPCRs of the rhodopsin superfamily. All opsins bind a chromophore, " +
                "such as 11-cis-retinal. " +
                "\n\n" +
                "The function of most opsins other than the photoisomerases is split into two steps: " +
                "light absorption and G-protein activation. Photoisomerases, on the other hand, are " +
                "not coupled to G-proteins - they are thought to generate and supply the chromophore " +
                "that is used by visual opsins [pubmed:15774036]")
                .build();

        assertEquals(expected, actual);

        if (LOGGER.isDebugEnabled())    {
            LOGGER.debug(actual);
        }

        String actualXml = marshal(actual);
        assertXmlEquals(expectedXml, actualXml, "Whitespace test");

        if (LOGGER.isDebugEnabled())    {
            LOGGER.debug(actualXml);
        }

    }

    @Test
    public void testBuilder() throws IOException, ParseException {
        final String AC       = "SIG001";
        final String NAME     = "test";
        final String TYPE     = "domain";
        final String DESC     = NAME;
        final String ABSTRACT = NAME;
        final Date CREATED    = DateAdapter.toDate("2005-12-25");
        final Date UPDATED    = DateAdapter.toDate("2010-10-18");
        final String MD5      = "5ab17489095dd2836122eec0e91db82d";
        final String COMMENT  = "RELAXIN is a 6-element fingerprint that provides a signature for the relaxins.";
        SignatureLibraryRelease release = new SignatureLibraryRelease(SignatureLibrary.PHOBIUS, "1.0");
        Signature signature = new Signature.Builder(AC)
                .name(NAME)
                .type(TYPE)
                .description(NAME)
                .abstractText(ABSTRACT)
                .signatureLibraryRelease(release)
                .model(new Model.Builder("MOD001").md5("6bb17489095dd2836122eec0e91db85f").build())
                .model(new Model("MOD002"))
                .created(CREATED)
                .updated(UPDATED)
                .md5(MD5)
                .comment(COMMENT)
                .build();
        assertEquals(AC, signature.getAccession());
        assertEquals(NAME, signature.getName());
        assertEquals(TYPE, signature.getType());
        assertEquals(DESC, signature.getDescription());
        assertEquals(ABSTRACT, signature.getAbstract());
        assertEquals(CREATED, signature.getCreated());
        assertEquals(UPDATED, signature.getUpdated());
        assertEquals(MD5, signature.getMd5());
        assertEquals(release, signature.getSignatureLibraryRelease());
        assertEquals(2, signature.getModels().size());
        if (LOGGER.isDebugEnabled())    {
            LOGGER.debug(signature);
            LOGGER.debug(super.marshal(signature));
        }
    }

    @Test public void testRemoveModel()   {
        Signature signature = new Signature("SIG001");
        Model m1 = signature.addModel(new Model("MOD001"));
        Model m2 = signature.addModel(new Model("MOD002"));
        // Should be OK
        assertEquals(2, signature.getModels().size());
        // Should be OK (key not recognised, so just ignores)
        signature.removeModel(new Model("UNKNOWN"));
        assertEquals(2, signature.getModels().size());
        // Should fail
        try {
            signature.removeModel(null);
        }
        catch (Exception e)    {
            assertTrue(e instanceof NullPointerException);
        }
        // Should be OK
        signature.removeModel(m1);
        assertEquals(1, signature.getModels().size());
        signature.removeModel(m2);
        assertEquals(0, signature.getModels().size());
    }

    @Test
    public void testSignatureLibraryReleaseShallow() throws IOException, SAXException {

        super.testSupportsMarshalling(Signature.class);

        // Expected XML
        String expectedXml =
            "<signature xmlns='http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5' ac='G3DSA:2.40.50.140' name='Nucleic acid-binding proteins'>" +
            "   <signature-library-release library='GENE3D' version='3.1.0'/>" +
            "</signature>";

        // Expected object
        Signature expectedObject = new Signature("G3DSA:2.40.50.140", "Nucleic acid-binding proteins");
        SignatureLibraryRelease release = new SignatureLibraryRelease(SignatureLibrary.GENE3D, "3.1.0");
        expectedObject.setSignatureLibraryRelease(release);

        // Actual
        Signature actualObject = super.unmarshal(expectedXml);
        String actualXml = super.marshal(actualObject);

        // Check
        assertEquals(expectedObject, actualObject, "Objects do not match");
        assertXmlEquals( expectedXml, actualXml, "XML does not match");

    }

}
