package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.ResourceReader;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for {@link StructuralMatchDataResourceReader}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class StructuralMatchDataResourceReaderTest {

    @Resource
    private ResourceReader<StructuralMatchDataRecord> reader;

    @Resource
    private org.springframework.core.io.Resource file;

    @Test
    public void testRead() throws IOException {
        Collection<StructuralMatchDataRecord> result = reader.read(file);
        assertNotNull(result);
        assertEquals(13, result.size());
    }
}
