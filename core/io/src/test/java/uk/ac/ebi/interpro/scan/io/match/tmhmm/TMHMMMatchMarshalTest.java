package uk.ac.ebi.interpro.scan.io.match.tmhmm;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a marshal test for TMHMM matches.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TMHMMMatchMarshalTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(TMHMMMatchMarshalTest.class.getName());

    private Jaxb2Marshaller marshaller;

    public void init() {
        marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Protein.class);
        Map<String, Boolean> properties = new HashMap<String, Boolean>();
        properties.put("jaxb.formatted.output", true);
        marshaller.setMarshallerProperties(properties);
    }


    @Test
    public void testMarshalTMHMMMatch() {
        init();
        final Set<TMHMMMatch.TMHMMLocation> locations = new HashSet<TMHMMMatch.TMHMMLocation>();
        locations.add(new TMHMMMatch.TMHMMLocation(1, 2, "TMHelix", 1.0f));
        final Signature signature = new Signature.Builder("TMhelix").build();
        final TMHMMMatch match = new TMHMMMatch(signature, locations);
        final Protein protein = new Protein("aaa");
        protein.addMatch(match);
        try {
            final String result = marshal(marshaller, protein);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(result);
            String resultFileStr = "XML result file:\n " + result;
            assertNotNull("XML result shouldn't be null!", result);
            assertTrue("Missed <matches> tag! " + resultFileStr, result.contains("<matches>"));
            assertTrue("Missed <tmhmm-match> tag! " + resultFileStr, result.contains("<tmhmm-match>"));
            assertTrue("Missed <signature> tag or tag values are wrong! " + resultFileStr, result.contains("<signature ac=\"TMhelix\"/>"));
            assertTrue("Missed <locations> tag! " + resultFileStr, result.contains("<locations>"));
            assertTrue("Missed <tmhmm-location> tag or tag values are wrong! " + resultFileStr, result.contains("<tmhmm-location"));
            assertTrue("Missed <tmhmm-location> tag or tag values are wrong! " + resultFileStr, result.contains("end=\"2\""));
            assertTrue("Missed <tmhmm-location> tag or tag values are wrong! " + resultFileStr, result.contains("start=\"1\""));
        } catch (IOException e) {
            LOGGER.warn("Couldn't marshal protein object!", e);
        }
    }

    /**
     * Marshalls an object to an XML, returned as a String instance.
     *
     * @param marshaller to perform the object -> XML marshalling
     * @param o          the object to marshall to XML
     * @return the XML in a String
     * @throws java.io.IOException in the event of an error writing out the XML to the StringWriter.
     */
    private String marshal(Marshaller marshaller, Object o) throws IOException {
        Writer writer = new StringWriter();
        marshaller.marshal(o, new StreamResult(writer));
        String xml = writer.toString();
        LOGGER.debug("\n" + xml);
        return xml;
    }
}
