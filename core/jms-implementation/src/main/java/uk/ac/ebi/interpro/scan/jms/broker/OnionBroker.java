package uk.ac.ebi.interpro.scan.jms.broker;


import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.broker.platforms.WorkerRunner;

/**
 * @author Phil Jones
 */
public class OnionBroker {

    private QueueJumper queueJumper;
    private String connectionConfigurationXml;
    private String jmsConfigurationXml;
    private WorkerRunner serialWorkerRunner;

    @Required
    public void setQueueJumper(QueueJumper queueJumper) {
        this.queueJumper = queueJumper;
    }

    @Required
    public void setConnectionConfigurationXml(String connectionConfigurationXml) {
        this.connectionConfigurationXml = connectionConfigurationXml;
    }

    @Required
    public void setJmsConfigurationXml(String jmsConfigurationXml) {
        this.jmsConfigurationXml = jmsConfigurationXml;
    }

    @Required
    public void setSerialWorkerRunner(WorkerRunner serialWorkerRunner) {
        this.serialWorkerRunner = serialWorkerRunner;
    }

    public void start(){
        try{
            FileConfiguration configuration = new FileConfiguration();
            configuration.setConfigurationUrl(connectionConfigurationXml);
            configuration.start();

            HornetQServer server = HornetQServers.newHornetQServer(configuration);
            JMSServerManager jmsServerManager = new JMSServerManagerImpl(server, jmsConfigurationXml);
            //if you want to use JNDI, simple inject a context here or don't call this method and make sure the JNDI parameters are set.
            jmsServerManager.setContext(null);
            jmsServerManager.start();
            System.out.println("STARTED::");

            // Start up the Thread that monitors the taskSubmission queue.
            Thread queueMonitorThread = new Thread (queueJumper);
            // Needs to keep going...
            queueMonitorThread.setDaemon(true);
            queueMonitorThread.start();

            // Start up the serial worker
            serialWorkerRunner.startupNewWorker();
        }
        catch (Throwable e){
            System.out.println("FAILED::");
            e.printStackTrace();
        }
    }
}
