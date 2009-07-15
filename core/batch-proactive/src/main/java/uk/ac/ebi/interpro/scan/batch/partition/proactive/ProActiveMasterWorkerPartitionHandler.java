package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.StepExecutionSplitter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.core.io.Resource;

import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.core.ProActiveException;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import uk.ac.ebi.interpro.scan.batch.partition.remote.RemoteStepExecutor;

/**
 * Executes steps on remote nodes using ProActive Parallel Suite's Master Worker API.
 *
 * @author  Antony Quinn
 * @version $Id: ProActiveMasterWorkerPartitionHandler.java,v 1.1 2009/06/18 15:08:38 aquinn Exp $
 * @see     <a href="http://proactive.inria.fr/">ProActive Parallel Suite</a>
 * @since   1.0
 */
public class ProActiveMasterWorkerPartitionHandler implements PartitionHandler, InitializingBean {

    private Step step;
    private Resource applicationDescriptor;
    private String remoteLaunchContext;
    private JobRepository jobRepository;
    private boolean persistRemoteStepExecutions = true;    

    // Not sure if we need this...
    private int gridSize = 1;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(applicationDescriptor, "An ApplicationDescriptor must be provided");
        Assert.notNull(remoteLaunchContext,   "A RemoteLaunchContext must be provided");
        Assert.notNull(step,                  "A Step must be provided");
        if (persistRemoteStepExecutions)    {
            Assert.notNull(jobRepository, "A JobRepository must be provided");
        }
    }

    public void setApplicationDescriptor(Resource applicationDescriptor) {
        this.applicationDescriptor = applicationDescriptor;
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void setPersistRemoteStepExecutions(boolean persistRemoteStepExecutions) {
        this.persistRemoteStepExecutions = persistRemoteStepExecutions;
    }

    public void setRemoteLaunchContext(String remoteLaunchContext) {
        this.remoteLaunchContext = remoteLaunchContext;
    }

    public void setStep(Step step) {
        this.step = step;
    }   

    /**
     * @see PartitionHandler#handle(StepExecutionSplitter, StepExecution)
     */    
    public Collection<StepExecution> handle(StepExecutionSplitter stepExecutionSplitter, 
                                            StepExecution masterStepExecution) throws Exception {

        // Tasks
        List<Task<StepExecution>> tasks = new ArrayList<Task<StepExecution>>();

        // Results
        Collection <StepExecution> results = null;        

        // Master
        ProActiveMaster<Task<StepExecution>, StepExecution> master =
                new ProActiveMaster<Task<StepExecution>, StepExecution>();

        try {  

            // Add virtual nodes
            master.addResources(applicationDescriptor.getURL());

            // Create tasks
            for (final StepExecution stepExecution : stepExecutionSplitter.split(masterStepExecution, gridSize)) {
                RemoteStepExecutor remoteStepExecutor =
                        new RemoteStepExecutor(remoteLaunchContext, step.getName(), stepExecution);
                Task<StepExecution> task = new ProActiveStepExecutorTask(remoteStepExecutor);
                tasks.add(task);
            }

            // Run tasks
            master.solve(tasks);

            // Collect results
            results = master.waitAllResults();
            
        }
        catch (ProActiveException e)    {
            // Couldn't add resources
            throw (e);
        }
        catch (TaskException e) {
            throw (e);
        }
        finally {
            // Shutdown ProActive nodes
            master.terminate(true);
		}

        // Persist step executions received from remote node
        if (persistRemoteStepExecutions)  {
            for (StepExecution stepExecution : results) {
                jobRepository.updateExecutionContext(stepExecution);
                jobRepository.update(stepExecution);
            }
        }        

        return results;

    }

}
