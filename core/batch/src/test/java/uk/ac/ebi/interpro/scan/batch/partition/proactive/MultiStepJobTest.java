package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * Tests multi-step HMMER job.
 *
 * @author  Antony Quinn
 * @version $Id: MultiStepJobTest.java,v 1.1 2009/06/18 17:04:33 aquinn Exp $
 * @see     <a href="http://www.ebi.ac.uk/seqdb/jira/browse/IBU-983">IBU-983</a>
 * @since   1.0 
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MultiStepJobTest extends AbstractPartitionJobTest {

    @Test public void testJob() throws Exception {
        super.runJob();
    }

}