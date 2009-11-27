package uk.ac.ebi.interpro.scan.io.model;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.model.Signature;

import java.io.IOException;
import java.net.URL;

/**
 * Tests {@link Hmmer3ModelLoader}.
 *
 * @author  Phil Jones
 * @version $Id$
 */
public class Hmmer3ModelLoaderTest {

    @Test
    public void testParse() throws IOException{
        URL url = Hmmer3ModelLoaderTest.class.getClassLoader().getResource("data/hmmer3/library/pfam-small.hmm");
        SignatureLibrary library = new SignatureLibrary("Pfam-A", "Pfam A");
        Hmmer3ModelLoader loader = new Hmmer3ModelLoader(library, "24.0");
        SignatureLibraryRelease release = loader.parse(url.getPath());
        assertEquals(library, release.getLibrary());
        assertNotNull(release.getSignatures());
        assertEquals(21, release.getSignatures().size());
        for (Signature signature : release.getSignatures()){
            assertNotNull(signature);
            assertNotNull(signature.getModels());
            
            assertEquals(1, signature.getModels().size());
            System.out.println("signature.getAccession() = " + signature.getAccession());
            System.out.println("signature.getName() = " + signature.getName());
            System.out.println("signature.getDescription() = " + signature.getDescription());

            
        }
    }
}
