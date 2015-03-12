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
 * Created by IntelliJ IDEA.
 * User: pjones
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class GeneralUnmarshallTest extends AbstractXmlTest<ProteinMatchesHolder> {

    @javax.annotation.Resource()
    private org.springframework.core.io.Resource testXML;

    @Test
    public void testXmlUn304marshalling() throws IOException {
        Assert.assertNotNull("The testXML Resource has not been set", testXML);
        Source source = new StreamSource(new InputStreamReader(testXML.getInputStream()));
        ProteinMatchesHolder pmh = unmarshal(source);
        Assert.assertNotNull("Unmarshalling of the XML has returned a null ProteinMatchesHolder object", pmh);
    }
}
