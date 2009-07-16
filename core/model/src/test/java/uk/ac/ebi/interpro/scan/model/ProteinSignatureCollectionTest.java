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

import org.junit.Test;
import junit.framework.TestCase;

/**
 * Test cases for ProteinSignatureCollection
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     ProteinSignatureCollection
 */
public final class ProteinSignatureCollectionTest extends TestCase {

    // TODO: Add tests for most (all?) classes in model

    // http://www.uniprot.org/uniprot/A0A000.fasta
    private static final String A0A000_SEQ = "MDFFVRLARETGDRKREFLELGRKAGRFPAASTSNGEISIWCSNDYLGMGQHPDVLDAMK\n" +
        "RSVDEYGGGSGGSRNTGGTNHFHVALEREPAEPHGKEDAVLFTSGYSANEGSLSVLAGAV\n" +
        "DDCQVFSDSANHASIIDGLRHSGARKHVFRHKDGRHLEELLAAADRDKPKFIALESVHSM\n" +
        "RGDIALLAEIAGLAKRYGAVTFLDEVHAVGMYGPGGAGIAARDGVHCEFTVVMGTLAKAF\n" +
        "GMTGGYVAGPAVLMDAVRARARSFVFTTALPPAVAAGALAAVRHLRGSDEERRRPAENAR\n" +
        "LTHGLLRERDIPVLSDRSPIVPVLVGEDRMCKRMSALPLERHGAYVQAIDAPSVPAGEEI\n" +
        "LRIAPSAVHETEEIHRFVDALDGIWSELGAARRV";

    // http://www.uniprot.org/uniprot/P02700.fasta
    private static final String P02700_SEQ = "MNGTEGPNFYVPFSNKTGVVRSPFEAPQYYLAEPWQFSMLAAYMFLLIVLGFPINFLTLY\n" +
            "VTVQHKKLRTPLNYILLNLAVADLFMVFGGFTTTLYTSLHGYFVFGPTGCNLEGFFATLG\n" +
            "GEIALWSLVVLAIERYVVVCKPMSNFRFGENHAIMGVAFTWVMALACAAPPLVGWSRYIP\n" +
            "QGMQCSCGALYFTLKPEINNESFVIYMFVVHFSIPLIVIFFCYGQLVFTVKEAAAQQQES\n" +
            "ATTQKAEKEVTRMVIIMVIAFLICWLPYAGVAFYIFTHQGSDFGPIFMTIPAFFAKSSSV\n" +
            "YNPVIYIMMNKQFRNCMLTTLCCGKNPLGDDEASTTVSKTETSQVAPA";

    @Test public void testRemoveProtein()   {
        ProteinSignatureCollection collection = new ProteinSignatureCollection();
        Protein p1 = collection.addProtein(new Protein(A0A000_SEQ));
        Protein p2 = collection.addProtein(new Protein(P02700_SEQ));
        // Should be OK
        assertEquals("Should have two proteins", 2, collection.getProteins().size());
        // Should be OK (key not recognised, so just ignores)
        collection.removeProtein(new Protein("UNKNOWN"));
        assertEquals("Should have two proteins", 2, collection.getProteins().size());
        // Should fail
        try {
            collection.removeProtein(null);
        }
        catch (Exception e)    {
            assertTrue("Should be NullPointerException", e instanceof NullPointerException);
        }
        // Should be OK
        collection.removeProtein(p1);
        assertEquals("Should have one protein", 1, collection.getProteins().size());
        collection.removeProtein(p2);
        assertEquals("Should have no proteins", 0, collection.getProteins().size());        
    }

    @Test public void testRemoveSignatureDatabaseRelease()   {
        ProteinSignatureCollection collection = new ProteinSignatureCollection();
        SignatureDatabaseRelease s1 = collection.addSignatureDatabaseRelease(
                new SignatureDatabaseRelease(new SignatureProvider("Pfam"), "23.0"));
        SignatureDatabaseRelease s2 = collection.addSignatureDatabaseRelease(
                new SignatureDatabaseRelease(new SignatureProvider("PRINTS"), "38.1"));
        // Should be OK
        assertEquals("Should have two releases", 2, collection.getSignatureDatabaseReleases().size());
        // Should be OK (key not recognised, so just ignores)
        collection.removeSignatureDatabaseRelease(new SignatureDatabaseRelease(new SignatureProvider("??"), "??"));
        assertEquals("Should have two releases", 2, collection.getSignatureDatabaseReleases().size());
        // Should fail
        try {
            collection.removeSignatureDatabaseRelease(null);
        }
        catch (Exception e)    {
            assertTrue("Should be NullPointerException", e instanceof NullPointerException);
        }
        // Should be OK
        collection.removeSignatureDatabaseRelease(s1);
        assertEquals("Should have one release", 1, collection.getSignatureDatabaseReleases().size());
        collection.removeSignatureDatabaseRelease(s2);
        assertEquals("Should have no releases", 0, collection.getSignatureDatabaseReleases().size());
    }

    
}
