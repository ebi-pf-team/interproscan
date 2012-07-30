package uk.ac.ebi.interpro.scan.jms.agent;

import org.springframework.jms.core.JmsTemplate;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Gift Nuka, Phil Jones.
 */
public interface Agent extends Runnable {

    /**
     * The maximum time in seconds for which this Agent can be idle.  If this is exceeded,
     * the agent will be shut down.
     *
     * @param maximumIdleTime in seconds.
     */
    public void setMaximumIdleTimeSeconds(Long maximumIdleTime);

    /**
     * Irrespective of how busy the agent is, this sets the maximum time for which the
     * Agent can live.  Note - the implementation should close the Agent cleanly - any
     * work the agent is doing (e.g. queuing StepInstances or running analyses) must
     * be finished first.
     *
     * @param maximumLife in seconds.
     */
    public void setMaximumLifeSeconds(Long maximumLife);

    /**
     * Sets the Spring JMSTemplateWrapper, to allow this Agent to receive
     * JMS messages from a Broker (all configured in Spring of course).
     * <p/>
     * TODO - It looks like JmsTemplateWrapper is a pointless class that wraps a JmsTemplate.
     * TODO - consider refactoring - this could inject a JmsTemplate directly.
     *
     * @param jmsTemplate
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate);

}
