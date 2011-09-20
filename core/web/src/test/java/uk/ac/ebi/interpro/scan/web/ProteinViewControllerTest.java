package uk.ac.ebi.interpro.scan.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for {@link ProteinViewController}
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinViewControllerTest {

    @Test
    public void testProtein()    {
        ProteinViewController c = new ProteinViewController();
        c.proteinFeatures("PLCH2");
    }

}
