package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests {@link com.activeeon.sandbox.spring.ProActiveSchedulerPartitionHandler}
 * by running sample job.
 * 
 * Note: This is an integration test, not a unit test, because we require a running instance of the
 * ProActive Scheduler. Run 'mvn verify'. 
 *
 * @author  Antony Quinn
 * @version $Id: ProActiveSchedulerJobIT.java,v 1.1 2009/06/18 17:04:33 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ProActiveSchedulerJobIT extends AbstractPartitionJobTest {

    @Test public void testJob() throws Exception {
        super.runJob();
    }

}