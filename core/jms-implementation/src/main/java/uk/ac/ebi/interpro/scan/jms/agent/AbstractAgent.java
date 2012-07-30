package uk.ac.ebi.interpro.scan.jms.agent;

import org.springframework.jms.core.JmsTemplate;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * Date: 30/07/12
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAgent implements Agent {
    protected final long startUpTime = new Date().getTime();
    protected long maximumIdleTimeMillis = Long.MAX_VALUE;
    protected long maximumLifeMillis = Long.MAX_VALUE;
    protected JmsTemplate jmsTemplate;

    public void setMaximumIdleTimeSeconds(Long maximumIdleTime) {
        this.maximumIdleTimeMillis = maximumIdleTime * 1000;
    }

    public void setMaximumLifeSeconds(Long maximumLife) {
        this.maximumLifeMillis = maximumLife * 1000;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
}
