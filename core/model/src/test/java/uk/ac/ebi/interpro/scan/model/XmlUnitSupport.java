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
import static org.junit.Assert.assertSame;
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
import java.io.StringWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.StringReader;

/**
 * Utility class for running XmlUnit tests.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     XMLUnit
 */
final class XmlUnitSupport {

    private static final Log logger = LogFactory.getLog(XmlUnitSupport.class);

    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    private XmlUnitSupport() {
        this.marshaller = null;
        this.unmarshaller = null;
    }

    public XmlUnitSupport(Marshaller marshaller, Unmarshaller unmarshaller) {
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    public void init()  {
        // Ignore comments and whitespace when comparing XML
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    public String marshal(Object o) throws IOException  {
        Writer writer = new StringWriter();
        marshaller.marshal(o, new StreamResult(writer));
        String xml = writer.toString();
        logger.debug("\n" + xml);
        return xml;
    }        

    public Object unmarshal(String xml) throws IOException  {
        Object o = unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
        logger.debug(o);
        return o;
    }

    public void testSupportsMarshalling(Class c) {
        assertTrue(marshaller.supports(c));
        assertTrue(unmarshaller.supports(c));
    }

    public String testMarshal(String message, Object o, String expectedXml)
            throws IOException, SAXException {
        String actualXml = marshal(o);
        Diff diff = new Diff(expectedXml, actualXml);
        // Order of attributes and elements is not important
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        assertTrue(message + ": " + diff.toString() + "\nExpected:\n" + expectedXml + "\n\nActual:\n" + actualXml, true);
        return actualXml;
    }
    
    public Object testUnmarshal(String message, String xml, Object expected) throws IOException {
        Object actual = unmarshal(xml);
        assertEquals(message, expected, actual);
        return actual;
    }
  

}