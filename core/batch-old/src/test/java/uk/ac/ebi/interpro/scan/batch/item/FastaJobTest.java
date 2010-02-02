package uk.ac.ebi.interpro.scan.batch.item;

import static org.junit.Assert.assertEquals;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.test.AbstractJobTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.Test;

import java.util.Date;

/**
 * Tests FASTA job.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FastaJobTest extends AbstractJobTests {

    @Test public void testJob() throws Exception {
		assertEquals(BatchStatus.COMPLETED, this.launchJob().getStatus());
	}

}
