package uk.ac.ebi.interpro.scan.jms.broker.platforms;

import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * This WorkerRunner runs workers on named
 * hosts on the local area network.
 *
 * It is configured with a map of host names to the number of
 * JVMs to run on the host, allowing powerful / multiprocessor machines
 * to run more than one Worker.
 *
 * @author Phil Jones
 * @version $Id: LANWorkerRunner.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class LANWorkerRunner {

    Map<String, Integer> hostNameToJVMCount;

    String command;

    String accessPrefix;

    /**
     * a Map of hostnames (possibly including domain too) to the number of JVMs to run
     * on the host.
     * @param hostNameToJVMCount map of hostnames (possibly including domain too) to the number of JVMs to run
     * on the host.
     */
    @Required
    public void setHostNameToJVMCount(Map<String, Integer> hostNameToJVMCount) {
        this.hostNameToJVMCount = hostNameToJVMCount;
    }

    /**
     * The command to run on each host, to start the JVM / worker. (probably a call to the java executable)
     * @param command The command to run on each host, to start the JVM / worker. (probably a call to the java executable)
     */
    @Required
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * TODO - Really need to think about this one!
     * How to run the command on the remote host, e.g.
     * "ssh -x username@"
     *
     * The hostname will be appended on to this.
     * @param accessPrefix What to stick in front of the hostname.
     */
    @Required
    public void setAccessPrefix(String accessPrefix) {
        this.accessPrefix = accessPrefix;
    }

    
}
