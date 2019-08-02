package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.junit.jupiter.api.Disabled;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * TODO: Description....
 *
 * Well am not sure this test is still required
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@Disabled("looks like ithas never really been swicthed on... .")
public class SeedAlignmentPersisterTest {

    @Resource
    private SeedAlignmentPersister persister;

    @Test
    @Disabled("Turn on only to create a new Pfam release dataset.")
    public void testSeedAlignmentPersister() throws IOException, SQLException {
        persister.loadNewSeedAlignments();
    }
}
