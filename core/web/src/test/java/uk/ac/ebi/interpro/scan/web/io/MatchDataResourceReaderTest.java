package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.ResourceReader;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link MatchDataResourceReader}
 *
 * @author Matthew Fraser
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MatchDataResourceReaderTest {

    @Resource
    private ResourceReader<MatchDataRecord> reader;

    @Resource
    private org.springframework.core.io.Resource file;

    @Test
    public void testRead() throws IOException {
        Collection<MatchDataRecord> result = reader.read(file);
        assertNotNull(result);
        assertEquals("Size should be 28.", 28, result.size());
    }
}
