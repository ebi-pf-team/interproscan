/*
 * Copyright 2010 the original author or authors.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * InterPro Consortium-specific tests cases for {@link uk.ac.ebi.interpro.scan.model.Protein}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ConsortiumProteinTest extends AbstractTest<Protein> {

    @Test public void testXml() throws IOException, SAXException {
        super.testSupportsMarshalling(Protein.class);
        super.testXmlRoundTrip();
    }

//    @ Test
//    public void testPfam()  {
//        Protein p = new Protein("MDFFVRLARETGDRKREFLELGRKAGRFPAASTSNGEISIWCSNDYLGMGQHPDVLDAMKRSVDEYGGGSGGS" +
//                "RNTGGTNHFHVALEREPAEPHGKEDAVLFTSGYSANEGSLSVLAGAVDDCQVFSDSANHASIIDGLRHSGARK" +
//                "HVFRHKDGRHLEELLAAADRDKPKFIALESVHSMRGDIALLAEIAGLAKRYGAVTFLDEVHAVGMYGPGGAGI" +
//                "AARDGVHCEFTVVMGTLAKAFGMTGGYVAGPAVLMDAVRARARSFVFTTALPPAVAAGALAAVRHLRGSDEER" +
//                "RRPAENARLTHGLLRERDIPVLSDRSPIVPVLVGEDRMCKRMSALPLERHGAYVQAIDAPSVPAGEEILRIAP" +
//                "SAVHETEEIHRFVDALDGIWSELGAARRV");
//        // p.addCrossReference(new Xref("A0A000", "A0A000_9ACTO"));
//        p.addCrossReference(new Xref("A0A000"));
//        SignatureLibraryRelease release = new SignatureLibraryRelease(SignatureLibrary.PFAM, "14");
//        Signature signature = new Signature.Builder("PF00155").
//                name("Aminotran_1_2").type("Domain").signatureLibraryRelease(release).build();
//        Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<Hmmer3Match.Hmmer3Location>();
//        locations.add(new Hmmer3Match.Hmmer3Location(37, 381, 206.7, 3.7E-67, 6, 363, HmmBounds.N_TERMINAL_COMPLETE, 41, 381));
//        p.addMatch(new Hmmer3Match(signature, 206.7, 3.7E-67, locations));
//    }

}