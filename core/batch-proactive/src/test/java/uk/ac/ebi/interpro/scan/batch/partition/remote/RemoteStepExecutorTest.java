package uk.ac.ebi.interpro.scan.batch.partition.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests {@link RemoteStepExecutor}.
 *
 * @author  Antony Quinn
 * @version $Id: RemoteStepExecutorTest.java,v 1.1 2009/06/18 15:08:38 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class RemoteStepExecutorTest {
    
    @Test public void testCreateNewInstance() throws Exception {
		RemoteStepExecutor rse = new RemoteStepExecutor(null, "foo", null);
		assertEquals("foo", rse.getStepName());
	}

	@Test public void testExecute() throws Exception {
		RemoteStepExecutor rse = new RemoteStepExecutor(
                "/uk/ac/ebi/interpro/scan/batch/partition/remote/RemoteStepExecutorTest-context.xml",
                "echoStep", 
                MetaDataInstanceFactory.createStepExecution("step", 1L));
		// Simulate remote execution by deserializing
		RemoteStepExecutor rseCopy = 
                (RemoteStepExecutor) SerializationUtils.deserialize(SerializationUtils.serialize(rse));
        assertNotNull(rseCopy.execute());
    }

}