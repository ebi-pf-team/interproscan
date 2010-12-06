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

import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

/**
 * Utility class for running XmlUnit tests.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @see     org.custommonkey.xmlunit.XMLUnit
 */
abstract class AbstractXmlTest<T> {

    @Resource
    private Marshaller marshaller;

    @Resource
    private Unmarshaller unmarshaller;

    @Resource
    private org.springframework.core.io.Resource schema;

    static {
        // Ignore comments and whitespace when comparing XML
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setNormalizeWhitespace(true);
    }

    // TODO: Add method that tests non-XML objects (so can test with @XmlTransient data)

    protected void testSupportsMarshalling(Class c) {
        assertTrue(marshaller.supports(c));
        assertTrue(unmarshaller.supports(c));
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
        return unmarshal(new StreamSource(new StringReader(xml)));
    }

    protected T unmarshal(Source source) throws IOException  {
        // There's no guarantee that this cast is correct, but it's test code so let's not clutter the compiler output
        @SuppressWarnings("unchecked") T result = (T) unmarshaller.unmarshal(source);
        return result;
    }

    protected void validate(String xml) throws SAXException, IOException {
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
//                                if (m.getName().substring(3).equivalent(mm.getName().substring(3))) {
//                                    // Call "set<Method.Name>(null)"
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

}