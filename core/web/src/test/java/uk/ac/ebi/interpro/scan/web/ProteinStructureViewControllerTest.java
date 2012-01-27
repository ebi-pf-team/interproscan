package uk.ac.ebi.interpro.scan.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.io.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link ProteinStructureViewController}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinStructureViewControllerTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    @Resource
    private CreateSimpleProteinFromMatchData matchData;

    @Test
    public void testProtein()    {
        ProteinStructureViewController c = new ProteinStructureViewController();
        c.setEntryHierarchy(entryHierarchy);
        c.setMatchData(matchData);
        c.proteinFeatures("P38398");
    }

    @Test
    public void testSimpleStructuralMatchSort() {
        List<SimpleStructuralMatch> structuralMatches = new ArrayList<SimpleStructuralMatch>();

        SimpleStructuralMatch structuralMatch1 = new SimpleStructuralMatch("MODBASE", "MB_P38398", "MB_P38398");
        SimpleLocation location1 = new SimpleLocation(20, 115);
        SimpleLocation location2 = new SimpleLocation(884, 1786);
        SimpleLocation location3 = new SimpleLocation(401, 1458);
        structuralMatch1.addLocation(location1);
        structuralMatch1.addLocation(location2);
        structuralMatch1.addLocation(location3);
        structuralMatches.add(structuralMatch1);

        SimpleStructuralMatch structuralMatch2 = new SimpleStructuralMatch("PDB", "1jm7A", "1jm7");
        SimpleLocation location4 = new SimpleLocation(1, 110);
        structuralMatch2.addLocation(location4);
        structuralMatches.add(structuralMatch2);

        SimpleStructuralMatch structuralMatch3 = new SimpleStructuralMatch("CATH", "1jm7A00", "3.30.40.10");
        SimpleLocation location5 = new SimpleLocation(1755, 1863);
        SimpleLocation location6 = new SimpleLocation(1, 103);
        structuralMatch3.addLocation(location5);
        structuralMatch3.addLocation(location6);
        structuralMatches.add(structuralMatch3);

        // Test the structural matches sort correctly
        Collections.sort(structuralMatches);
        assertEquals(3, structuralMatches.size());
        assertEquals("CATH", structuralMatches.get(0).getDatabaseName());
        assertEquals("PDB", structuralMatches.get(1).getDatabaseName());
        assertEquals("MODBASE", structuralMatches.get(2).getDatabaseName());
    }

}
