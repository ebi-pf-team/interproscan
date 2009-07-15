package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.batch.test.AbstractJobTests;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link org.springframework.batch.core.partition.PartitionHandler} implementations.
 *
 * @author  Antony Quinn
 * @version $Id: AbstractPartitionJobTest.java,v 1.1 2009/06/18 17:04:33 aquinn Exp $
 * @since   1.0
 */
abstract class AbstractPartitionJobTest extends AbstractJobTests {

    static    {
        final String SEC_POLICY_KEY = "java.security.policy";
        if (System.getProperty(SEC_POLICY_KEY) == null) {
            //System.setProperty(SEC_POLICY_KEY, "proactive/security.policy");
            System.setProperty(SEC_POLICY_KEY,
                    "src/test/resources/uk/ac/ebi/interpro/scan/batch/partition/proactive/security.policy");
        }
    }   

    @Autowired
    private JobParameters jobParameters;

	@Override protected JobParameters getUniqueJobParameters() {
		return jobParameters;
	}

    protected void runJob() throws Exception {
		assertEquals(BatchStatus.COMPLETED, this.launchJob().getStatus());
	}    

    // To run a step on its own:
    //	@Test public void testTigrfamStep() {
    //		assertEquals(BatchStatus.COMPLETED, this.launchStep("tigrfamStep").getStatus());
    //	}    

}
