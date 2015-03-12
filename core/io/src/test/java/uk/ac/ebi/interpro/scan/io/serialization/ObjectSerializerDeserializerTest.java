package uk.ac.ebi.interpro.scan.io.serialization;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Test the object serializer / deserializer
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ObjectSerializerDeserializerTest {

    @Resource
    private ObjectSerializerDeserializer<String> serializerDeserializer;

    @Resource
    private ObjectSerializerDeserializer<String> serializerDeserializerGzip;

    @Test
    public void testSerializerDeserializer() throws IOException {
        String testObject = "Ho, ho, ho";
        serializerDeserializer.serialize(testObject);
        String deserializedObject = serializerDeserializer.deserialize();
        Assert.assertTrue(testObject.equals(deserializedObject));
    }

    @Test
    public void testSerializerDeserializerGzip() throws IOException {

        String testObject = "Ho, ho, ho";
        serializerDeserializerGzip.serialize(testObject);
        String deserializedObject = serializerDeserializerGzip.deserialize();
        Assert.assertTrue(testObject.equals(deserializedObject));
    }


}
