package uk.ac.ebi.interpro.scan.batch.item;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.batch.core.*;
import org.springframework.batch.test.AbstractJobTests;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Executes sample job to merge several files into one file.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FileMergeJobTest extends AbstractJobTests {

    @Test
    public void testJob() throws Exception {
		assertEquals(BatchStatus.COMPLETED, this.launchJob().getStatus());
	}

}