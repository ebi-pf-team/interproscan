package uk.ac.ebi.interpro.scan.web.model;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.annotation.Resource;

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

    @Test
    @Ignore
    public void testCondensedView() {
        final SimpleProtein protein = new SimpleProtein("P99999", "A_PROTEIN", "This is a protein", 400, "ABCDEF123456789", "23948239", 9606, "Homo sapiens", "Homo sapiens (Human)");

    }
}
