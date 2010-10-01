package uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.serialization.ObjectSerializerDeserializer;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.annotation.Resource;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * Tests the InterPro.xml unmarshaller, used to extract InterPro entry and GO term data.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class InterProXMLUnmarshallerTest {

    @Resource
    private org.springframework.core.io.Resource interproXmlFile;

    @Resource
    private InterProXMLUnmarshaller unmarshaller;

    @Resource
    private ObjectSerializerDeserializer<Map<SignatureLibrary, SignatureLibraryIntegratedMethods>> serializerDeserializer;


    @Test
    public void testUnmarshallerAndSerialization()
            throws IOException, XMLStreamException, ClassNotFoundException {
        BufferedInputStream bis = null;
        try {

            if (interproXmlFile.getFilename().endsWith("gz")) {
                bis = new BufferedInputStream(new GZIPInputStream(interproXmlFile.getInputStream()));
            } else if (interproXmlFile.getFilename().endsWith("zip")) {
                bis = new BufferedInputStream(new ZipInputStream(interproXmlFile.getInputStream()));
            } else {
                bis = new BufferedInputStream(interproXmlFile.getInputStream());
            }

            Map<SignatureLibrary, SignatureLibraryIntegratedMethods> unmarshalledData = unmarshaller.unmarshal(bis);

            // Serialize out un-marshaled data
            serializerDeserializer.serialize(unmarshalledData);

            // Serialize back in and cheque for equality.
            Map<SignatureLibrary, SignatureLibraryIntegratedMethods> retrievedData = serializerDeserializer.deserialize();

            Assert.assertTrue(unmarshalledData.equals(retrievedData));

        }
        finally {
            if (bis != null) bis.close();
        }
    }

}
