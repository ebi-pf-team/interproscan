package uk.ac.ebi.interpro.scan.web.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.ac.ebi.interpro.scan.io.ResourceReader;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;



/**
 * Tests for {@link MatchDataResourceReader}
 *
 * @author Matthew Fraser
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
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
        assertEquals(28, result.size(), "Size should be 28.");
    }
}
