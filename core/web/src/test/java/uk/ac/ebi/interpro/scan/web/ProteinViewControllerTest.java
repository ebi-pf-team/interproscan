package uk.ac.ebi.interpro.scan.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.web.io.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import javax.annotation.Resource;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ProteinViewController}
 *
 * @author Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinViewControllerTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    @Resource
    private CreateSimpleProteinFromMatchData matchData;

    @Test
    public void testProtein() {
        ProteinViewController c = new ProteinViewController();
        c.setEntryHierarchy(entryHierarchy);
        c.setMatchData(matchData);
        c.proteinBody("P38398");
    }

    @Test
    public void testSimpleProteinValueOf() {
        final Protein protein = sampleProtein();
        SimpleProtein sp = SimpleProtein.valueOf(protein, protein.getCrossReferences().iterator().next(), entryHierarchy);
        assertEquals("A0A314", sp.getAc());
    }

    @Test
    public void testSimpleEntrySort() {
        List<SimpleEntry> entries = new ArrayList<SimpleEntry>();

        SimpleEntry entry1 = new SimpleEntry("IPR000001", "Kringle", "Name not available", uk.ac.ebi.interpro.scan.web.model.EntryType.DOMAIN, this.entryHierarchy);
        List<SimpleLocation> locations1 = new ArrayList<SimpleLocation>();
        SimpleLocation location1 = new SimpleLocation(55, 66);
        locations1.add(location1);
        entry1.setLocations(locations1);
        entries.add(entry1);

        SimpleEntry entry2 = new SimpleEntry(null, "Unintegrated", "Name not available", null, null);
        List<SimpleLocation> locations2 = new ArrayList<SimpleLocation>();
        SimpleLocation location2 = new SimpleLocation(33, 44);
        locations2.add(location2);
        entry2.setLocations(locations2);
        entries.add(entry2);

        SimpleEntry entry3 = new SimpleEntry("IPR000007", "Tubby_C", "Name not available", uk.ac.ebi.interpro.scan.web.model.EntryType.DOMAIN, this.entryHierarchy);
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
     * Returns sample protein
     *
     * @return Sample protein
     */
    private Protein sampleProtein() {

        // TODO: This is a really bad example -- we should replace this with a an XML file containing real protein data injected as a Spring resource

        // Create protein
        Protein p = new Protein.Builder("MPTIKQLIRNARQPIRNVTKSPALRGCPQRRGTCTRVYTITPKKPNSALRKVARVRLTSG\n" +
                "FEITAYIPGIGHNLQEHSVVLVRGGRVKDLPGVRYHIVRGTLDAVGVKDRQQGRSKYGVK\n" +
                "KPK")
                .crossReference(new ProteinXref("UniProt", "A0A314", "RR12_COFAR", "30S ribosomal protein S12, chloroplastic"))
                .build();

        // Signature library
        SignatureLibraryRelease release = new SignatureLibraryRelease(SignatureLibrary.GENE3D, "3.0");

        // Add matches
        Set<Hmmer3Match.Hmmer3Location> l1 = new HashSet<Hmmer3Match.Hmmer3Location>();
        l1.add(new Hmmer3Match.Hmmer3Location(1, 123, -8.9, 0.28, 63, 82, HmmBounds.INCOMPLETE, 73, 94));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("G3DSA:2.40.50.140")
                        .name("Nucleic acid-binding proteins")
                        .signatureLibraryRelease(release)
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
        l2.add(new Hmmer3Match.Hmmer3Location(2, 123, -8.9, 0.28, 63, 82, HmmBounds.INCOMPLETE, 73, 94));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("SSF50249")
                        .name("Nucleic_acid_OB")
                        .signatureLibraryRelease(release)
                        .entry(entry)
                        .build(),
                -8.9, 0.28, l2));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("SSF50250")
                        .name("Made up name")
                        .signatureLibraryRelease(release)
                        .entry(entry)
                        .build(),
                -8.9, 0.28, l2));

        return p;

    }


}
