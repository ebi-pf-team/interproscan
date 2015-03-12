package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 *
 * The LANServerSettings class is used as an object to hold the information regarding
 * the settings (name, JVM count, Xmx memory size) for a particular server machine.
 * Only hostname is actually required, other values have default values: vmNumber = 1,
 * memoryUsage = -DXmx-1024m.
 *
 * User: maslen
 * Date: Feb 10, 2010
 * Time: 9:25:17 AM
 */
public class LANServerSettings {
    /**
    * What is the fully-qualified name of the server machine?
    */
    private String hostname;

   /**
    * How many JVMs do you want to run on this machine in parallel?
    */
    private int vmNumber = 1;

   /**
    * What do you want to set the Xmx memory usage to for each JVM?
    */
    private String memory = "-DXmx=1024m";

    @Required
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setVmNumber(int vmNumber) {
        this.vmNumber = vmNumber;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getHostname() {
        return hostname;
    }

    public int getVmNumber() {
        return vmNumber;
    }

    public String getMemory() {
        return memory;
    }
}
