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


import org.apache.commons.lang.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;


/**
 * Test cases for {@link uk.ac.ebi.interpro.scan.model.NucleicAcidMatchesHolder}
 *
 * @author Maxim Scheremetjew
 * @author Gift Nuka
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class NucleicAcidMatchesHolderTest extends AbstractTest<NucleicAcidMatchesHolder> {

    private static final Logger LOGGER = LogManager.getLogger(NucleicAcidMatchesHolderTest.class.getName());

    @Test
    public void testEquals() throws IOException, ParseException {
        // Original
        NucleicAcidMatchesHolder original = getPfamObject();
        // Copy
        NucleicAcidMatchesHolder copy = (NucleicAcidMatchesHolder) SerializationUtils.clone(original);
        // Should be equal
        assertEquals( original, copy,"Original and copy should be equal");
        // Print
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(original.toString());
            LOGGER.debug(super.marshal(original));
        }
    }

    @Test
    @Disabled("Round trip does not work.  The embedded SignatureLibraryRelease element is not parsed.")
    public void testXml() throws IOException, SAXException {
        super.testSupportsMarshalling(NucleicAcidMatchesHolder.class);
        super.testXmlRoundTrip();
    }

    private NucleicAcidMatchesHolder getPfamObject() {
        //Create nucleic acid
        NucleotideSequence nucleotideSequence = new NucleotideSequence("cgcugggcauugcgccgugggcgguggaa");
        nucleotideSequence.addCrossReference(new NucleotideSequenceXref("databaseName", "identifier", "name"));
        //Create protein
        Protein protein = new Protein("MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQCPLCKNDI");
        Signature signature = new Signature("PF02310", "B12-binding");
        protein.addCrossReference(new ProteinXref("A0A000_9ACTO"));
        //Create ORF
        OpenReadingFrame orf = new OpenReadingFrame(10, 50, NucleotideSequenceStrand.SENSE);
        protein.addOpenReadingFrame(orf);
        nucleotideSequence.addOpenReadingFrame(orf);
        //Create matches
        Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<Hmmer3Match.Hmmer3Location>();
        locations.add(new Hmmer3Match.Hmmer3Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.INCOMPLETE, 2, 108, true, DCStatus.CONTINUOUS));
        protein.addMatch(new Hmmer3Match(signature, "PF02310", 0.035, 3.7e-9, locations));
        // Create release
        SignatureLibraryRelease release = new SignatureLibraryRelease(SignatureLibrary.PFAM, "23");
        signature.setSignatureLibraryRelease(release);
        // Create holder
        NucleicAcidMatchesHolder holder = new NucleicAcidMatchesHolder("5.15-54.0");
        holder.addProtein(protein);
        return holder;
    }
}
