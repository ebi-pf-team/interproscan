package uk.ac.ebi.interpro.scan.batch.item;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.interpro.scan.model.Protein;

/**
 * Tests {@link uk.ac.ebi.interpro.scan.batch.item.FastaFileItemReader}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public final class FastaFileItemReaderTest {
    
    private static final Log logger = LogFactory.getLog(FastaFileItemReaderTest.class);

    @Autowired
    private FastaFileItemReader reader;

    @Autowired
    private Resource resource;

    @Before public final void setUp()  {
        reader.open(new ExecutionContext());
    }

    @After public final void tearDown()  {
        reader.close();
    }

    @Test public final void testProperties() throws Exception {
        assertNotNull(reader);
    }

    @Test public final void testRead() throws Exception {
        int count = 0;
        Protein protein;
        while ((protein = reader.read()) != null)    {
            if (logger.isDebugEnabled())    {
                logger.debug(protein);
            }
            assertNotNull(protein);
            count++;
        }
        // Try some additional tests
        String fileName = resource.getFilename();
        if (fileName.equals("10.fasta"))    {
            assertEquals(10, count);
        }
        if (fileName.equals("5k.fasta"))    {
            assertEquals(5000, count);
        }
    }

}