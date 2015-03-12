package uk.ac.ebi.interpro.scan.io.gene3d;

import junitx.framework.FileAssert;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;

import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;
import uk.ac.ebi.interpro.scan.io.ResourceWriter;

/**
 * Tests {@link uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceWriter}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DomainFinderResourceWriterTest {

    @Resource
    private ResourceWriter<DomainFinderRecord> writer;

    @Resource
    private org.springframework.core.io.Resource expectedFile;    

    @Resource
    private org.springframework.core.io.Resource file;

    private static final Collection<DomainFinderRecord> RECORDS = DomainFinderResourceReaderTest.EXPECTED;

    @Test
    public void testWrite() throws IOException {
        writer.write(file, RECORDS);
        FileAssert.assertEquals(expectedFile.getFile(), file.getFile());
    }

}