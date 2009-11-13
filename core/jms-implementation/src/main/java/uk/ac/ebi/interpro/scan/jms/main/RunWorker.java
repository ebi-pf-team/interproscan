package uk.ac.ebi.interpro.scan.jms.main;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.interpro.scan.jms.worker.Worker;

/**
 * TODO: Add description of class.
 *
 * @author Phil Jones
 * @version $Id: RunWorker.java,v 1.2 2009/10/16 12:04:44 pjones Exp $
 * @since 1.0
 */
public class RunWorker {

    public static void main(String[] args) {
        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String []{"spring-config-worker.xml"});
        ctx.registerShutdownHook();
        Worker worker = (Worker) ctx.getBean("worker");
        worker.start();
        ctx.close();
    }
}
