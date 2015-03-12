package uk.ac.ebi.interpro.scan.model;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Phil Jones
 *         Date: 12/03/12
 *         Time: 12:14
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
        Assert.assertNotNull("The testXML Resource has not been set", testXML);
        Source source = new StreamSource(new InputStreamReader(testXML.getInputStream()));
        ProteinMatchesHolder pmh = unmarshal(source);
        Assert.assertNotNull("Unmarshalling of the XML has returned a null ProteinMatchesHolder object", pmh);
        boolean foundTMHMM = false;
        boolean foundCoils = false;
        boolean foundPhobius = false;
        boolean foundPrositeProfile = false;
        // Search for a TMHMM match
        for (Protein protein : pmh.getProteins()) {
            if ("3aa683c2f45177660a5105dd430aaf07".equalsIgnoreCase(protein.getMd5())) {
                for (Match match : protein.getMatches()) {
                    Signature signature = match.getSignature();
                    Assert.assertNotNull(signature);
                    SignatureLibraryRelease release = signature.getSignatureLibraryRelease();
                    Assert.assertNotNull(release);
                    SignatureLibrary library = release.getLibrary();

                    if (match instanceof TMHMMMatch) {
                        foundTMHMM = true;
                        Assert.assertEquals(SignatureLibrary.TMHMM, library);
                    } else if (match instanceof CoilsMatch) {
                        foundCoils = true;
                        Assert.assertEquals(SignatureLibrary.COILS, library);
                    } else if (match instanceof PhobiusMatch) {
                        foundPhobius = true;
                        Assert.assertEquals(SignatureLibrary.PHOBIUS, library);
                    } else if (match instanceof ProfileScanMatch) {
                        foundPrositeProfile = true;
                        Assert.assertEquals(SignatureLibrary.PROSITE_PROFILES, library);
                    }
                }
                break;
            }
        }
        Assert.assertTrue("Not found the Coils match", foundCoils);
        Assert.assertTrue("Not found the Phobius match", foundPhobius);
        Assert.assertTrue("Not found the Prosite Profile match", foundPrositeProfile);
        Assert.assertTrue("Not found the TMHMM match", foundTMHMM);
    }

}
