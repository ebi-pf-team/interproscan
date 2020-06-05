package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.List;

/**
 * This WorkerRunner runs workers on named hosts on the local area network.
 * <p/>
 * It is configured with a hostname, and optionally (in the servers-context.xml file) the number of
 * JVMs to run on the host, allowing powerful / multiprocessor machines to run more than one Worker.
 *
 * @author Phil Jones, John Maslen
 * @version Id: LANWorkerRunner.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class LANWorkerRunner implements WorkerRunner {

    private static final Logger LOGGER = LogManager.getLogger(LANWorkerRunner.class.getName());

    String command;

    String accessPrefix;

    LANServerSettings lanServerSettings;

    LANServerListing lanServerListing;

    /**
     * The command to run on each host, to run the JVM / worker. (probably a call to the java executable)
     *
     * @param command The command to run on each host, to run the JVM / worker. (probably a call to the java executable)
     */
    @Required
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * TODO - Really need to think about this one!
     * How to run the command on the remote host, e.g.
     * "ssh -x username@"
     * <p/>
     * The hostname will be appended on to this.
     *
     * @param accessPrefix What to stick in front of the hostname.
     */
    @Required
    public void setAccessPrefix(String accessPrefix) {
        this.accessPrefix = accessPrefix;
    }

    public void setLanServerListing(uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.LANServerListing lanServerListing) {
        this.lanServerListing = lanServerListing;
    }

    public void setLanServerSettings(uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.LANServerSettings lanServerSettings) {
        this.lanServerSettings = lanServerSettings;
    }

    /**
     * According to the number of JVMs required, a new worker JVM is started.
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     */
    @Override
    public int startupNewWorker() {
        List<uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.LANServerSettings> lanServerSettingsList = lanServerListing.getListServerSettings();
        for (uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.LANServerSettings serverSettings : lanServerSettingsList) {
            setLanServerSettings(serverSettings);
            for (int i = 0; i < lanServerSettings.getVmNumber(); i++) {
                StringBuffer commandBuf = new StringBuffer();

                commandBuf.append(accessPrefix);

                if (lanServerSettings.getHostname() != null) {
                    commandBuf.append('@').append(lanServerSettings.getHostname()).append(' ');
                }

                commandBuf.append(command);

                if (lanServerSettings.getMemory() != null) {
                    commandBuf.append(' ').append(lanServerSettings.getMemory());
                }

                LOGGER.debug("LAN command: " + commandBuf.toString());

                try {
                    Runtime.getRuntime().exec(commandBuf.toString());
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot run the worker", e);
                }
            }
        }
        return 1;
    }

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    @Override
    public int startupNewWorker(int priority, String tcpUri, String temporaryDirectory) {
        startupNewWorker(priority);
        return 1;
    }

    /**
     * Runs a new worker JVM, by whatever mechanism (e.g. LSF, PBS, SunGridEngine)
     * Assumes that the jar being executed has a main class define in the MANIFEST.
     * Sets the worker to only accept jobs above the priority passed in as argument.
     *
     * @param priority being the minimum message priority that this worker will accept.
     */
    @Override
    public int startupNewWorker(int priority) {
        startupNewWorker();
        return 1;
    }

    /**
     * See {@link #startupNewWorker(int, String, String)} }
     * The masterWorker boolean flag indicates if a worker was created by the master itself. TRUE if created by the master, otherwise FALSE.
     *
     * TODO: test it in this LAN mode
     * @param priority
     * @param tcpUri
     * @param temporaryDirectory
     * @param masterWorker
     */
    @Override
    public int startupNewWorker(int priority, String tcpUri, String temporaryDirectory, boolean masterWorker) {
        startupNewWorker();
        return 1;
    }

    /**
     * See {@link #startupNewWorker(int, String, String)} }
     * The newWorkersCount is the number of workers to be created using a job Array
     *
     * * TODO: test it in this LAN mode?
     * @param priority
     * @param tcpUri
     * @param temporaryDirectory
     * @param newWorkersCount
     */
    @Override
    public int startupNewWorker(int priority, String tcpUri, String temporaryDirectory, int newWorkersCount) {
        startupNewWorker();
        return 1;
    }
}
