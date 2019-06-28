package uk.ac.ebi.interpro.scan.web.model;


import org.apache.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Phil Jones
 *         Date: 01/02/12
 *         Time: 15:31
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class CondensedViewTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    private static final Logger LOG = Logger.getLogger(CondensedViewTest.class.getName());

    @Test
    @Disabled
    public void testCondensedView() {
        final SimpleProtein protein = new SimpleProtein("P99999", "A_PROTEIN", "This is a protein", 400, "ABCDEF123456789", "23948239", 9606, "Homo sapiens", "Homo sapiens (Human)",false);

    }

    @Test
    public void testConstructFromSuperMatches() {
        // Condensed view input data loosely based on UPI000012C6D3 / P17945
        int proteinLength = 728;
        List<SimpleSuperMatch> ssms = new ArrayList<SimpleSuperMatch>();
        // #2
        SimpleSuperMatch ssm1 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(124, 209));
        // #2
        SimpleSuperMatch ssm2 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(210, 293));
        // #3
        SimpleSuperMatch ssm3 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(299, 386));
        // #3
        SimpleSuperMatch ssm4 = new SimpleSuperMatch(
                new SimpleEntry("IPR000001", "Kringle", "Kringle", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(389, 477));
        // #4
        SimpleSuperMatch ssm5 = new SimpleSuperMatch(
                new SimpleEntry("IPR001254", "Peptidase_S1", "Peptidase S1", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(495, 724));
        // #1
        SimpleSuperMatch ssm6 = new SimpleSuperMatch(
                new SimpleEntry("IPR003014", "PAN-1_domain", "PAN-1 domain", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(43, 122));
        // #1
        SimpleSuperMatch ssm7 = new SimpleSuperMatch(
                new SimpleEntry("IPR003609", "Pan_app", "Apple-like", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(38, 124));
        // #4
        SimpleSuperMatch ssm8 = new SimpleSuperMatch(
                new SimpleEntry("IPR009003", "Trypsin-like_Pept_dom", "Trypsin-like cysteine/serine peptidase domain", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(486, 723));
        // #2
        SimpleSuperMatch ssm9 = new SimpleSuperMatch(
                new SimpleEntry("IPR013806", "Kringle-like", "Kringle-like fold", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(116, 294));
        // #3
        SimpleSuperMatch ssm10 = new SimpleSuperMatch(
                new SimpleEntry("IPR013806", "Kringle-like", "Kringle-like fold", EntryType.DOMAIN, entryHierarchy),
                new SimpleLocation(303, 478));

        // #1 = SimpleSuperMatch{type='Domain', location=SimpleLocation{start=38, end=124}, entries=[IPR003609, IPR003014]}
        // #2 = SimpleSuperMatch{type='Domain', location=SimpleLocation{start=116, end=294}, entries=[IPR013806, IPR000001]}
        // #3 = SimpleSuperMatch{type='Domain', location=SimpleLocation{start=299, end=478}, entries=[IPR013806, IPR000001]}
        // #4 = SimpleSuperMatch{type='Domain', location=SimpleLocation{start=486, end=724}, entries=[IPR009003, IPR001254]}

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
        assertNotNull(condensedView);

        if (LOG.isDebugEnabled()) {
            LOG.debug(ssms);
            for (CondensedLine line : condensedView.getLines()) {
                for (SimpleSuperMatch m : line.getSuperMatchList()) {
                    LOG.debug(m.getEntries() + " " + m.getLocation());
                }
            }
        }

        // Check 10 is condensed down to 4 once hierarchy and position are taken into account
        assertEquals(4, condensedView.getNumSuperMatchBlobs());
    }
}
