package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * Tests {@link com.activeeon.sandbox.spring.ProActiveSchedulerPartitionHandler} by running a test job.
 * 
 * Note: This is an integration test, not a unit test, because we require a running instance of the
 * ProActive Scheduler.
 *
 * @author  Antony Quinn
 * @version $Id: ProActiveSchedulerJobIT.java,v 1.1 2009/06/18 15:08:37 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ProActiveSchedulerJobIT extends AbstractPartitionJobTest {

    @Test public void testJob() throws Exception {
        super.runJob();
    }

}