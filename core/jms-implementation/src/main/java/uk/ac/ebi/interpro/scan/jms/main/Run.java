package uk.ac.ebi.interpro.scan.jms.main;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.interpro.scan.jms.master.Master;

/**
 * The main entry point for the the master and workers in a
 * Java Messaging configuration of InterProScan.
 *
 * java -Dconfig=conf/myconfig.props -jar interproscan-5.jar master
 */

public class Run {

    public static void main(String[] args) {
        String mode="master";

        if (args.length>0) mode=args[0];

        String config=System.getProperty("config");

        System.out.println("Welcome to InterProScan v5");
        System.out.println("Running as: "+mode);
        if (config==null)
            System.out.println("No custom config used. Use java -Dconfig=");
        else
            System.out.println("Custom config: "+config);

        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String []{"spring-config-all.xml"});
        ctx.registerShutdownHook();
        Runnable main = (Runnable) ctx.getBean(mode);
        main.run();
        ctx.close();
    }
}
