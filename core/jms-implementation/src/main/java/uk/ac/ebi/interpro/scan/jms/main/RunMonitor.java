package uk.ac.ebi.interpro.scan.jms.main;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.interpro.scan.jms.monitor.WorkerMonitorController;

/**
 * TODO: Add description of class.
 *
 * @author Phil Jones
 * @version $Id: RunMonitor.java,v 1.1 2009/10/21 18:44:40 pjones Exp $
 * @since 1.0
 */
public class RunMonitor {

    public static void main(String[] args) {
        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String []{"spring-config-monitor.xml"});
        ctx.registerShutdownHook();
        WorkerMonitorController worker = (WorkerMonitorController) ctx.getBean("monitor");
        worker.run();
        ctx.close();
    }
    
}
