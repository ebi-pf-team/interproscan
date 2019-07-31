package uk.ac.ebi.interpro.scan.io.serialization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import javax.annotation.Resource;
import java.io.IOException;

/**
 * Test the object serializer / deserializer
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@ExtendWith(SpringExtension.class)
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
        assertTrue(testObject.equals(deserializedObject));
    }

    @Test
    public void testSerializerDeserializerGzip() throws IOException {

        String testObject = "Ho, ho, ho";
        serializerDeserializerGzip.serialize(testObject);
        String deserializedObject = serializerDeserializerGzip.deserialize();
        assertTrue(testObject.equals(deserializedObject));
    }


}
