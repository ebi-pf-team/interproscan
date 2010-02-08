package uk.ac.ebi.interpro.scan.jms.master;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.util.Map;

/**
 * Test implementation of a ResponseHandler.  Just spits out the response to System.out.
 *
 * @author Phil Jones
 * @version $Id: ResponseHandlerImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class InterProScanHandlerImpl implements ResponseHandler{


    private volatile Map<String, StepExecution> stepExecutions;

    private StepExecutionDAO stepExecutionDAO;

    @Required
    public void setStepExecutionDAO(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }

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
                System.out.println("Text Message Received: " + textMessage.getText());
            }
            else if (message instanceof ObjectMessage){
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object messageContents = objectMessage.getObject();
                if (messageContents instanceof StepExecution){
                    StepExecution freshStepExecution = (StepExecution) messageContents;
                    // Some StepExecution responses may require further action
                    // (e.g. data persistence) before continuing, so deal with that here.
                    // HOPEFULLY NOT THOUGH!
                    processStepExecution(freshStepExecution);

                    stepExecutionDAO.refreshStepExecution(freshStepExecution);
                }
            }
            else {
                throw new IllegalArgumentException("Don't know how to handle the message: " + message.toString());
            }
            // TODO - Handle other kinds of response???
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    private void processStepExecution(StepExecution freshStepExecution) {
        // Currently no-op - hopefully it will stay that way.
    }

    /**
     * Sets a reference to the stepExecutions so that they can be
     * updated from the response to the Master.
     *
     * @param stepExecutions that have been completed on remote nodes.
     */
    @Override
    public void setStepExecutionMap(Map<String, StepExecution> stepExecutions) {
        this.stepExecutions = stepExecutions;
    }
}
