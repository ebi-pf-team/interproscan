package uk.ac.ebi.interpro.scan.jms.stats;

import org.springframework.jms.core.ProducerCallback;

import javax.jms.*;

/**
 * @author Gift Nuka, Maxim
 * Date: 20/08/12
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
public class ProducerCallbackImpl implements ProducerCallback {

    private Destination replyQueue;

    public ProducerCallbackImpl(Destination replyQueue) {
        this.replyQueue = replyQueue;
    }

    @Override
    public Object doInJms(Session session, MessageProducer messageProducer) throws JMSException {

        Message message = session.createMessage();
        message.setJMSReplyTo(replyQueue);
        messageProducer.send(message);
        return null;
    }
}
