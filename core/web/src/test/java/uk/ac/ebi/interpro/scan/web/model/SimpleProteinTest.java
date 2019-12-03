package uk.ac.ebi.interpro.scan.web.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
@ExtendWith(SpringExtension.class)
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

    @BeforeEach
    public void initBeforeEachTest() {
        //Instantiate protein object
        this.testProtein = new Protein(PROTEIN_SEQ);
        //Instantiate  protein Xref object
        this.testProteinXref = this.testProtein.addCrossReference(new ProteinXref(proteinXrefIdentifier));

        //Instantiate match with locations
        this.hmmer2Location1 = new Hmmer2Match.Hmmer2Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.N_TERMINAL_COMPLETE);
        this.hmmer2Location2 = new Hmmer2Match.Hmmer2Location(120, 310, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.C_TERMINAL_COMPLETE);
        this.locations.add(hmmer2Location1);
        this.locations.add(hmmer2Location2);

        //Simple sanity checks
        assertEquals(PROTEIN_SEQ, testProtein.getSequence());
        assertEquals(proteinXrefIdentifier, testProteinXref.getIdentifier());
        assertEquals(3, hmmer2Location1.getStart());
        assertEquals(107, hmmer2Location1.getEnd());
        assertEquals(120, hmmer2Location2.getStart());
        assertEquals(310, hmmer2Location2.getEnd());
    }

    @Test
    public void testValueOfMethodForUnintegratedSignature() {
        //Add protein match
        testMatch = testProtein.addMatch(new Hmmer2Match(new Signature(signatureAccession, signatureName), signatureAccession, 0.035, 3.7e-9, locations));

        //Simple sanity checks
        assertNotNull(testMatch.getSignature());
        assertEquals(signatureName, testMatch.getSignature().getName());
        assertEquals(signatureAccession, testMatch.getSignature().getAccession());
        assertNotNull(testMatch.getLocations());
        assertEquals(2, testMatch.getLocations().size());
        assertTrue(testMatch.getLocations().contains(hmmer2Location1));
        assertTrue(testMatch.getLocations().contains(hmmer2Location2));

        //Finally, test the valueOf method
        SimpleProtein simpleProtein = SimpleProtein.valueOf(testProtein, testProteinXref, entryHierarchy);

        //Test simple attributes
        assertNotNull(simpleProtein);
        assertEquals(proteinXrefIdentifier, simpleProtein.getAc());
        assertEquals(simpleProtein.getId(), "Unknown");
        assertEquals(simpleProtein.getName(), "Unknown");
        assertEquals(testProtein.getSequenceLength(), simpleProtein.getLength());
        assertEquals(testProtein.getMd5(), simpleProtein.getMd5());
        assertEquals(simpleProtein.getCrc64(), "Unknown");
        assertEquals(simpleProtein.getTaxScienceName(), "Unknown");
        assertEquals(simpleProtein.getTaxFullName(), "Unknown");
        assertFalse(simpleProtein.isProteinFragment());

        //Start to test complex attributes
        //First, check all entries
        assertEquals(1, simpleProtein.getAllEntries().size());
        //Second, check for integrated entries (there shouldn't be any)
        assertEquals(0, simpleProtein.getEntries().size());
        //Third, check for un-integrated entries
        assertEquals(1, simpleProtein.getUnintegratedSignatures().size());

        //Compare signature and simple signature attributes
        SimpleSignature simpleSignature = simpleProtein.getUnintegratedSignatures().get(0);
        assertEquals(testMatch.getSignature().getAccession(), simpleSignature.getAc());
        assertEquals(testMatch.getSignature().getName(), simpleSignature.getName());
        assertEquals(MatchDataSource.UNKNOWN.name(), simpleSignature.getDataSource().getName());

        //Test simple signature locations
        assertNotNull(simpleSignature.getLocations());
        assertEquals(2, simpleSignature.getLocations().size());
        for (SimpleLocation simpleLocation : simpleSignature.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
            }
        }
        //Test simple entry
        assertNull(testMatch.getSignature().getEntry(), "Entry signature isn't null! But the following tests require, that the signature entry is NULL.");
        SimpleEntry simpleEntry = simpleProtein.getAllEntries().get(0);
        assertEquals("", simpleEntry.getAc());
        assertEquals("Unintegrated", simpleEntry.getShortName());
        assertEquals("Unintegrated", simpleEntry.getName());
        assertEquals(EntryType.UNKNOWN, simpleEntry.getType());
        assertEquals(entryHierarchy.getEntryHierarchyData(proteinXrefIdentifier), simpleEntry.getHierarchyData());
        assertEquals(entryHierarchy.getHierarchyLevel(proteinXrefIdentifier), simpleEntry.getHierarchyLevel());

        //Test simple entry locations
        assertNotNull(simpleEntry.getLocations());
        assertEquals(2, simpleEntry.getLocations().size());
        for (SimpleLocation simpleLocation : simpleEntry.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
            }
        }
        assertEquals(1, simpleEntry.getSignaturesMap().size());
        assertTrue(simpleEntry.getSignaturesMap().containsValue(simpleSignature));
    }

    @Test
    public void testValueOfMethodForIntegratedSignature() {
        Signature pirsfSignature = new Signature.Builder("PIRSF001734").name("BRCA1").build();
        Signature pantherSignature = new Signature.Builder("PTHR13763").name("BRCA1").signatureLibraryRelease(new SignatureLibraryRelease(SignatureLibrary.PANTHER, "7.0")).build();

        Entry entry = buildTestEntry();
        entry.addSignature(pantherSignature);
        entry.addSignature(pirsfSignature);

        //Add protein match
        testMatch = testProtein.addMatch(new Hmmer2Match(pantherSignature, "PTHR13763", 0.035, 3.7e-9, locations));

        //Finally, test the valueOf method
        SimpleProtein simpleProtein = SimpleProtein.valueOf(testProtein, testProteinXref, entryHierarchy);

        //Test simple attributes
        assertNotNull(simpleProtein);
        assertEquals(proteinXrefIdentifier, simpleProtein.getAc());
        assertEquals(simpleProtein.getId(), "Unknown");
        assertEquals(simpleProtein.getName(), "Unknown");
        assertEquals(testProtein.getSequenceLength(), simpleProtein.getLength());
        assertEquals(testProtein.getMd5(), simpleProtein.getMd5());
        assertEquals(simpleProtein.getCrc64(), "Unknown");
        assertEquals(simpleProtein.getTaxScienceName(), "Unknown");
        assertEquals(simpleProtein.getTaxFullName(), "Unknown");
        assertFalse(simpleProtein.isProteinFragment());

        //Start to test complex attributes
        //First, check all entries
        assertEquals(1, simpleProtein.getAllEntries().size());
        //Second, check for integrated entries (there shouldn't be any)
        assertEquals(1, simpleProtein.getEntries().size());
        //Third, check for un-integrated entries
        assertEquals(0, simpleProtein.getUnintegratedSignatures().size());

        //Test simple entry
        assertNotNull(testMatch.getSignature().getEntry(), "Entry signature is null! But the following tests require, that the signature entry is NOT NULL.");
        SimpleEntry simpleEntry = simpleProtein.getAllEntries().get(0);
        assertEquals(simpleEntry.getAc(), "IPR011364");
        assertEquals(simpleEntry.getShortName(), "BRCA1");
        assertEquals(simpleEntry.getName(), "BRCA1");
        assertEquals(EntryType.FAMILY, simpleEntry.getType());
        assertEquals(entryHierarchy.getEntryHierarchyData(simpleEntry.getAc()), simpleEntry.getHierarchyData());
        assertEquals(entryHierarchy.getHierarchyLevel(simpleEntry.getAc()), simpleEntry.getHierarchyLevel());
        assertEquals(1, simpleEntry.getSignaturesMap().size());

        //Test simple entry locations
        assertNotNull(simpleEntry.getLocations());
        assertEquals(2, simpleEntry.getLocations().size());
        for (SimpleLocation simpleLocation : simpleEntry.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
            }
        }

        //Test simple signature locations
        SimpleSignature simpleSignature = simpleEntry.getSignaturesMap().get("PTHR13763-PANTHER");
        assertNotNull(simpleSignature);
        assertNotNull(simpleSignature.getLocations());
        assertEquals(2, simpleSignature.getLocations().size());
        for (SimpleLocation simpleLocation : simpleSignature.getLocations()) {
            if (simpleLocation.getLength() == hmmer2Location1.getStart()) {
                assertEquals(hmmer2Location1.getEnd(), simpleLocation.getEnd());
            } else if (simpleLocation.getLength() == hmmer2Location2.getStart()) {
                assertEquals(hmmer2Location2.getEnd(), simpleLocation.getEnd());
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
