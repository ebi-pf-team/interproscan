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

import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Writer;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import junit.framework.TestCase;

/**
 * Test cases for JAXB marshalling and unmarshalling of any class with an @XmlRootElement annotation.
 *
 * @author  Antony Quinn
 * @version $Id$ 
 * @since   1.0
 * @see     Model
 */
public class JaxbTest extends TestCase {
    
    private static final Log logger = LogFactory.getLog(JaxbTest.class);


    @Test
    public void testGene3D()  {
        // Create
    }


    @Test public void testMarshallSignatureDatabaseRelease() throws JAXBException {
        // Pfam
        marshal(getSdrPfamSample(), JAXBContext.newInstance(SignatureDatabaseRelease.class));
        // PRINTS
        marshal(getSdrPrintsSample(), JAXBContext.newInstance(SignatureDatabaseRelease.class));
    }

    @Test public void testUnmarshallSignatureDatabaseRelease() throws JAXBException {
        // TODO: Write unmarshall test
    }

    @Test public void testMarshallProteinSignatureCollection() throws JAXBException {
        // Pfam
        marshal(getPscPfamSample(), JAXBContext.newInstance(ProteinSignatureCollection.class));
        // PRINTS
        marshal(getPscPrintsSample(), JAXBContext.newInstance(ProteinSignatureCollection.class));
    }

    @Test public void testUnmarshallProteinSignatureCollection() throws JAXBException {
        // TODO: Find why unmarshall failing - tested Model (see code below), now test other classes in isolation
        String xml = "";
//        ProteinSignatureCollection psc = (ProteinSignatureCollection)unmarshal(
//                                                    xml,
//                                                    JAXBContext.newInstance(ProteinSignatureCollection.class)
//                                          );
    }

    /*
    @Test public void testMarshall() throws JAXBException {
        Model model = new Model.Builder("PF00533").name("BRCT").build();
        String xml = Utils.marshal(model, JAXBContext.newInstance(Model.class));
        // Deserialise
        Model modelU = (Model)Utils.unmarshal(xml, JAXBContext.newInstance(Model.class));
    }
    */

    /**
     * Marshalls object using context, optionally writing XML to console if
     * the system property test.log.level equals DEBUG.
     *
     * @param  object
     * @param  context
     * @throws JAXBException if could not create marshaller or marshall output.
     */
    private String marshal(Object object, JAXBContext context) throws JAXBException {
        // Serialise
        Writer writer = new StringWriter();
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(object, writer);
        logger.debug(writer.toString());
        return writer.toString();
    }

    /**
     * Unmarshalls XML.
     *
     * @param xml
     * @param context
     * @return
     * @throws JAXBException
     */
    private Object unmarshal(String xml, JAXBContext context) throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //unmarshaller.setSchema("");
        return unmarshaller.unmarshal(new StringReader(xml));
    }    

    private ProteinSignatureCollection getPscPfamSample()   {

        ProteinSignatureCollection collection = new ProteinSignatureCollection();

        // Add a protein with a public identifier
        Protein protein = collection.addProtein(new Protein("MDFFVRLARETGDRKREFLELGRKAGRFPAASTSNGEISIWCS"));
        Model model = new Model.Builder("PF00155").name("Aminotran_1_2").build();
        RawHmmMatch rawMatch = protein.addRawMatch(new RawHmmMatch(model, 0.035, 4.3e-61));
        rawMatch.addLocation(new HmmLocation(36, 381, 89.0, 4.3e-61, 1, 325, HmmLocation.HmmBounds.parseSymbol("[]")));

        // ... and one with no public identifier
        protein = collection.addProtein(new Protein("MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQCPLCKNDI"));
        model = new Model.Builder("PF00533").name("BRCT").build();
        rawMatch = protein.addRawMatch(new RawHmmMatch(model, 0.035, 4.3e-61 + 1.5e-40));
        // TODO: Disallow add and remove and force location in constructor like filtered match?
        rawMatch.addLocation(new HmmLocation(1642, 1723, 89.0, 4.3e-61, 1, 81, HmmLocation.HmmBounds.parseSymbol("[.")));
        rawMatch.addLocation(new HmmLocation(1756, 1842, 45.8, 1.5e-40, 1, 88, HmmLocation.HmmBounds.parseSymbol("..")));

        // Add filtered B12-binding match
        Signature signature = new Signature.Builder("PF02310").name("B12-binding").build();
        Set<HmmLocation> locations = new HashSet<HmmLocation>();
        locations.add(new HmmLocation(3, 107, 3.0, 7.6e-08, 1, 104, HmmLocation.HmmBounds.parseSymbol("[.")));        
        FilteredHmmMatch filteredMatch = protein.addFilteredMatch(new FilteredHmmMatch(signature, 0.035, 3.7e-09, locations));

        return collection;

    }

    private ProteinSignatureCollection getPscPrintsSample()   {

        ProteinSignatureCollection collection = new ProteinSignatureCollection();

        // Add a protein with a public identifier
        // TODO: Add P02700 and OPSD_SHEEP as xrefs
        Protein protein = collection.addProtein(new Protein("MNGTEGPNFYVPFSNKTGVVRSPFEAPQYYLAEPWQFSMLAAYMFLLIVLGFPIN"));

        // Add raw RHODOPSIN match
        Model model = new Model.Builder("PR00579").name("RHODOPSIN").build();
        RawFingerPrintsMatch rawMatch = protein.addRawMatch(new RawFingerPrintsMatch(model, 2.2e-64));
        rawMatch.addLocation(new FingerPrintsLocation(3, 22, 1.45e-14));
        rawMatch.addLocation(new FingerPrintsLocation(22, 39, 2.06e-10));
        rawMatch.addLocation(new FingerPrintsLocation(85, 102, 1.83e-13));
        rawMatch.addLocation(new FingerPrintsLocation(191, 208, 1.43e-10));
        rawMatch.addLocation(new FingerPrintsLocation(271, 290, 9.33e-15));
        rawMatch.addLocation(new FingerPrintsLocation(319, 333, 1.04e-09));

        // Add raw OPSIN match
        model = new Model.Builder("PR00238").name("OPSIN").build();
        rawMatch = protein.addRawMatch(new RawFingerPrintsMatch(model, 3.362519e-19));
        rawMatch.addLocation(new FingerPrintsLocation(60, 73, 3.59e-09));
        rawMatch.addLocation(new FingerPrintsLocation(174, 187, 8.34e-08));
        rawMatch.addLocation(new FingerPrintsLocation(285, 298, 1.48e-09));

        // Add filtered NUCEPIMERASE match
        Signature signature = new Signature.Builder("PR01713").name("NUCEPIMERASE").build();
        FilteredFingerPrintsMatch filteredMatch = protein.addFilteredMatch(new FilteredFingerPrintsMatch(signature, 3.7e-09));
        filteredMatch.addLocation(new FingerPrintsLocation(220, 235, 3.7e-09));
        filteredMatch.addLocation(new FingerPrintsLocation(243, 258, 3.7e-09));

        return collection;

    }

    private SignatureDatabaseRelease getSdrPfamSample()   {

       SignatureDatabaseRelease release = new SignatureDatabaseRelease(new SignatureProvider("Pfam"), "23.0");

       // Add signature with abstract
       Signature signature = release.addSignature(new Signature
               .Builder("PF00001")
               .name("7tm_2")
               .type("Family")
               .description("7 transmembrane receptor (rhodopsin family)")
               .abstractText(
                   "This family contains, amongst other G-protein-coupled receptors (GPCRs), " +
                   "members of the opsin family, which have been considered to be typical members of the " +
                   "rhodopsin superfamily. They share several motifs, mainly the seven transmembrane helices, " +
                   "GPCRs of the rhodopsin superfamily. All opsins bind a chromophore, such as 11-cis-retinal. " +
                   "The function of most opsins other than the photoisomerases is split into two steps: " +
                   "light absorption and G-protein activation. Photoisomerases, on the other hand, are not coupled to " +
                   "G-proteins - they are thought to generate and supply the chromophore that is used by visual opsins" +
                   "[pubmed:15774036]"
                )
                .build());
       // Could add separate LS and FS models here
       signature.addModel(new Model("PF00001"));

       // Add signature without abstract
       signature = release.addSignature(new Signature.Builder("PF00155").name("Aminotran_1_2").type("Family")
                                                    .description("Aminotransferase class I and II").build());
       signature.addModel(new Model("PF00155"));

       return release;

   }

    private SignatureDatabaseRelease getSdrPrintsSample()   {

       SignatureDatabaseRelease release = new SignatureDatabaseRelease(new SignatureProvider("PRINTS"), "38.1");

       // Add model with abstract
       Signature signature = release.addSignature(new Signature
               .Builder("PR00579")
               .name("RHODOPSIN")
               .type("Family")
               .abstractText(
                   "Opsins, the light-absorbing molecules that mediate vision [1,2], are integral " +
                   "membrane proteins that belong to a superfamily of G protein-coupled receptors (GPCRs).")
               .build());
       signature.addModel(new Model("PR00579"));

       // Add signature without abstract
       signature = release.addSignature(new Signature.Builder("PR00238").name("OPSIN").type("Family")
                                                    .description("Opsin signature").build());
       signature.addModel(new Model("PR00238"));

       return release;

   }

}
