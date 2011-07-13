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

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link uk.ac.ebi.interpro.scan.model.PathwayXref.PathwayDatabase}
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 */
public class PathwayXrefTest {

    private static final Log LOGGER = LogFactory.getLog(PathwayXrefTest.class);

    @Test
    public void testParseDatabaseCode() {
        //Lower case characters
        PathwayXref.PathwayDatabase actual = PathwayXref.PathwayDatabase.parseDatabaseCode('t');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.META_CYC.getDatabaseName(), actual.getDatabaseName());
        assertEquals(PathwayXref.PathwayDatabase.META_CYC.getDatabaseCode(), actual.getDatabaseCode());
        assertEquals(PathwayXref.PathwayDatabase.META_CYC.toString(), actual.toString());
        assertEquals(PathwayXref.PathwayDatabase.META_CYC, actual);

        actual = PathwayXref.PathwayDatabase.parseDatabaseCode('w');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.UNI_PATHWAY.getDatabaseName(), actual.getDatabaseName());
        assertEquals(PathwayXref.PathwayDatabase.UNI_PATHWAY.getDatabaseCode(), actual.getDatabaseCode());
        assertEquals(PathwayXref.PathwayDatabase.UNI_PATHWAY.toString(), actual.toString());
        assertEquals(PathwayXref.PathwayDatabase.UNI_PATHWAY, actual);

        actual = PathwayXref.PathwayDatabase.parseDatabaseCode('k');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.KEGG.getDatabaseName(), actual.getDatabaseName());
        assertEquals(PathwayXref.PathwayDatabase.KEGG.getDatabaseCode(), actual.getDatabaseCode());
        assertEquals(PathwayXref.PathwayDatabase.KEGG.toString(), actual.toString());
        assertEquals(PathwayXref.PathwayDatabase.KEGG, actual);

        actual = PathwayXref.PathwayDatabase.parseDatabaseCode('r');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.REACTOME.getDatabaseName(), actual.getDatabaseName());
        assertEquals(PathwayXref.PathwayDatabase.REACTOME.getDatabaseCode(), actual.getDatabaseCode());
        assertEquals(PathwayXref.PathwayDatabase.REACTOME.toString(), actual.toString());
        assertEquals(PathwayXref.PathwayDatabase.REACTOME, actual);

        //Upper case characters
        actual = PathwayXref.PathwayDatabase.parseDatabaseCode('T');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.META_CYC.getDatabaseName(), actual.getDatabaseName());

        actual = PathwayXref.PathwayDatabase.parseDatabaseCode('W');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.UNI_PATHWAY.getDatabaseName(), actual.getDatabaseName());

        actual = PathwayXref.PathwayDatabase.parseDatabaseCode('K');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.KEGG.getDatabaseName(), actual.getDatabaseName());

        actual = PathwayXref.PathwayDatabase.parseDatabaseCode('R');
        assertNotNull(actual);
        assertEquals(PathwayXref.PathwayDatabase.REACTOME.getDatabaseName(), actual.getDatabaseName());

        //Test special cases
        try {
            actual = PathwayXref.PathwayDatabase.parseDatabaseCode('?');
            assertNull("Unexpected behaviour. Method call should throw an exception!", actual);
        } catch (IllegalArgumentException e) {
            assertNull("That is expected behaviour.", null);
        }
    }
}