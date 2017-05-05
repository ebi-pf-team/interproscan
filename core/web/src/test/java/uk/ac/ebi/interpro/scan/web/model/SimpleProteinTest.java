package uk.ac.ebi.interpro.scan.web.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for {@link uk.ac.ebi.interpro.scan.web.model.SimpleProtein}
 *
 * @author Maxim Scheremetjew , EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SimpleProteinTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    private Protein testProtein;

    private ProteinXref testProteinXref;

    private Match testMatch;

    private Hmmer2Match.Hmmer2Location hmmer2Location1;

    private Hmmer2Match.Hmmer2Location hmmer2Location2;

    private Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<Hmmer2Match.Hmmer2Location>();

    //Create a set of expected values
    private final String PROTEIN_SEQ = "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";
    private final String proteinXrefIdentifier = "Test_1";
    private final String signatureName = "B12-binding";
    private final String signatureAccession = "PF02310";

    @Before
    public void initBeforeEachTest() {
        //Instantiate protein object
        this.testProtein = new Protein(PROTEIN_SEQ);
        //Instantiate  protein Xref object
        this.testProteinXref = this.testProtein.addCrossReference(new ProteinXref(proteinXrefIdentifier));

        //Instantiate match with locations
        this.hmmer2Location1 = new Hmmer2Match.Hmmer2Location(3, 107, 3.0, 3.7e-9, 1, 104, HmmBounds.N_TERMINAL_COMPLETE);
        this.hmmer2Location2 = new Hmmer2Match.Hmmer2Location(120, 310, 3.0, 3.7e-9, 1, 104, HmmBounds.C_TERMINAL_COMPLETE);
        this.locations.add(hmmer2Location1);
        this.locations.add(hmmer2Location2);

        //Simple sanity checks
        Assert.assertEquals(PROTEIN_SEQ, testProtein.getSequence());
        Assert.assertEquals(proteinXrefIdentifier, testProteinXref.getIdentifier());
        Assert.assertEquals(3, hmmer2Location1.getStart());
        Assert.assertEquals(107, hmmer2Location1.getEnd());
        Assert.assertEquals(120, hmmer2Location2.getStart());
        Assert.assertEquals(310, hmmer2Location2.getEnd());
    }

    @Test
    public void testValueOfMethodForUnintegratedSignature() {
        //Add protein match
        testMatch = testProtein.addMatch(new Hmmer2Match(new Signature(signatureAccession, signatureName), 0.035, 3.7e-9, locations));

        //Simple sanity checks
        Assert.assertNotNull(testMatch.getSignature());
        Assert.assertEquals(signatureName, testMatch.getSignature().getName());
        Assert.assertEquals(signatureAccession, testMatch.getSignature().getAccession());
        Assert.assertNotNull(testMatch.getLocations());
        Assert.assertEquals(2, testMatch.getLocations().size());
        Assert.assertTrue(testMatch.getLocations().contains(hmmer2Location1));
        Assert.assertTrue(testMatch.getLocations().contains(hmmer2Location2));

        //Finally, test the valueOf method
        SimpleProtein simpleProtein = SimpleProtein.valueOf(testProtein, testProteinXref, entryHierarchy);

        //Test simple attributes
        Assert.assertNotNull(simpleProtein);
        Assert.assertEquals(proteinXrefIdentifier, simpleProtein.getAc());
        Assert.assertEquals("Unknown", simpleProtein.getId());
        Assert.assertEquals("Unknown", simpleProtein.getName());
        Assert.assertEquals(testProtein.getSequenceLength(), simpleProtein.getLength());
        Assert.assertEquals(testProtein.getMd5(), simpleProtein.getMd5());
        Assert.assertEquals("Unknown", simpleProtein.getCrc64());
        Assert.assertEquals("Unknown", simpleProtein.getTaxScienceName());
        Assert.assertEquals("Unknown", simpleProtein.getTaxFullName());
        Assert.assertFalse(simpleProtein.isProteinFragment());

        //Start to test complex attributes
        //First, check all entries
        Assert.assertEquals(1, simpleProtein.getAllEntries().size());
        //Second, check for integrated entries (there shouldn't be any)
        Assert.assertEquals(0, simpleProtein.getEntries().size());
        //Third, check for un-integrated entries
        Assert.assertEquals(1, simpleProtein.getUnintegratedSignatures().size());

        //Compare signature and simple signature attributes
        SimpleSignature simpleSignature = simpleProtein.getUnintegratedSignatures().get(0);
        Assert.assertEquals(testMatch.getSignature().getAccession(), simpleSignature.getAc());
        Assert.assertEquals(testMatch.getSignature().getName(), simpleSignature.getName());
        Assert.assertEquals(MatchDataSource.UNKNOWN.name(), simpleSignature.getDataSource().getName());

        //Test simple signature locations
        Assert.assertNotNull(simpleSignature.getLocations());
        Assert.assertEquals(2, simpleSignature.getLocations().size());
        for (SimpleLocation simpleLocation : simpleSignature.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                Assert.assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location1.getHmmLength(), simpleLocation.getLength());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                Assert.assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location2.getHmmLength(), simpleLocation.getLength());
            }
        }
        //Test simple entry
        Assert.assertNull("Entry signature isn't null! But the following tests require, that the signature entry is NULL.", testMatch.getSignature().getEntry());
        SimpleEntry simpleEntry = simpleProtein.getAllEntries().get(0);
        Assert.assertEquals("", simpleEntry.getAc());
        Assert.assertEquals("Unintegrated", simpleEntry.getShortName());
        Assert.assertEquals("Unintegrated", simpleEntry.getName());
        Assert.assertEquals(EntryType.UNKNOWN, simpleEntry.getType());
        Assert.assertEquals(entryHierarchy.getEntryHierarchyData(proteinXrefIdentifier), simpleEntry.getHierarchyData());
        Assert.assertEquals(entryHierarchy.getHierarchyLevel(proteinXrefIdentifier), simpleEntry.getHierarchyLevel());

        //Test simple entry locations
        Assert.assertNotNull(simpleEntry.getLocations());
        Assert.assertEquals(2, simpleEntry.getLocations().size());
        for (SimpleLocation simpleLocation : simpleEntry.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                Assert.assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location1.getHmmLength(), simpleLocation.getLength());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                Assert.assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location2.getHmmLength(), simpleLocation.getLength());
            }
        }
        Assert.assertEquals(1, simpleEntry.getSignaturesMap().size());
        Assert.assertTrue(simpleEntry.getSignaturesMap().containsValue(simpleSignature));
    }

    @Test
    public void testValueOfMethodForIntegratedSignature() {
        Signature pirsfSignature = new Signature.Builder("PIRSF001734").name("BRCA1").build();
        Signature pantherSignature = new Signature.Builder("PTHR13763").name("BRCA1").signatureLibraryRelease(new SignatureLibraryRelease(SignatureLibrary.PANTHER, "7.0")).build();

        Entry entry = buildTestEntry();
        entry.addSignature(pantherSignature);
        entry.addSignature(pirsfSignature);

        //Add protein match
        testMatch = testProtein.addMatch(new Hmmer2Match(pantherSignature, 0.035, 3.7e-9, locations));

        //Finally, test the valueOf method
        SimpleProtein simpleProtein = SimpleProtein.valueOf(testProtein, testProteinXref, entryHierarchy);

        //Test simple attributes
        Assert.assertNotNull(simpleProtein);
        Assert.assertEquals(proteinXrefIdentifier, simpleProtein.getAc());
        Assert.assertEquals("Unknown", simpleProtein.getId());
        Assert.assertEquals("Unknown", simpleProtein.getName());
        Assert.assertEquals(testProtein.getSequenceLength(), simpleProtein.getLength());
        Assert.assertEquals(testProtein.getMd5(), simpleProtein.getMd5());
        Assert.assertEquals("Unknown", simpleProtein.getCrc64());
        Assert.assertEquals("Unknown", simpleProtein.getTaxScienceName());
        Assert.assertEquals("Unknown", simpleProtein.getTaxFullName());
        Assert.assertFalse(simpleProtein.isProteinFragment());

        //Start to test complex attributes
        //First, check all entries
        Assert.assertEquals(1, simpleProtein.getAllEntries().size());
        //Second, check for integrated entries (there shouldn't be any)
        Assert.assertEquals(1, simpleProtein.getEntries().size());
        //Third, check for un-integrated entries
        Assert.assertEquals(0, simpleProtein.getUnintegratedSignatures().size());

        //Test simple entry
        Assert.assertNotNull("Entry signature is null! But the following tests require, that the signature entry is NOT NULL.", testMatch.getSignature().getEntry());
        SimpleEntry simpleEntry = simpleProtein.getAllEntries().get(0);
        Assert.assertEquals("IPR011364", simpleEntry.getAc());
        Assert.assertEquals("BRCA1", simpleEntry.getShortName());
        Assert.assertEquals("BRCA1", simpleEntry.getName());
        Assert.assertEquals(EntryType.FAMILY, simpleEntry.getType());
        Assert.assertEquals(entryHierarchy.getEntryHierarchyData(simpleEntry.getAc()), simpleEntry.getHierarchyData());
        Assert.assertEquals(entryHierarchy.getHierarchyLevel(simpleEntry.getAc()), simpleEntry.getHierarchyLevel());
        Assert.assertEquals(1, simpleEntry.getSignaturesMap().size());

        //Test simple entry locations
        Assert.assertNotNull(simpleEntry.getLocations());
        Assert.assertEquals(2, simpleEntry.getLocations().size());
        for (SimpleLocation simpleLocation : simpleEntry.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                Assert.assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location1.getHmmLength(), simpleLocation.getLength());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                Assert.assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location2.getHmmLength(), simpleLocation.getLength());
            }
        }

        //Test simple signature locations
        SimpleSignature simpleSignature = simpleEntry.getSignaturesMap().get("PTHR13763-PANTHER");
        Assert.assertNotNull(simpleSignature);
        Assert.assertNotNull(simpleSignature.getLocations());
        Assert.assertEquals(2, simpleSignature.getLocations().size());
        for (SimpleLocation simpleLocation : simpleSignature.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                Assert.assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location1.getHmmLength(), simpleLocation.getLength());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                Assert.assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
                Assert.assertEquals(hmmer2Location2.getHmmLength(), simpleLocation.getLength());
            }
        }
    }

    private Entry buildTestEntry() {
        return new Entry.Builder("IPR011364")
                .name("BRCA1")
                .type(uk.ac.ebi.interpro.scan.model.EntryType.FAMILY)
                .description("BRCA1")
                .abstractText("This group represents a DNA-damage repair protein, BRCA1")
                .release(new Release("32.0"))
                .goCrossReference(new GoXref("GO:0006281", "DNA repair", GoCategory.BIOLOGICAL_PROCESS))
                .goCrossReference(new GoXref("GO:0003677", "DNA binding", GoCategory.MOLECULAR_FUNCTION))
                .goCrossReference(new GoXref("GO:0008270", "zinc ion binding", GoCategory.MOLECULAR_FUNCTION))
                .goCrossReference(new GoXref("GO:0005634", "nucleus", GoCategory.CELLULAR_COMPONENT))
                .pathwayCrossReference(new PathwayXref("identifier", "name", "databaseName"))
                .build();
    }
}
