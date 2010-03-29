package uk.ac.ebi.interpro.scan.jms.broker;

import org.apache.log4j.Logger;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.springframework.beans.factory.annotation.Required;

import javax.naming.Context;

/**
 * Simple embedded HornetQ Server.
 *
 * This is expected to be run from the Master, which will communicate
 * with this broker using an in-jvm connection.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EmbeddedHornetQBroker implements EmbeddedBroker {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedHornetQBroker.class.getName());

    private Configuration hornetQConfig;

    private String jmsConfigFileName;

    private Context context;

    /**
     * COnfiguration of the hornetQ server.
     * @param hornetQConfig
     */
    @Required
    public void setHornetQConfig(Configuration hornetQConfig) {
        this.hornetQConfig = hornetQConfig;
    }

    @Required
    public void setJmsConfigFileName(String jmsConfigFileName) {
        this.jmsConfigFileName = jmsConfigFileName;
    }

    /**
     * OPTIONAL JNDI Context for the JMS server.
     * @param context OPTIONAL JNDI Context for the JMS server.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void runBroker(){
        try{
            HornetQServer server = HornetQServers.newHornetQServer(hornetQConfig);
            JMSServerManager jmsServerManager = new JMSServerManagerImpl(server, jmsConfigFileName);
            jmsServerManager.setContext(context);
            jmsServerManager.start();
            while (!jmsServerManager.isStarted()) Thread.sleep(1000);
            LOGGER.info("STARTED::");
        }
        catch (Throwable e){
            LOGGER.fatal("Failed to runBroker Broker.", e);
        }
    }
}
