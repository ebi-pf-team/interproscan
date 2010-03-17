package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
                                    
import java.util.HashMap;
import java.util.Map;

/**
 * The main entry point for the the master and workers in a
 * Java Messaging configuration of InterProScan.
 *
 * Runs in mode 'master' by default.
 *
 * Usage:
 * java -Dconfig=conf/myconfig.props -jar interproscan-5.jar master
 * java -Dconfig=conf/myconfig.props -jar interproscan-5.jar worker
 * java -Dconfig=conf/myconfig.props -jar interproscan-5.jar monitor
 */

public class Run {

    private static final Logger LOGGER = Logger.getLogger(Run.class.getName());

    private static final Map<String, String> modeToSpringXmlFile = new HashMap<String, String>();

    static {
        modeToSpringXmlFile.put("master", "spring-config-master.xml");
        modeToSpringXmlFile.put("worker", "spring-config-parallel-worker.xml");
        modeToSpringXmlFile.put("monitor", "spring-config-monitor.xml");
    }


    public static void main(String[] args) {
        String mode="master";

        if (args.length>0) {
            mode=args[0];
        }

        if (! modeToSpringXmlFile.keySet().contains(mode)){
            LOGGER.fatal("The mode '" + mode + "' is not handled.  Should be one of: " );
            for (String validMode : modeToSpringXmlFile.keySet()){
                LOGGER.fatal(validMode);
            }
            System.exit(1);
        }

        String config=System.getProperty("config");

        LOGGER.info("Welcome to InterProScan v5");
        LOGGER.info("Running as: "+mode);
        if (config==null){
            LOGGER.info("No custom config used. Use java -Dconfig=");
        }
        else{
            LOGGER.info("Custom config: "+config);
        }
        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String []{modeToSpringXmlFile.get(mode)});
        ctx.registerShutdownHook();
        Runnable main = (Runnable) ctx.getBean(mode);
        main.run();
        ctx.close();
    }
}
