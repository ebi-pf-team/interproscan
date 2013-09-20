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
    public int startupNewWorker();

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    public int startupNewWorker(int priority, String tcpUri, String temporaryDirectory);

    /**
     * See {@link #startupNewWorker(int, String, String)} }
     * The masterWorker boolean flag indicates if a worker was created by the master itself. TRUE if created by the master, otherwise FALSE.
     * @param priority
     * @param tcpUri
     * @param temporaryDirectory
     * @param masterWorker
     */
    public int startupNewWorker(int priority, String tcpUri, String temporaryDirectory, boolean masterWorker);

    /**
     * See {@link #startupNewWorker(int, String, String)} }
     * The newWorkersCount is the number of workers to be created using a job Array
     * @param priority
     * @param tcpUri
     * @param temporaryDirectory
     * @param newWorkersCount
     */
    public int startupNewWorker(int priority, String tcpUri, String temporaryDirectory, int newWorkersCount);


    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    public int startupNewWorker(int priority);
}
