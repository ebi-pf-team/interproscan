package uk.ac.ebi.interpro.scan.jms.worker;

import org.springframework.jms.core.JmsTemplate;

/**
 * Created with IntelliJ IDEA.
 * User: pjones, nuka
 * Date: 30/07/12
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public interface Worker extends Runnable  {
    /**
     * The maximum time in seconds for which this Worker can be idle.  If this is exceeded,
     * the Worker will be shut down.
     *
     * @param maximumIdleTime in seconds.
     */
    public void setMaximumIdleTimeSeconds(Long maximumIdleTime);

    /**
     * Irrespective of how busy the Worker is, this sets the maximum time for which the
     * Worker can live.  Note - the implementation should close the Worker cleanly - any
     * work the Worker is doing (e.g. queuing StepInstances or running analyses) must
     * be finished first.
     *
     * @param maximumLife in seconds.
     */
    public void setMaximumLifeSeconds(Long maximumLife);

    /**
     * Sets the Spring JMSTemplate, to allow this Worker to send messages to the local jobRequestQueue
     *
     * <p/>
     * TODO - consider refactoring - this could inject a JmsTemplate directly.
     *
     * @param localJmsTemplate
     */
    public void setLocalJmsTemplate(JmsTemplate localJmsTemplate);

    /**
     *Sets the Spring JMSTemplate, to allow this Worker to send response messages to the remote jobResponseQueue
     *
     * @param remoteJmsTemplate
     */
    public void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate);
}
