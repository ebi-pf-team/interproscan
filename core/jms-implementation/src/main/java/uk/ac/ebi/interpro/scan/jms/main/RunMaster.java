package uk.ac.ebi.interpro.scan.jms.main;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.interpro.scan.jms.master.Master;

/**
 * TODO: Add description of class.
 *
 * @author Phil Jones
 * @version $Id: RunMaster.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class RunMaster {
    public static void main(String[] args) {
        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String []{"spring-config-master.xml"});
        ctx.registerShutdownHook();
        Master broker = (Master) ctx.getBean("master");
        broker.run();
        ctx.close();
    }
}
