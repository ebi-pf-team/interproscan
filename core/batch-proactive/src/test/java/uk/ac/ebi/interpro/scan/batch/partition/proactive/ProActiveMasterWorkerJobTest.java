package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * Tests {@link ProActiveMasterWorkerPartitionHandler}.
 *
 * @author  Antony Quinn
 * @version $Id: ProActiveMasterWorkerJobTest.java,v 1.1 2009/06/18 15:08:37 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ProActiveMasterWorkerJobTest extends AbstractPartitionJobTest {

    @Test (timeout = 60000) public void testJob() throws Exception {
        super.runJob();
    }

}

