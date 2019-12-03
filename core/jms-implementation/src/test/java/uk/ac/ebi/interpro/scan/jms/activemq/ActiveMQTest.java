package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.interpro.scan.jms.master.BlackBoxMaster;

import javax.annotation.Resource;

/**
 * Currently a stub for testing the ActiveMQ setup.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@Disabled("Needs to be reconfigured.")
public class ActiveMQTest {

    private static final Logger LOGGER = Logger.getLogger(ActiveMQTest.class.getName());

    @Resource
    private BlackBoxMaster amqstandalone;


    @Test
    @Disabled("Needs to be reconfigured.")
    public void testEmbeddedSystem() {
        LOGGER.debug("If this test fails, check that the test database located in /src/test/resources/interpro.h2.db is up-to-date with " +
                "the current schema.  If not, the easiest way to create a new empty database is to use the installer " +
                "with all member database loading tasks commented out in the installer-context.xml file.");
        amqstandalone.setFastaFilePath("5.fasta");
        amqstandalone.run();

        // TODO - Test the contents of the database  - look for proteins / sequences / completed steps etc.
    }
}
