package uk.ac.ebi.interpro.scan.io.match.prosite;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.raw.PfScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.Set;


/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PrositeMatchParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(PrositeMatchParserTest.class.getName());

    @javax.annotation.Resource
    org.springframework.core.io.Resource hamapGffFile;

    @javax.annotation.Resource
    PrositeMatchParser hamapMatchParser;

    @javax.annotation.Resource
    String signatureLibraryRelease;

    @Test
    public void testParserForHAMAP() throws IOException {
        assertNotNull(hamapGffFile);
        assertNotNull(hamapMatchParser);
        Set<RawProtein<PfScanRawMatch>> rawProteins = hamapMatchParser.parse(hamapGffFile.getInputStream());
        assertNotNull(rawProteins);
        assertEquals(20, rawProteins.size());
    }

}
