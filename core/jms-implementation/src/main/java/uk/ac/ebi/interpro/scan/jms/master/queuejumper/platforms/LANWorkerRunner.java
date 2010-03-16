package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.List;

/**
 * This WorkerRunner runs workers on named hosts on the local area network.
 * <p/>
 * It is configured with a hostname, and optionally (in the spring-config-servers.xml file) the number of
 * JVMs to run on the host, allowing powerful / multiprocessor machines to run more than one Worker.
 *
 * @author Phil Jones, John Maslen
 * @version Id: LANWorkerRunner.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class LANWorkerRunner implements WorkerRunner {

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
    public void startupNewWorker() {
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

                System.out.println("LAN command: " + commandBuf.toString());

                try {
                    Runtime.getRuntime().exec(commandBuf.toString());
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot run the worker", e);
                }
            }
        }
    }
}
