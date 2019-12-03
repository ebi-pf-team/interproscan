package uk.ac.ebi.interpro.scan.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * @author pjones
 * @author Gift Nuka
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class GeneralUnmarshallTest extends AbstractXmlTest<ProteinMatchesHolder> {

    @javax.annotation.Resource()
    private org.springframework.core.io.Resource testXML;

    @Test
    public void testXmlUn304marshalling() throws IOException {
        assertNotNull( testXML, "The testXML Resource has not been set");
        Source source = new StreamSource(new InputStreamReader(testXML.getInputStream()));
        ProteinMatchesHolder pmh = unmarshal(source);
        assertNotNull( pmh,"Unmarshalling of the XML has returned a null ProteinMatchesHolder object");
    }
}
