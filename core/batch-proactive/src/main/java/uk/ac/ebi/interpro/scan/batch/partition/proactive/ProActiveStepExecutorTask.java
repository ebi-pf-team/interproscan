package uk.ac.ebi.interpro.scan.batch.partition.proactive;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.annotation.RemoteObject;
import org.springframework.batch.core.StepExecution;
import uk.ac.ebi.interpro.scan.batch.partition.remote.RemoteStepExecutor;

/**
 * Executes step on remote node as a ProActive task.
 *
 * @author  Antony Quinn
 * @version $Id: ProActiveStepExecutorTask.java,v 1.1 2009/06/18 15:08:38 aquinn Exp $
 * @since   1.0
 */
@RemoteObject
public class ProActiveStepExecutorTask implements Task<StepExecution> {

    private final RemoteStepExecutor remoteStepExecutor;

    public ProActiveStepExecutorTask(RemoteStepExecutor remoteStepExecutor)    {
        this.remoteStepExecutor = remoteStepExecutor;
    }

    public StepExecution run(WorkerMemory workerMemory) throws Exception {
        return remoteStepExecutor.execute();
    }
   
}
