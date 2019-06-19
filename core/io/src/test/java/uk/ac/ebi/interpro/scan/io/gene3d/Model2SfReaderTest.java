package uk.ac.ebi.interpro.scan.io.gene3d;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link Model2SfReader}.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@ExtendWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class Model2SfReaderTest {

    @Resource
    private org.springframework.core.io.Resource file;

    @Test
    @Ignore("This test needs re-writing to fit in with the change to the Model2SfReader class.")
    public void testRead() throws IOException {
        Model2SfReader reader = new Model2SfReader();
        reader.setModelFiles(file);
        reader.setSignatureLibrary(SignatureLibrary.GENE3D);
        reader.setReleaseVersionNumber("3.3.0");
        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("1j09A04", "G3DSA:1.10.8.70");
        expected.put("3c3dA02", "G3DSA:1.10.8.240");
        expected.put("3c9pA00", "G3DSA:1.10.8.290");
        expected.put("2ptrB03", "G3DSA:1.10.40.30");
        expected.put("1vdkB03", "G3DSA:1.10.40.30");
        assertEquals(expected, reader.parse());
    }

}
