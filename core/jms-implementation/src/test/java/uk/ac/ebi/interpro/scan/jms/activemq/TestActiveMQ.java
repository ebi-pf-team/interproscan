package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.jms.master.BlackBoxMaster;

import javax.annotation.Resource;

/**
 * Currently a stub for testing the ActiveMQ setup.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestActiveMQ {

    private static final Logger LOGGER = Logger.getLogger(TestActiveMQ.class.getName());

    @Resource
    private BlackBoxMaster amqstandalone;


    @Test
    @Ignore("Needs to be reconfigured.")
    public void testEmbeddedSystem() {
        LOGGER.debug("If this test fails, check that the test database located in /src/test/resources/interpro.h2.db is up-to-date with " +
                "the current schema.  If not, the easiest way to create a new empty database is to use the installer " +
                "with all member database loading tasks commented out in the installer-context.xml file.");
        amqstandalone.setFastaFilePath("5.fasta");
        amqstandalone.run();

        // TODO - Test the contents of the database  - look for proteins / sequences / completed steps etc.
    }
}
