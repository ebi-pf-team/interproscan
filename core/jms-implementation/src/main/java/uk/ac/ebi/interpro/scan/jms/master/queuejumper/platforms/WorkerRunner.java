package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

/**
 * Interface to run a new worker JVM (i.e. remote worker).
 * Should be sub-classed for different platforms, e.g. LSF, PBS, SunGridEngine etc.
 *
 * @author Phil Jones
 * @version $Id: WorkerRunner.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public interface WorkerRunner {

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     */
    public void startupNewWorker();

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    public void startupNewWorker(int priority, String tcpUri, String temporaryDirectory);

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    public void startupNewWorker(int priority);
}
