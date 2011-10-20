package uk.ac.ebi.interpro.scan.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;

import javax.annotation.Resource;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link ProteinViewController}
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinViewControllerTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    @Test
    public void testProtein()    {
        ProteinViewController c = new ProteinViewController();
        c.setEntryHierarchy(entryHierarchy);
        c.proteinFeatures("P38398");
    }

    @Test
    public void testSimpleEntrySort() {
        List<SimpleEntry> entries = new ArrayList<SimpleEntry>();

        SimpleEntry entry1 = new SimpleEntry("IPR000001", "Kringle", "Name not available", "Domain");
        List<SimpleLocation> locations1 = new ArrayList<SimpleLocation>();
        SimpleLocation location1 = new SimpleLocation(55, 66);
        locations1.add(location1);
        entry1.setLocations(locations1);
        entries.add(entry1);

        SimpleEntry entry2 = new SimpleEntry(null, "Unintegrated", "Name not available", null);
        List<SimpleLocation> locations2 = new ArrayList<SimpleLocation>();
        SimpleLocation location2 = new SimpleLocation(33, 44);
        locations2.add(location2);
        entry2.setLocations(locations2);
        entries.add(entry2);

        SimpleEntry entry3 = new SimpleEntry("IPR000007", "Tubby_C", "Name not available", "Domain");
        List<SimpleLocation> locations3 = new ArrayList<SimpleLocation>();
        SimpleLocation location3 = new SimpleLocation(11, 22);
        locations3.add(location3);
        entry3.setLocations(locations3);
        entries.add(entry3);

        // Test the entries sort correctly
        Collections.sort(entries);
        assertEquals(3, entries.size());
        assertEquals("IPR000007", entries.get(0).getAc());
        assertEquals("IPR000001", entries.get(1).getAc());
        assertEquals(null, entries.get(2).getAc());
    }

    /**
     * Returns protein for given accession number
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return Protein for given accession
     */
    private Protein sampleProtein(String ac) {

        // Create protein
        Protein p = new Protein.Builder("MPTIKQLIRNARQPIRNVTKSPALRGCPQRRGTCTRVYTITPKKPNSALRKVARVRLTSG\n" +
                "FEITAYIPGIGHNLQEHSVVLVRGGRVKDLPGVRYHIVRGTLDAVGVKDRQQGRSKYGVK\n" +
                "KPK")
                .crossReference(new ProteinXref("UniProt", "A0A314", "RR12_COFAR", "30S ribosomal protein S12, chloroplastic"))
                .build();

        // Add matches
        Set<Hmmer3Match.Hmmer3Location> l1 = new HashSet<Hmmer3Match.Hmmer3Location>();
        l1.add(new Hmmer3Match.Hmmer3Location(1, 123, -8.9, 0.28, 63, 82, 114, 73, 94));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("G3DSA:2.40.50.140")
                        .name("Nucleic acid-binding proteins")
                        .entry(new Entry.Builder("IPR012340")
                                .description("Nucleic acid-binding, OB-fold")
                                .type(EntryType.DOMAIN)
                                .build())
                        .build(),
                -8.9, 0.28, l1));


        Entry entry = new Entry.Builder("IPR016027")
                .description("Nucleic acid-binding, OB-fold-like")
                .type(EntryType.DOMAIN)
                .build();
        Set<Hmmer3Match.Hmmer3Location> l2 = new HashSet<Hmmer3Match.Hmmer3Location>();
        l2.add(new Hmmer3Match.Hmmer3Location(2, 123, -8.9, 0.28, 63, 82, 114, 73, 94));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("SSF50249")
                        .name("Nucleic_acid_OB")
                        .entry(entry)
                        .build(),
                -8.9, 0.28, l2));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("SSF50250")
                        .name("Made up name")
                        .entry(entry)
                        .build(),
                -8.9, 0.28, l2));

        return p;

    }
    

}
