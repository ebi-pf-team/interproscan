package uk.ac.ebi.interpro.scan.io.gene3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;

import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;
import uk.ac.ebi.interpro.scan.io.ResourceReader;

/**
 * Tests {@link uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceReader}.
 *
 * @author  Antony Quinn
 * @author Gift Nuka
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class DomainFinderResourceReaderTest {

    @Resource
    private ResourceReader<DomainFinderRecord> reader;

    @Resource
    private org.springframework.core.io.Resource file;
    
    static final Collection<DomainFinderRecord> EXPECTED = Arrays.asList(
        new DomainFinderRecord("HP0834", "2hxsA00", 458, 178, 0, 0, 10, 167, 8, 171, 8.6e-10, 41.5, 41.5, 1, "10:167", 8.6e-10),
        new DomainFinderRecord ("HP0834", "2hxsA00", 458, 178, 0, 0, 195, 362, 5, 169, 6.5e-08, 35.4, 35.4, 1, "195:362", 6.5e-08),
        new DomainFinderRecord ("NT01CJ0385", "2hxsA00", 460, 178, 0, 0, 3, 158, 8, 169, 1.5e-07, 34.2, 34.2, 1, "3:158", 1.5e-07),
        new DomainFinderRecord ("NT01CJ0385", "2hxsA00", 460, 178, 0, 0, 193, 362, 4, 169, 7.3e-09, 38.5, 38.5, 1, "193:362", 7.3e-09)
    );

    @Test
    public void testRead() throws IOException {
        assertEquals(EXPECTED, reader.read(file));
    }

}