package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * Tests {@link org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskExecutorJobTest extends AbstractPartitionJobTest {

    @Test public void testJob() throws Exception {
        super.runJob();
    }
    
}