package uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro;

import org.junit.jupiter.api.Assertions;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * <p/>
 * Note that this test should be turned ON, with xmx set to a high value to generate a new InterPro Entry / GO mapping
 * file for the build.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@ExtendWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class InterProXMLUnmarshallerTest {

    @Resource
    private org.springframework.core.io.Resource interproXmlFile;

    @Resource
    private InterProXMLUnmarshaller unmarshaller;

    @Resource
    private ObjectSerializerDeserializer<Map<SignatureLibrary, SignatureLibraryIntegratedMethods>> serializerDeserializer;


    /**
     * Run with
     * <p/>
     * mvn clean test -Pentry-go,default-test
     * <p/>
     * The resulting serialized data will then be in ./target/interproEntryGoMapping.gz
     * <p/>
     * This file should then be copied to
     * <p/>
     * jms-implementation/support-mini-x86-32/interproEntryGoMapping-NN.N.gz
     * <p/>
     * and the version number updated in all properties files.
     * <p/>
     * (DON'T overwrite old versions - commit alongside them please).
     *
     * @throws IOException
     * @throws XMLStreamException
     * @throws ClassNotFoundException
     */
    @Test
    @Ignore("Slow and memory intensive - turn on only to rebuild the InterPro / GO mapping file (each InterPro release)")
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

        } finally {
            if (bis != null) bis.close();
        }
    }

}
