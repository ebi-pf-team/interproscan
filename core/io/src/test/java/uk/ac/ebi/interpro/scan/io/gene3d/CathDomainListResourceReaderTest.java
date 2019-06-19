package uk.ac.ebi.interpro.scan.io.gene3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;

import uk.ac.ebi.interpro.scan.io.ResourceReader;

/**
 * Tests {@link CathDomainListResourceReader}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@ExtendWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CathDomainListResourceReaderTest {

    @Resource
    private ResourceReader<CathDomainListRecord> reader;

    @Resource
    private org.springframework.core.io.Resource file;

    private static final Collection<CathDomainListRecord> EXPECTED = Arrays.asList(
        new CathDomainListRecord("1oaiA00", 1, 10,  8,  10),
        new CathDomainListRecord("1go5A00", 1, 10,  8,  10),
        new CathDomainListRecord("1oksA00", 1, 10,  8,  10),
        new CathDomainListRecord("1ws8C00", 2, 60, 40, 420),
        new CathDomainListRecord("1ws8D00", 2, 60, 40, 420)
    );

    @Test
    public void testRead() throws IOException {
        assertEquals(EXPECTED, reader.read(file));
    }

}