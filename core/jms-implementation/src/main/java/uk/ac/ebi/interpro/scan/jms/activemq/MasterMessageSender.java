package uk.ac.ebi.interpro.scan.jms.activemq;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.jms.master.ClusterState;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.jms.JMSException;

/**
 * Interface for the message sender used by the Master implementation.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface MasterMessageSender {

    /**
     * Creates messages to be sent to Worker nodes.
     * Does all of this in a transaction, hence in this separate interface.
     *
     * @param stepInstance to send as a message
     * @throws javax.jms.JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    void sendMessage(StepInstance stepInstance, final boolean highMemory, final int priority, final boolean canRunRemotely) throws JMSException;

    void sendShutDownMessage();


    void sendTopicMessage(final ClusterState clusterState);
}
