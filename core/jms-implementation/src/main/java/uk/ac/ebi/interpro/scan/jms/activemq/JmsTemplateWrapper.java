package uk.ac.ebi.interpro.scan.jms.activemq;

import org.springframework.jms.core.JmsTemplate;

/**
 * Wraps a JMS template, to (for example) allow it to be created at runtime.
 * Date: 21/09/11
 *
 * @author Phil Jones
 */
public class JmsTemplateWrapper {

    private JmsTemplate template;

    public JmsTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }
}
