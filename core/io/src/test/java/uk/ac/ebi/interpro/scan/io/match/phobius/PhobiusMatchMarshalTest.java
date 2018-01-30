package uk.ac.ebi.interpro.scan.io.match.phobius;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.PhobiusMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a marshal test for PhobiusMatches.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PhobiusMatchMarshalTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(PhobiusMatchMarshalTest.class.getName());

    private Jaxb2Marshaller marshaller;

    public void init() {
        marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Protein.class);
        Map<String, Boolean> properties = new HashMap<String, Boolean>();
        properties.put("jaxb.formatted.output", true);
        marshaller.setMarshallerProperties(properties);
    }


    @Test
    public void testMarshalPhobiusMatch() {
        init();
        final Set<PhobiusMatch.PhobiusLocation> locations = new HashSet<PhobiusMatch.PhobiusLocation>();
        locations.add(new PhobiusMatch.PhobiusLocation(1, 2));
        final Signature signature = new Signature.Builder("SIGNAL_PEPTIDE").name("Signal Peptide").build();
        final PhobiusMatch match = new PhobiusMatch(signature, "SIGNAL_PEPTIDE", locations);
        final Protein protein = new Protein("aaa");
        protein.addMatch(match);
        try {
            final String result = marshal(marshaller, protein);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(result);
            }
            assertNotNull("XML result shouldn't be null!", result);
            assertTrue(result.contains("<matches>"));
            assertTrue(result.contains("<phobius-match>"));
            assertTrue(result.contains("<signature"));
            assertTrue(result.contains("name=\"Signal Peptide\""));
            assertTrue(result.contains("ac=\"SIGNAL_PEPTIDE\""));
            assertTrue(result.contains("<locations>"));
            assertTrue(result.contains("<phobius-location"));
            assertTrue(result.contains("end=\"2\""));
            assertTrue(result.contains("start=\"1\""));
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
