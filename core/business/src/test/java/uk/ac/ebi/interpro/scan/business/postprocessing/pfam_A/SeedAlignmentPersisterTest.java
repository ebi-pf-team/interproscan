package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SeedAlignmentPersisterTest {

    @Resource
    private SeedAlignmentPersister persister;

    @Test
    @Ignore("Turn on only to create a new Pfam release dataset.")
    public void testSeedAlignmentPersister() throws IOException, SQLException {
        persister.loadNewSeedAlignments();
    }
}
