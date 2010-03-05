package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.*;
import java.util.Map;

/**
 * This implementation recieves responses on the destinationResponseQueue
 * and then delegates to the injected Handler to respond to them.
 *
 * @author Phil Jones
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class ResponseMonitorImpl implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(ResponseMonitorImpl.class);

    StepExecutionDAO stepExecutionDAO;

    public ResponseMonitorImpl(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }

    @Override
    public void onMessage(Message message) {
        try{
            if (message instanceof ObjectMessage){
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object messageContents = objectMessage.getObject();
                if (messageContents instanceof StepExecution){
                    StepExecution freshStepExecution = (StepExecution) messageContents;
                    stepExecutionDAO.refreshStepExecution(freshStepExecution);
                }
            }
            else {
                LOGGER.error ("Received message that I don't know how to handle.");
                throw new IllegalArgumentException("Don't know how to handle the message: " + message.toString());
            }
        }
        catch (JMSException jmse){
            LOGGER.error("JMSException thrown by Response Monitor on the Master", jmse);
        }
    }
}
