package uk.ac.ebi.interpro.scan.condensed.berkeley;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 */
public class Main {
    public static void main(String[] args) {
        final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("spring/htmlWriter-context.xml");
        final BerkeleyDBCreator creator = (BerkeleyDBCreator) ctx.getBean("dbCreator");
        creator.run(new String[]{"output", "jdbc:oracle:thin:@cobra.ebi.ac.uk:1531:IPPRO", "username", "password"});
    }
}
