/*
 * Copyright 2011 the original author or authors.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link NucleotideSequence}
 *
 * @author Antony Quinn
 * @author Gift nuka
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class NucleotideSequenceTest extends AbstractTest<NucleotideSequence> {

    private static final Logger LOGGER = LogManager.getLogger(NucleotideSequenceTest.class.getName());

    @Test
    public void testEquals() throws IOException, ParseException {
        // Original
        NucleotideSequence original = getPfamObject();
        // Copy
        NucleotideSequence copy = (NucleotideSequence) SerializationUtils.clone(original);
        // Should be equal
        assertEquals(original, copy, "Original and copy should be equal");
        // Print
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(original);
            LOGGER.debug(super.marshal(original));
        }
    }

    @Test
    public void testXml() throws IOException, SAXException {
        super.testSupportsMarshalling(NucleotideSequence.class);
        super.testXmlRoundTrip();
    }

    @Test
    public void testIUPACNucleotideCharacters() {
        // This sequence contains all IPUAC nucleotide characters, so MUST be accepted as valid
        String sequence = "cccaaaaaaatgatgcttatacatrttattataaagtctagctcagagcttttgttttcaaty" +
                "ttctatggttctcacagagattttctccacaatgaatcttattactgaattcccaaattgggc" +
                "agctatagca--ttcgatttcgnntttgaatcaaaagagaagactcggccattgatactcgca" +
                "gggaaatgcagagctaacaatccaccgtctagtttctatgaattgcaaaacatgttacaaata" +
                "dttttattgaaaacaaaacacagaatccagakctggaaaagacamaagattactagcagaaca" +
                "tttacggatggagcagaacatttacggatggagccgtagaaatagtcgtctgtctatcgacag" +
                "aaatstaaatacaacagtgattttattgatgagatgcagtctttagtttgcabgacggcaaca" +
                "haaagctttuttctttcatcttaataaatagacttaaaaagaatgaaaccaaacatccaactg" +
                "tvtaaataaccacttgcacttgcagtcattaatccaatttaagaaaccaaaaatccccaaaaat" +
                "taaatagccctctgcaaaggttttcaccaagaagacccagatccagggaaattttctggttttt" +
                "ttttcatttcgataaaaattaaacaggcgaattaccgtaatggagcgacgactgccattgcggt" +
                "twaagaagaaattatggaatgggcagcaacggaggtaactgttctagaaggaggctcccatctc" +
                "tgtagaatcttcagtaatgaggtggcttttcgtttyycgcggtgagtgcctgtcgtgagaagaa" +
                "tgtacaaggaagg.accaacgatggaacca.ggtcacggcgttcagaacattgccgcctccgt";
        NucleotideSequence ns = new NucleotideSequence(sequence);


    }

    @Test
    public void testInvalidNucleotideCharacters() {
        // Create a sequence with some completely invalid characters
        assertThrows(IllegalArgumentException.class, () -> {
            NucleotideSequence ns = new NucleotideSequence("gggazatgcagagctaacaatccaccgtctagtttclatgaattgcaaaacatgttacaaata");
        });


    }

    private NucleotideSequence getPfamObject() {
        // Create nucleotide sequence
        NucleotideSequence ns = new NucleotideSequence("CCGGAAGTTATTCACATTTATATGCGGAACCTCATATAAAATGTGAAAAGAAGGAATGCATGGAATGAATATTGGACAAAAAGTACTGTTCGAACTTAAAA");
        // Create ORF
        OpenReadingFrame orf = new OpenReadingFrame(3, 63, NucleotideSequenceStrand.SENSE);
        orf.setProtein(getPfamProtein());
        ns.addCrossReference(new NucleotideSequenceXref("ENA", "AP009179", "AP009179.1"));
        ns.addOpenReadingFrame(orf);
        return ns;
    }

    private Protein getPfamProtein() {
        // Protein
        Protein p = new Protein("MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQCPLCKNDI");
        p.addCrossReference(new ProteinXref("A0A000_9ACTO"));
        // Entry
        Entry entry = new Entry("IPR006158", "Cobalamin (vitamin B12)-binding", EntryType.DOMAIN);
        Signature signature = new Signature("PF02310", "B12-binding");
        entry.addSignature(signature);
        // Super matches
        SuperMatch sm = new SuperMatch(entry);
        sm.addLocation(new SuperMatch.Location(1, 30));
        sm.addLocation(new SuperMatch.Location(45, 60));
        p.addSuperMatch(sm);
        // Matches;
        Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<>();
        locations.add(new Hmmer3Match.Hmmer3Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.INCOMPLETE, 2, 108, true, DCStatus.CONTINUOUS));
        p.addMatch(new Hmmer3Match(signature, "PF02310", 0.035, 3.7e-9, locations));
        return p;
    }

}
