package uk.ac.ebi.interpro.scan.jms.master;

import uk.ac.ebi.interpro.scan.jms.broker.EmbeddedBroker;

/**
 * Interface for the Master application.
 *
 * @author Phil Jones
 * @version $Id: Master.java,v 1.2 2009/10/16 12:05:10 pjones Exp $
 * @since 1.0
 */
public interface Master extends Runnable{

    /**
     * This OPTIONAL bean method allows an Embedded JMS broker to be injected.
     * If not injected, the Master will make no attempt to runBroker a Broker, but
     * rely on an external one being present.
     * @param embeddedBroker implementation, e.g. for HornetQ, ActiveMQ.
     */
    public void setEmbeddedBroker(EmbeddedBroker embeddedBroker);

}
