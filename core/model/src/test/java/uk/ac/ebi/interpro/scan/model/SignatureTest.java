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
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * Test cases for {@link Signature}
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SignatureTest {

    // TODO: Add SuperFamily SSF53098
    // TODO: Add Xrefs and Entry to Signature
    // TODO: Add Model MD5?

    @Resource
    private Marshaller marshaller;

    @Resource
    private Unmarshaller unmarshaller;

    // TODO: Compare constructed Pfam signature to expected XML in Spring context
    @Resource
    private Map<String, ObjectXmlPair<Signature>> signatureXmlMap;

    private XmlUnitSupport support;

    @Before public void init()  {
        support = new XmlUnitSupport(marshaller, unmarshaller);
        support.init();
    }

    @Test public void testBuilder()   {
        final String AC       = "SIG001";
        final String NAME     = "test";
        final String TYPE     = "domain";
        final String DESC     = NAME;
        final String ABSTRACT = NAME;
        SignatureDatabaseRelease release = new SignatureDatabaseRelease(new SignatureProvider("TST"), "1.0");
        Signature signature = new Signature.Builder(AC)
                .name(NAME)
                .type(TYPE)
                .description(NAME)
                .abstractText(NAME)
                .signatureDatabaseRelease(release)
                .build();
        assertEquals(AC, signature.getAccession());
        assertEquals(NAME, signature.getName());
        assertEquals(TYPE, signature.getType());
        assertEquals(DESC, signature.getDescription());
        assertEquals(ABSTRACT, signature.getAbstract());
        assertEquals(release, signature.getSignatureDatabaseRelease());
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

    @Test public void testSupportsMarshalling() {
        support.testSupportsMarshalling(Signature.class);
    }    

    /**
     * Runs representative test of HMM database.
     *
     * @throws IOException   if problem marshalling or unmarshalling
     * @throws SAXException  if cannot parse expected or actual XML
     */
    @Test public void testGene3D() throws IOException, SAXException {

        // CATHCODE in CathDomainDescriptionFile
        final String AC         = "G3DSA:1.10.10.10";
        // HOMOL in CathDomainDescriptionFile (see also [http://www.cathdb.info/cathnode/1.10.10.10])        
        final String NAME       = "\"winged helix\" repressor DNA binding domain";
        // Always "domain" -- supplied by Gene3D and *not* same as EntryType (InterPro-specific)
        final String TYPE       = "domain";
        // Do we ever have a description?
        final String DESC       = "";
        // Do we ever get an abstract from Gene 3D?
        final String ABSTRACT   = "";
        // Representative model (has name and description)
        final String MODEL_AC   = "1bm9A00";
        // Same as signature.name
        final String MODEL_NAME = NAME;
        final String MODEL_DESC = "Replication terminator protein. Chain: a, b. Synonym: rtp, ter. " +
                                  "Engineered: yes. Biological_unit: dimer and a dimer of dimers";
        // Additional models (we just have accession)
        final String[] OTHER_MODELS   = {"1iw7F03", "1ri7A01"};

        // Expected object
        Signature expectedSignature = new Signature
                .Builder(AC).name(NAME).type(TYPE).description(DESC).abstractText(ABSTRACT).build();
        expectedSignature.addModel(new Model.Builder(MODEL_AC).name(MODEL_NAME).description(MODEL_DESC).build());
        for (String ac : OTHER_MODELS)  {
            expectedSignature.addModel(new Model(ac));
        }        

        // Expected XML
        String xml =
                "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n" +
                "<signature ac='" + AC + "' type='" + TYPE + "' " +
                        "name='" + StringEscapeUtils.escapeXml(NAME) + "' " +
                        "desc='" + StringEscapeUtils.escapeXml(DESC) + "' " +
                        "xmlns='http://www.ebi.ac.uk/schema/interpro/scan/model'>\n" +
                "\t<abstract>" + ABSTRACT + "</abstract>\n" +
                "\t\t<models>\n" +
                "\t\t\t<model ac='" + MODEL_AC + "' " +
                        "name='" + StringEscapeUtils.escapeXml(MODEL_NAME) + "' " +
                        "desc='" + StringEscapeUtils.escapeXml(MODEL_DESC) + "'/>\n";
        for (String ac : OTHER_MODELS)  {
            xml += "\t\t\t<model ac='" + ac + "'/>\n";
        }
        xml += "\t\t</models>\n" +
                "</signature>";

        // Unmarshall
        Signature signature = (Signature) support.testUnmarshal("Gene3D", xml, expectedSignature);

        // TODO: Add persistence tests

        // Marshall
        support.testMarshal("Gene3D", signature, xml);
        
    }

    @Test public void testOtherMemberDatabases() throws IOException, SAXException {
        for (String key : signatureXmlMap.keySet()) {
            // TODO: Make getObject() generic so can guarantee that only Signature objects are present
            Signature signature = signatureXmlMap.get(key).getObject();
            String    xml       = signatureXmlMap.get(key).getXml();
            // Test
            support.marshal(signature);
            support.testUnmarshal(key, xml, signature);
            support.testMarshal(key, signature, xml);
        }
    }


    /*  ========= NOTES =========

    ---- GENE 3D ----
     
    To populate database we just need CathDomainDescriptionFile to:
    1. Create Signature using CATHCODE as accession and HOMOL as name
    2. Create Model using DOMAIN as accession and Signature created in step 1
    3. When persist in DAO, check if Signature already exists -- if not, create -- and
       check the association between Model and Signature is persisted
    This can be done using Spring Batch FileItemReader, using "//" as record separator

    HMM model file only contains NAME (in Onion this is called method_ac), e.g. "1o7iB00"
    (http://www.ebi.ac.uk/~aquinn/projects/jira/IBU-804/1o7iB00.hmm)
    Nothing about Gene3D in MARCH schema (only covers Pfam, PIRSF, SMART and TIGRFAM)
    Note: MARCH contains MD5 of model! (HMM_MODEL.HMM)

    Got following sample from http://release.cathdb.info/v3.1.0/CathDomainDescriptionFile:

    FORMAT    CDDF1.0
    DOMAIN    1bm9A00
    VERSION   3.1.0
    VERDATE   20-Jan-2007
    NAME      Replication terminator protein. Chain: a, b. Synonym: rtp, ter. Engine
    NAME      ered: yes. Biological_unit: dimer and a dimer of dimers
    SOURCE    Bacillus subtilis. Gene: rtp.  Expressed in: escherichia coli.
    CATHCODE  1.10.10.10
    CLASS     Mainly Alpha
    ARCH      Orthogonal Bundle
    TOPOL     Arc Repressor Mutant, subunit A
    HOMOL     "winged helix" repressor DNA binding domain
    DLENGTH   120
    DSEQH     >pdb|1bm9A00
    DSEQS     EEKRSSTGFLVKQRAFLKLYMITMTEQERLYGLKLLEVLRSEFKEIGFKPNHTEVYRSLHELLDDGILKQ
    DSEQS     IKVKKEGAKLQEVVLYQFKDYEAAKLYKKQLKVELDRCKKLIEKALSDNF
    NSEGMENTS 1
    SEGMENT   1bm9A00:1:1
    SRANGE    START=3  STOP=122
    SLENGTH   120
    SSEQH     >pdb|1bm9A00:1:1
    SSEQS     EEKRSSTGFLVKQRAFLKLYMITMTEQERLYGLKLLEVLRSEFKEIGFKPNHTEVYRSLHELLDDGILKQ
    SSEQS     IKVKKEGAKLQEVVLYQFKDYEAAKLYKKQLKVELDRCKKLIEKALSDNF
    ENDSEG
    //
    
    */

}