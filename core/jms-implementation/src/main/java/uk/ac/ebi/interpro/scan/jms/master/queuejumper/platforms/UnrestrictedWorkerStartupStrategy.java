package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

/**
 * Created by IntelliJ IDEA.
 * @author Phil Jones
 * Date: 18/10/11
 *
 * Unrestricted worker startup.
 */
public class UnrestrictedWorkerStartupStrategy implements WorkerStartupStrategy {
    /**
     * Returns true if the worker should be run, false otherwise.
     *
     * This implementation always returns true, so workers are started freely as requested.
     *
     * @return true if the worker should be run, false otherwise.
     */
    public boolean startUpWorker(int priority) {
        return true;
    }
}
