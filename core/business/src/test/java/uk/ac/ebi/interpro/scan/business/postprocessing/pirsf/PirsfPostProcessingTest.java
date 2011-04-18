package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests {@link PirsfPostProcessing}.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PirsfPostProcessingTest {

    private static final Logger LOGGER = Logger.getLogger(PirsfPostProcessingTest.class.getName());

    @Resource
    private org.springframework.core.io.Resource rawMatches;

    @Resource
    private org.springframework.core.io.Resource filteredMatches;

    @Resource
    private PirsfPostProcessing postProcessor;

    @Test
    @Ignore
    public void testFilter() throws IOException {
        fail("Test not implemented yet!");
    }


}
