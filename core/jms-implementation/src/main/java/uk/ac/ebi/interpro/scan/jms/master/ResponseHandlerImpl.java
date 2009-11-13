package uk.ac.ebi.interpro.scan.jms.master;

import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.ObjectMessage;
import javax.jms.JMSException;

/**
 * Test implementation of a ResponseHandler.  Just spits out the response to System.out.
 *
 * @author Phil Jones
 * @version $Id: ResponseHandlerImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class ResponseHandlerImpl implements ResponseHandler{
    /**
     * Recieves a Message and does whatever is needed.
     *
     * @param message being a JMS message returned from the worker.
     */
    @Override
    public void handleResponse(Message message) {
        try{
            if (message instanceof TextMessage){
                TextMessage textMessage = (TextMessage) message;
                System.out.println("Response message is: " + textMessage.getText());
            }
            else if (message instanceof ObjectMessage){
                ObjectMessage objectMessage = (ObjectMessage) message;
                System.out.println("Object message (.toString()) is: " + objectMessage.getObject().toString());
            }
            else {
                throw new IllegalArgumentException("Don't know how to handle the message: " + message.toString());
            }
            // TODO - Handle other kinds of response???
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
