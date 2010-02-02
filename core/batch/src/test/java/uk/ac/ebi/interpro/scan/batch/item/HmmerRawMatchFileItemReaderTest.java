package uk.ac.ebi.interpro.scan.batch.item;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;

/**
 * Tests reading of HMMER output using {@link FlatFileItemReader}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public final class HmmerRawMatchFileItemReaderTest {
    
    @Autowired
    private FlatFileItemReader<Hmmer3RawMatch> reader;

    @Before
    public final void setUp()  {
        reader.open(new ExecutionContext());
    }

    @After
    public final void tearDown()  {
        reader.close();
    }

    @Test
    public final void testProperties() throws Exception {
        assertNotNull(reader);
    }

    @Test
    public final void testRead() throws Exception {
        final double delta = 0; // max delta between expected and actual for which both numbers are still considered equal
        int count = 0;
        Hmmer3RawMatch match;
        while ((match = reader.read()) != null)    {
            count++;
            assertNotNull(match);
            match.getSequenceIdentifier();
            switch (count)  {
                case 1:
                    assertEquals("9d380adca504b0b1a2654975c340af78", match.getSequenceIdentifier());
                    assertEquals("PF02310", match.getModel());
                    assertEquals(3.7E-9, match.getDomainIeValue(), delta);
                    break;
                case 2:
                    assertEquals("1adb07a14aa81b3033f2d33059670f2d", match.getSequenceIdentifier());
                    assertEquals("PF00058", match.getModel());
                    assertEquals(4.9E-13, match.getDomainIeValue(), delta);
                    break;
                case 3:
                    assertEquals("1adb07a14aa81b3033f2d33059670f2d", match.getSequenceIdentifier());
                    assertEquals("PF00058", match.getModel());
                    assertEquals(1.3E-16, match.getDomainIeValue(), delta);
                    break;                
            }
        }
        assertEquals(3, count);
    }

}
