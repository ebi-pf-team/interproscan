package uk.ac.ebi.interpro.scan.web.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.interpro.scan.io.ResourceReader;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link StructuralMatchDataResourceReader}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
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
