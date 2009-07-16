package uk.ac.ebi.interpro.scan.batch.partition.remote;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.apache.commons.lang.SerializationUtils;

/**
 * Tests {@link RemoteJobRepository}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class RemoteJobRepositoryTest {  

    @Autowired
    private JobRepository remoteJobRepository;

    @Test public void testUpdate() throws Exception   {

        // Create local step execution
        StepExecution localStepExecution = MetaDataInstanceFactory.createStepExecution("step", 2L);

        // Simulate remote execution by serializing and deserializing
		StepExecution remoteStepExecution = (StepExecution) SerializationUtils.
                deserialize(SerializationUtils.serialize(localStepExecution));

        // Update repository (this will ultimately be called by step.execute() when running on the remote node)
        remoteJobRepository.update(remoteStepExecution);

    }

}
