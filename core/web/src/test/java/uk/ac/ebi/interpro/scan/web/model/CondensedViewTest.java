package uk.ac.ebi.interpro.scan.web.model;

import org.junit.Assert;
import org.apache.log4j.Logger;
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
 *         Date: 01/02/12
 *         Time: 15:31
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CondensedViewTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    private static final Logger LOG = Logger.getLogger(CondensedViewTest.class.getName());

    @Test
    @Ignore
    public void testCondensedView() {
        final SimpleProtein protein = new SimpleProtein("P99999", "A_PROTEIN", "This is a protein", 400, "ABCDEF123456789", "23948239", 9606, "Homo sapiens", "Homo sapiens (Human)",false);

    }

    @Test
    public void testConstructFromSuperMatches() {
        // Condensed view input data loosely based on UPI000012C6D3 / P17945
        int proteinLength = 728;
        List<SimpleSuperMatch> ssms = new ArrayList<SimpleSuperMatch>();
        SimpleSuperMatch ssm1 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(124, 209));
        SimpleSuperMatch ssm2 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(210, 293));
        SimpleSuperMatch ssm3 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(299, 386));
        SimpleSuperMatch ssm4 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(389, 477));
        SimpleSuperMatch ssm5 = new SimpleSuperMatch(
                new SimpleEntry("IPR001254", "Peptidase_S1", "Peptidase S1", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(495, 724));
        SimpleSuperMatch ssm6 = new SimpleSuperMatch(
                new SimpleEntry("IPR003014", "PAN-1_domain", "PAN-1 domain", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(43, 122));
        SimpleSuperMatch ssm7 = new SimpleSuperMatch(
                new SimpleEntry("IPR003609", "Pan_app", "Apple-like", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(38, 124));
        SimpleSuperMatch ssm8 = new SimpleSuperMatch(
                new SimpleEntry("IPR009003", "Trypsin-like_Pept_dom", "Trypsin-like cysteine/serine peptidase domain", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(486, 723));
        SimpleSuperMatch ssm9 = new SimpleSuperMatch(
                new SimpleEntry("IPR013806", "Kringle-like", "Kringle-like fold", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(116, 294));
        SimpleSuperMatch ssm10 = new SimpleSuperMatch(
                new SimpleEntry("IPR013806", "Kringle-like", "Kringle-like fold", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(303, 478));

        ssms.add(ssm1);
        ssms.add(ssm2);
        ssms.add(ssm3);
        ssms.add(ssm4);
        ssms.add(ssm5);
        ssms.add(ssm6);
        ssms.add(ssm7);
        ssms.add(ssm8);
        ssms.add(ssm9);
        ssms.add(ssm10);

        CondensedView condensedView = new CondensedView(proteinLength, ssms);
        Assert.assertNotNull(condensedView);

        if (LOG.isDebugEnabled()) {
            LOG.debug(ssms);
            for (CondensedLine line : condensedView.getLines()) {
                for (SimpleSuperMatch m : line.getSuperMatchList()) {
                    LOG.debug(m.getEntries() + " " + m.getLocation());
                }
            }
        }

        // Check 10 is condensed down to 5 once hierarchy and position are taken into account
        Assert.assertEquals(5, condensedView.getNumSuperMatchBlobs());
    }
}
