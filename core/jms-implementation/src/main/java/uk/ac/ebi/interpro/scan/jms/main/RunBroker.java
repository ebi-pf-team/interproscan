package uk.ac.ebi.interpro.scan.jms.main;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.interpro.scan.jms.broker.OnionBroker;

/**
 * TODO: Add description of class.
 *
 * @author Phil Jones
 * @version $Id: RunBroker.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class RunBroker {

    public static void main(String[] args) {
        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String []{"spring-config-broker.xml"});
        ctx.registerShutdownHook();
        OnionBroker broker = (OnionBroker) ctx.getBean("broker");
        broker.start();
        ctx.close();
    }
}
