package uk.ac.ebi.interpro.scan.model;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Phil Jones
 * @author Gift Nuka
 *         Date: 12/03/12
 *         Time: 12:14
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class AAXmlTest extends AbstractXmlTest<ProteinMatchesHolder> {

    @javax.annotation.Resource()
    private org.springframework.core.io.Resource testXML;


    /**
     * This test method was written initially to solve the problem of not being able to unmarshall
     * TMHMM matches.
     *
     * @throws IOException
     */
    @Test
    public void testXmlUnmarshalling() throws IOException {
        assertNotNull( testXML, "The testXML Resource has not been set");
        Source source = new StreamSource(new InputStreamReader(testXML.getInputStream()));
        ProteinMatchesHolder pmh = unmarshal(source);
        assertNotNull( pmh, "Unmarshalling of the XML has returned a null ProteinMatchesHolder object");
        boolean foundTMHMM = false;
        boolean foundCoils = false;
        boolean foundPhobius = false;
        boolean foundPrositeProfile = false;
        // Search for a TMHMM match
        for (Protein protein : pmh.getProteins()) {
            if ("3aa683c2f45177660a5105dd430aaf07".equalsIgnoreCase(protein.getMd5())) {
                for (Match match : protein.getMatches()) {
                    Signature signature = match.getSignature();
                    assertNotNull(signature);
                    SignatureLibraryRelease release = signature.getSignatureLibraryRelease();
                    assertNotNull(release);
                    SignatureLibrary library = release.getLibrary();

                    if (match instanceof TMHMMMatch) {
                        foundTMHMM = true;
                        assertEquals(SignatureLibrary.TMHMM, library);
                    } else if (match instanceof CoilsMatch) {
                        foundCoils = true;
                        assertEquals(SignatureLibrary.COILS, library);
                    } else if (match instanceof PhobiusMatch) {
                        foundPhobius = true;
                        assertEquals(SignatureLibrary.PHOBIUS, library);
                    } else if (match instanceof ProfileScanMatch) {
                        foundPrositeProfile = true;
                        assertEquals(SignatureLibrary.PROSITE_PROFILES, library);
                    }
                }
                break;
            }
        }
        assertTrue( foundCoils, "Not found the Coils match");
        assertTrue( foundPhobius, "Not found the Phobius match");
        assertTrue( foundPrositeProfile, "Not found the Prosite Profile match");
        assertTrue( foundTMHMM, "Not found the TMHMM match");
    }

}
