package uk.ac.ebi.interpro.scan.io.gene3d;

import static junit.framework.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import uk.ac.ebi.interpro.scan.io.ResourceReader;

/**
 * Tests {@link Model2SfReader}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class Model2SfReaderTest {

    @Resource
    private org.springframework.core.io.Resource file;

    @Test
    public void testRead() throws IOException {
        Model2SfReader reader = new Model2SfReader();        
        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("1j09A04", "G3DSA:1.10.8.70");
        expected.put("3c3dA02", "G3DSA:1.10.8.240");
        expected.put("3c9pA00", "G3DSA:1.10.8.290");
        expected.put("2ptrB03", "G3DSA:1.10.40.30");
        expected.put("1vdkB03", "G3DSA:1.10.40.30");
        assertEquals(expected, reader.read(file));
    }

}