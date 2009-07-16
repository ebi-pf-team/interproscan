package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.batch.test.AbstractJobTests;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParametersBuilder;
import static org.junit.Assert.assertEquals;
import org.apache.log4j.Logger;

import java.util.Date;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Tests {@link org.springframework.batch.core.partition.PartitionHandler} implementations.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
abstract class AbstractPartitionJobTest extends AbstractJobTests {
    
    private static Logger LOGGER = Logger.getLogger(AbstractPartitionJobTest.class);

    static    {
        final String SEC_POLICY_KEY = "java.security.policy";
        if (System.getProperty(SEC_POLICY_KEY) == null) {
            System.setProperty(SEC_POLICY_KEY,
                    "src/test/resources/uk/ac/ebi/interpro/scan/batch/partition/proactive/security.policy");
        }
    }   

    protected void runJob() throws Exception {
		assertEquals(BatchStatus.COMPLETED, this.launchJob().getStatus());
	}    

	@Override protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder()
                        .addDate("date", new Date())
                        .addString("userName", System.getProperty("user.name"))
                        .addString("hostName", getHostName())
                        .toJobParameters();
	}

    private static String getHostName() {
        String hostName = "unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)  {
            LOGGER.warn("Could not get host name: " + e.getMessage());
        }
        return hostName;
    }

    // To run a step on its own:
    //	@Test public void testStep() {
    //		assertEquals(BatchStatus.COMPLETED, this.launchStep("echoStep").getStatus());
    //	}    

}
