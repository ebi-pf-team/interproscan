package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * Tests {@link org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler}
 * by running sample i5 job.
 *
 * @author  Antony Quinn
 * @version $Id: TaskExecutorJobTest.java,v 1.1 2009/06/18 17:04:33 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskExecutorJobTest extends AbstractPartitionJobTest {

    @Test public void testJob() throws Exception {
        super.runJob();
    }
    
}