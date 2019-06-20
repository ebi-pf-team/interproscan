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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.*;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.*;
import java.util.Map;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

/**
 * Utility class for running XmlUnit tests.
 *
 * @author  Antony Quinn
 * @author Gift Nuka
 * @version $Id$
 * @since   1.0
 * @see     org.custommonkey.xmlunit.XMLUnit
 */
abstract class AbstractTest<T> extends AbstractXmlTest<T> {

    private static final Log LOGGER = LogFactory.getLog(AbstractTest.class);

    private static final Long LONG_ZERO = 0L;

    @Resource
    private Map<String, ObjectXmlPair<T>> objectXmlMap;

    @Resource
    private GenericDAO<T, Long> dao;

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
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        initJpa();
        for (String key : objectXmlMap.keySet()) {
            // Get expected object
            T expectedObject   = objectXmlMap.get(key).getObject();
            // Persist
            if (isDebugEnabled) {
                LOGGER.debug("Inserting: " + expectedObject);
            }
            dao.insert(expectedObject);
            assertEquals(1, dao.retrieveAll().size(), key);
            // Retrieve
            Long pk = retriever.getPrimaryKey(expectedObject);
            if (isDebugEnabled) {
                LOGGER.debug("Retrieving: " + pk);
            }
            T actualObject = retriever.getObjectByPrimaryKey(dao, pk);
            assertEquals(expectedObject, actualObject, key);
            // Delete
            if (isDebugEnabled) {
                LOGGER.debug("Deleting: " + actualObject);
            }
            dao.delete(actualObject);
            assertEquals(0, dao.retrieveAll().size(), key);
        }
    }

    /**
     * Performs XML round-trip using {@link java.util.Map} of {@link uk.ac.ebi.interpro.scan.model.ObjectXmlPair}.
     *
     * @throws java.io.IOException      if problem marshalling or unmarshalling
     * @throws org.xml.sax.SAXException if cannot parse expected or actual XML
     */
    protected void testXmlRoundTrip() throws IOException, SAXException {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setNormalizeWhitespace(true);        
        for (String key : objectXmlMap.keySet()) {
            try {
                // Get expected object and XML
                T expectedObject   = objectXmlMap.get(key).getObject();
                String expectedXml = objectXmlMap.get(key).getXml();
                LOGGER.debug(key + " (expected object XML):\n" + marshal(expectedObject));
                // Convert XML to object
                T actualObject = unmarshal(expectedXml);
                LOGGER.debug(actualObject);
                assertEquals(expectedObject, actualObject, key);
                // ... and back again
                String actualXml = marshal(actualObject);
                LOGGER.debug(key + " (actual object XML):\n" + actualXml);
                assertXmlEquals(expectedXml, actualXml, key);
                // Validate against XML schema
                validate(actualXml);
            }
            catch (Exception e) {
                throw new RuntimeException(key + " error:", e);
            }
        }
    }

}
