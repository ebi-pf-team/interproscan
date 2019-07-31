package uk.ac.ebi.interpro.scan.io.match.prosite;


import org.apache.log4j.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.ac.ebi.interpro.scan.model.raw.PfScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.Set;


/**
 * @author Phil Jones, EMBL-EBI
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */


@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class PrositeMatchParserTest {

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
