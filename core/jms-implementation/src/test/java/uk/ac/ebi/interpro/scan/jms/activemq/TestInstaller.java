package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.installer.Installer;

import javax.annotation.Resource;

/**
 * Junit test for the Installer mode.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@Disabled
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
