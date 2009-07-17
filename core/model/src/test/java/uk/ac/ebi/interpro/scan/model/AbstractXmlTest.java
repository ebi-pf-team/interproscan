/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.annotation.Resource;
import java.io.*;
import java.util.Map;

/**
 * Utility class for running XmlUnit tests.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     XMLUnit
 */
abstract class AbstractXmlTest<T> {

    private static final Log logger = LogFactory.getLog(AbstractXmlTest.class);

    @Resource
    private Marshaller marshaller;

    @Resource
    private Unmarshaller unmarshaller;

    @Resource
    private Map<String, ObjectXmlPair<T>> objectXmlMap;

    static {
        // Ignore comments and whitespace when comparing XML
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    protected void testSupportsMarshalling(Class c) {
        assertTrue(marshaller.supports(c));
        assertTrue(unmarshaller.supports(c));
    }
    
    /**
     * Performs XML round-trip using {@link Map} of {@link ObjectXmlPair}.
     *
     * @throws IOException   if problem marshalling or unmarshalling
     * @throws SAXException  if cannot parse expected or actual XML
     */
    protected void testXmlRoundTrip() throws IOException, SAXException {
        for (String key : objectXmlMap.keySet()) {
            // Get expected object and XML
            T expectedObject   = objectXmlMap.get(key).getObject();
            String expectedXml = objectXmlMap.get(key).getXml();
            // Convert XML to object
            T actualObject = unmarshal(expectedXml);
            logger.debug(actualObject);
            assertEquals(key, expectedObject, actualObject);
            // ... and back again
            String actualXml = marshal(actualObject);
            logger.debug(key + ":\n" + actualXml);
            Diff diff = new Diff(expectedXml, actualXml);
            // Order of attributes and elements is not important
            diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
            assertTrue(key + ": " + diff.toString() + "\nExpected:\n" + expectedXml + "\n\nActual:\n" + actualXml, true);
        }
    }

    protected String marshal(T object) throws IOException  {
        Writer writer = new StringWriter();
        marshaller.marshal(object, new StreamResult(writer));
        return writer.toString();
    }

    @SuppressWarnings("unchecked")
    protected T unmarshal(String xml) throws IOException  {
        return (T) unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
    }

//        Tried following in testUnmarshal() but will not work because no public setter methods in model!
//        T expectedCopy = (T) SerializationUtils.clone(expected);
//        for (Method m : expectedCopy.getClass().getMethods())   {
//            for (Annotation a : m.getDeclaredAnnotations()) {
//                if (a instanceof XmlTransient)    {
//                    if (m.getName().startsWith("get"))  {
//                        // Find corresponding "set" method
//                        for (Method mm : expectedCopy.getClass().getMethods())   {
//                            if (mm.getName().startsWith("set")) {
//                                if (m.getName().substring(3).equals(mm.getName().substring(3))) {
//                                    // Call "set<Method.Name>(null)"
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
  

}