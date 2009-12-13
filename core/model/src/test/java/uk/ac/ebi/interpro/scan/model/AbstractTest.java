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
import static org.junit.Assert.assertNotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.annotation.Resource;
import java.io.*;
import java.util.Map;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

/**
 * Utility class for running XmlUnit tests.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     org.custommonkey.xmlunit.XMLUnit
 */
abstract class AbstractTest<T> {

    private static final Log LOGGER = LogFactory.getLog(AbstractTest.class);

    private static final Long LONG_ZERO = 0L;

    @Resource
    private Marshaller marshaller;

    @Resource
    private Unmarshaller unmarshaller;

    @Resource
    private org.springframework.core.io.Resource schema;

    @Resource
    private Map<String, ObjectXmlPair<T>> objectXmlMap;

    @Resource
    private GenericDAO<T, Long> dao;

    static {
        // Ignore comments and whitespace when comparing XML
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    // TODO: Add method that tests non-XML objects (so can test with @XmlTransient data)

    private void initJpa()   {
        assertNotNull(dao);
        dao.deleteAll();
        assertEquals(LONG_ZERO, dao.count());
    }

    interface ObjectRetriever<P>{

        P getObjectByPrimaryKey(GenericDAO<P, Long> dao, Long primaryKey);

        Long getPrimaryKey(P persistable);
    }

    protected void testJpaXmlObjects(ObjectRetriever<T> retriever){
        initJpa();
        for (String key : objectXmlMap.keySet()) {
            // Get expected object
            T expectedObject   = objectXmlMap.get(key).getObject();
            // Persist
            dao.insert(expectedObject);
            assertEquals(key, 1, dao.retrieveAll().size());
            // Retrieve
            Long pk = retriever.getPrimaryKey(expectedObject);
            T actualObject = retriever.getObjectByPrimaryKey(dao, pk);
            assertEquals(key, expectedObject, actualObject);
            // Delete
            LOGGER.debug("Deleting: " + actualObject);
            dao.delete(actualObject);
            assertEquals(key, 0, dao.retrieveAll().size());
        }
    }

    protected void testSupportsMarshalling(Class c) {
        assertTrue(marshaller.supports(c));
        assertTrue(unmarshaller.supports(c));
    }

    /**
     * Performs XML round-trip using {@link java.util.Map} of {@link uk.ac.ebi.interpro.scan.model.ObjectXmlPair}.
     *
     * @throws java.io.IOException   if problem marshalling or unmarshalling
     * @throws org.xml.sax.SAXException  if cannot parse expected or actual XML
     */
    protected void testXmlRoundTrip() throws IOException, SAXException {
        for (String key : objectXmlMap.keySet()) {
            // Get expected object and XML
            T expectedObject   = objectXmlMap.get(key).getObject();
            String expectedXml = objectXmlMap.get(key).getXml();
            LOGGER.debug(key + " (expected object XML):\n" + marshal(expectedObject));
            // Convert XML to object
            T actualObject = unmarshal(expectedXml);
            LOGGER.debug(actualObject);
            assertEquals(key, expectedObject, actualObject);
            // ... and back again
            String actualXml = marshal(actualObject);
            LOGGER.debug(key + " (actual object XML):\n" + actualXml);
            assertXmlEquals(key, expectedXml, actualXml);
            // Validate against XML schema
            validate(actualXml);
        }
    }

    protected void assertXmlEquals(String key, String expectedXml, String actualXml) throws IOException, SAXException {
        Diff diff = new Diff(expectedXml, actualXml);
        // Order of attributes and elements is not important
        diff.overrideElementQualifier(new RecursiveElementNameAndAttributeQualifier());
        String message = key + ": " + diff.toString() + "\nExpected:\n" + expectedXml + "\n\nActual:\n" + actualXml;
        assertTrue(message, diff.similar());
    }

    protected String marshal(T object) throws IOException  {
        Writer writer = new StringWriter();
        marshaller.marshal(object, new StreamResult(writer));
        return writer.toString();
    }

    protected T unmarshal(String xml) throws IOException  {
        // There's no guarantee that this cast is correct, but it's test code so let's not clutter the compiler output
        @SuppressWarnings("unchecked") T result = (T) unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
        return result;
    }

    private void validate(String xml) throws SAXException, IOException {
        Validator v = new Validator(xml);
        v.useXMLSchema(true);
        v.setJAXP12SchemaSource(schema.getInputStream());
        v.assertIsValid();
    }

    /**
     * Tests two elements for tag name and attribute name comparability, ignoring the order of elements and the order
     * of attributes
     */
    private static final class RecursiveElementNameAndAttributeQualifier extends ElementNameAndAttributeQualifier {

        private final ElementQualifier recursiveElementQualifier = new RecursiveElementNameAndTextQualifier();

        /**
         * Determine whether two elements qualify for further Difference comparison.
         * 
         * @param  control an Element from the control XML NodeList
         * @param  test    an Element from the test XML NodeList
         * @return true    if the elements are comparable, false otherwise
         */
        @Override public boolean qualifyForComparison(Element control, Element test) {
            // TODO: This should ensure the order of attributes and elements is not important, BUT...
            // TODO: sometimes it does not work. Perhaps best solution is to order elements in normalised form
            // TODO: using XSLT before using Diff()
            boolean isElementsComparable = recursiveElementQualifier.qualifyForComparison(control, test);
            boolean isAttributesComparable = super.areAttributesComparable(control, test);
            return (isElementsComparable && isAttributesComparable);
        }

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
