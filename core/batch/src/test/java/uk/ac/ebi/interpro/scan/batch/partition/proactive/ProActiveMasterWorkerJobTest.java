package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests {@link ProActiveMasterWorkerPartitionHandler} by running sample job.
 *
 * @author  Antony Quinn
 * @version $Id: ProActiveMasterWorkerJobTest.java,v 1.1 2009/06/18 17:04:33 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ProActiveMasterWorkerJobTest extends AbstractPartitionJobTest {

    @Test public void testJob() throws Exception {
        super.runJob();
    }

}
