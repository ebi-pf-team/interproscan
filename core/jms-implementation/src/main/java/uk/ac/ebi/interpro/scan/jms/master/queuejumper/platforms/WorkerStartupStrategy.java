package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

/**
 * Created by IntelliJ IDEA.
 * @author Phil Jones
 * Date: 18/10/11
 *
 */
public interface WorkerStartupStrategy {

    /**
     * Returns true if the worker should be run, false otherwise.
     * @param priority the message priority that the worker is being configured to process.
     * (1 - 10, 10 is high)
     * @return true if the worker should be run, false otherwise.
     */
    public boolean startUpWorker(int priority);
}
