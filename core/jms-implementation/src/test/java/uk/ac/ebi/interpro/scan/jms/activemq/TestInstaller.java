package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.installer.Installer;

import javax.annotation.Resource;

/**
 * Junit test for the Installer mode.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore
public class TestInstaller {

    private static final Logger LOGGER = Logger.getLogger(TestInstaller.class.getName());

    @Resource
    private Installer installer;

    @Resource
    private TemporaryDirectoryManager tempDirectoryManager;

    @Test
    public void testInstaller() {
        installer.run();
    }
}
