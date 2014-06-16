package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Test the PIRSF raw matches temporary file parser.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */
public class PirsfMatchTempParserTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(PirsfMatchTempParserTest.class.getName());

    private Resource testFile = new ClassPathResource("data/pirsf/hmmer2/matchesTemp.out");

    @Test
    public void testPirsfMatchTempParser() throws IOException {

        LOGGER.warn("Note that some inputs are deliberately wrong, so errors/warnings may be thrown by this test! " +
                "Does it pass?");

        // Run test method
        PirsfMatchTempParser parser = new PirsfMatchTempParser();
        Set<RawProtein<PIRSFHmmer2RawMatch>> actualResult = PirsfMatchTempParser.parse(testFile.getURI().getPath());

        // Compare actual result with expected result
        Assert.assertEquals(1, actualResult.size());

        Iterator<RawProtein<PIRSFHmmer2RawMatch>> i = actualResult.iterator();
        RawProtein<PIRSFHmmer2RawMatch> rawProtein = i.next();

        Collection<PIRSFHmmer2RawMatch> rawMatches = rawProtein.getMatches();
        Assert.assertEquals(1, rawMatches.size());
        Iterator<PIRSFHmmer2RawMatch> j = rawMatches.iterator();
        PIRSFHmmer2RawMatch rawMatch = j.next();

        // Setup expected result
        String proteinId = "1";
        String modelId = "PIRSF001500";
        SignatureLibrary library = SignatureLibrary.PIRSF;
        String signatureLibraryRelease = "2.78";
        int locationStart = 1;
        int locationEnd = 364;
        // Perform tests
        Assert.assertEquals(proteinId, rawProtein.getProteinIdentifier());
        Assert.assertEquals(modelId, rawMatch.getModelId());
        Assert.assertEquals(library, rawMatch.getSignatureLibrary());
        Assert.assertEquals(signatureLibraryRelease, rawMatch.getSignatureLibraryRelease());
        Assert.assertEquals(locationStart, rawMatch.getLocationStart());
        Assert.assertEquals(locationEnd, rawMatch.getLocationEnd());
    }
}
