package uk.ac.ebi.interpro.scan.web.model;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Phil Jones
 *         Date: 31/01/12
 *         Time: 17:33
 *         <p/>
 *         Test the SimpleSuperMatch class
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CondensedViewComponentTest {

    private static final String DOMAIN = "domain";

    private static final String REPEAT = "repeat";

    @Resource
    private EntryHierarchy entryHierarchy;

    /*
    Hierarchies in the provided test file:

    4 level hierarchy:
    IPR000276
        ^
    IPR001671
        ^
    IPR001908
        ^
    IPR002122
    ---------------------------------
    3 level hierarchy:
    IPR000020
        ^
    IPR018081
        ^
    IPR001840
     */

    private SimpleSuperMatch IPR000276_24_67;
    private SimpleSuperMatch IPR001671_64_99;
    private SimpleSuperMatch IPR001671_60_95;
    private SimpleSuperMatch IPR001671_59_94;
    private SimpleSuperMatch IPR001671_61_96;
    private SimpleSuperMatch IPR001908_212_276;
    private SimpleSuperMatch IPR002122_264_312;
    private SimpleSuperMatch IPR000020_12_24;
    private SimpleSuperMatch IPR018081_20_24;
    private SimpleSuperMatch IPR001840_36_48;

    @Before
    public void setup() {
        IPR000276_24_67 = new SimpleSuperMatch(
                new SimpleEntry("IPR000276", "tstNm1", "testName1", EntryType.DOMAIN, entryHierarchy), new SimpleLocation(24, 67)
        );

        IPR001671_64_99 = new SimpleSuperMatch(
                new SimpleEntry("IPR001671", "tstNm2", "testName2", EntryType.DOMAIN, entryHierarchy), new SimpleLocation(64, 99)
        );

        IPR001671_60_95 = new SimpleSuperMatch(
                new SimpleEntry("IPR001671", "tstNm2", "testName2", EntryType.DOMAIN, entryHierarchy), new SimpleLocation(60, 95)
        );

        IPR001671_59_94 = new SimpleSuperMatch(
                new SimpleEntry("IPR001671", "tstNm2", "testName2", EntryType.DOMAIN, entryHierarchy), new SimpleLocation(59, 94)
        );

        IPR001671_61_96 = new SimpleSuperMatch(
                new SimpleEntry("IPR001671", "tstNm2", "testName2", EntryType.DOMAIN, entryHierarchy), new SimpleLocation(61, 96)
        );


        IPR001908_212_276 = new SimpleSuperMatch(
                new SimpleEntry("IPR001908", "tstNm3", "testName3", EntryType.DOMAIN, entryHierarchy), new SimpleLocation(212, 276)
        );

        IPR002122_264_312 = new SimpleSuperMatch(
                new SimpleEntry("IPR002122", "tstNm4", "testName4", EntryType.DOMAIN, entryHierarchy), new SimpleLocation(264, 312)
        );

        IPR000020_12_24 = new SimpleSuperMatch(
                new SimpleEntry("IPR000020", "tstNm5", "testName5", EntryType.REPEAT, entryHierarchy), new SimpleLocation(12, 24)
        );

        IPR018081_20_24 = new SimpleSuperMatch(
                new SimpleEntry("IPR018081", "tstNm6", "testName6", EntryType.REPEAT, entryHierarchy), new SimpleLocation(20, 24)
        );

        IPR001840_36_48 = new SimpleSuperMatch(
                new SimpleEntry("IPR001840", "tstNm7", "testName7", EntryType.REPEAT, entryHierarchy), new SimpleLocation(36, 48)
        );

    }


    @Test
    public void matchesOverlapTest() {
        Assert.assertTrue(IPR000276_24_67.matchesOverlap(IPR001671_64_99, true)); // Only overlaps by 3aa
        Assert.assertFalse(IPR000276_24_67.matchesOverlap(IPR001671_64_99, false)); // Only overlaps by 3aa
        Assert.assertFalse(IPR000276_24_67.matchesOverlap(IPR001671_60_95, false)); // Overlaps by exactly 20% shortest match
        Assert.assertTrue(IPR000276_24_67.matchesOverlap(IPR001671_59_94, false)); // Overlaps >20% shortest match
        Assert.assertFalse(IPR000276_24_67.matchesOverlap(IPR001671_61_96, false)); // Overlaps <20% shortest match
        Assert.assertTrue(IPR000276_24_67.matchesOverlap(IPR001840_36_48, true));
        Assert.assertFalse(IPR000276_24_67.matchesOverlap(IPR001908_212_276, true));
        Assert.assertFalse(IPR000276_24_67.matchesOverlap(IPR002122_264_312, true));
        Assert.assertTrue(IPR000276_24_67.matchesOverlap(IPR000020_12_24, true));
    }

    @Test
    public void inSameHierarchyTest() {
        Assert.assertTrue(IPR000276_24_67.inSameHierarchy(IPR001908_212_276));
        Assert.assertFalse(IPR000276_24_67.inSameHierarchy(IPR018081_20_24));
    }

    @Test
    @Ignore("Fiddling about with overlap requirement")
    public void testSuperMatchBucket() {
        SuperMatchBucket bucket = new SuperMatchBucket(IPR000276_24_67);

        Assert.assertTrue(EntryType.DOMAIN == bucket.getType());
        Assert.assertNotNull(bucket.getSupermatches());
        Assert.assertEquals(1, bucket.getSupermatches().size());

        // Now attempt to merge in another SuperMatch of the same hierarchy that overlaps, so should merge
        Assert.assertTrue(bucket.addIfSameHierarchyMergeIfOverlap(IPR001671_64_99));
        Assert.assertEquals("There should now still be one supermatch, as the one that was added should have merged.", 1, bucket.getSupermatches().size());
        // Retrieve the one merged SimpleSuperMatch
        SimpleSuperMatch mergedMatch = bucket.getSupermatches().get(0);
        Assert.assertNotNull(mergedMatch);
        Assert.assertNotNull(mergedMatch.getLocation());
        Assert.assertEquals(24, mergedMatch.getLocation().getStart());
        Assert.assertEquals(99, mergedMatch.getLocation().getEnd());

        // Now attempt at add another SimpleSuperMatch that is in the same hierarchy but does not overlap.
        Assert.assertTrue(bucket.addIfSameHierarchyMergeIfOverlap(IPR001908_212_276));
        Assert.assertEquals("There should now be two supermatches in the bucket - the second one was in the same hierarchy, but in a different location.", 2, bucket.getSupermatches().size());

        // The first merged match should be unchanged.
        mergedMatch = bucket.getSupermatches().get(0);
        Assert.assertNotNull(mergedMatch);
        Assert.assertNotNull(mergedMatch.getLocation());
        Assert.assertEquals(24, mergedMatch.getLocation().getStart());
        Assert.assertEquals(99, mergedMatch.getLocation().getEnd());


        mergedMatch = bucket.getSupermatches().get(1);
        Assert.assertNotNull(mergedMatch);
        Assert.assertNotNull(mergedMatch.getLocation());
        Assert.assertEquals(212, mergedMatch.getLocation().getStart());
        Assert.assertEquals(276, mergedMatch.getLocation().getEnd());

        // Now attempt to add a SimpleSuperMatch from a different hierarchy
        Assert.assertFalse(bucket.addIfSameHierarchyMergeIfOverlap(IPR018081_20_24));

        Assert.assertEquals("There bucket should be unmodified following a failed attempt to add a SimpleSuperMatch.", 2, bucket.getSupermatches().size());

        // The first merged match should be unchanged.
        mergedMatch = bucket.getSupermatches().get(0);
        Assert.assertNotNull(mergedMatch);
        Assert.assertNotNull(mergedMatch.getLocation());
        Assert.assertEquals(24, mergedMatch.getLocation().getStart());
        Assert.assertEquals(99, mergedMatch.getLocation().getEnd());


        mergedMatch = bucket.getSupermatches().get(1);
        Assert.assertNotNull(mergedMatch);
        Assert.assertNotNull(mergedMatch.getLocation());
        Assert.assertEquals(212, mergedMatch.getLocation().getStart());
        Assert.assertEquals(276, mergedMatch.getLocation().getEnd());

    }

    @Test
    public void testCondensedLine() {
        final SuperMatchBucket bucket1 = new SuperMatchBucket(IPR000276_24_67);
        bucket1.addIfSameHierarchyMergeIfOverlap(IPR001671_64_99);
        bucket1.addIfSameHierarchyMergeIfOverlap(IPR001908_212_276);
        bucket1.addIfSameHierarchyMergeIfOverlap(IPR002122_264_312);

        final SuperMatchBucket bucket2 = new SuperMatchBucket(IPR000020_12_24);
        bucket2.addIfSameHierarchyMergeIfOverlap(IPR018081_20_24);
        bucket2.addIfSameHierarchyMergeIfOverlap(IPR001840_36_48);

        CondensedLine line1 = new CondensedLine(bucket1);
        Assert.assertFalse(line1.addSuperMatchesSameTypeWithoutOverlap(bucket2));

        CondensedLine line2 = new CondensedLine(bucket2);
        Assert.assertFalse(line2.addSuperMatchesSameTypeWithoutOverlap(bucket1));

        Assert.assertTrue(line1.compareTo(line2) < 0);
    }
}
