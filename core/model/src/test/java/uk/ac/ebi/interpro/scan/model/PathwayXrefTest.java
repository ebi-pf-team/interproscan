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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

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

    @Test
    public void testHashCodeAndEquals() {
        PathwayXref instance1 = new PathwayXref("id_1", "name_1", PathwayXref.PathwayDatabase.REACTOME.getDatabaseName());
        PathwayXref instance2 = new PathwayXref("id_2", "name_2", PathwayXref.PathwayDatabase.KEGG.getDatabaseName());
        Entry entry_instance2 = buildEntry("entry_2", "name_2", new Signature("sig_acc"));
        Entry entry_instance1 = buildEntry("entry_1", "name_1", new Signature("sig_acc"));
        instance1.addEntry(entry_instance1);
        instance2.addEntry(entry_instance2);

        assertNull("Id of instance 1 should be null!", instance1.getId());
        assertNull("Id of instance 2 should be null!", instance2.getId());
        assertNotSame("Identifier should be not the same!", instance1.getIdentifier(), instance2.getIdentifier());
        assertFalse(instance1.equals(instance2));
        //
        PathwayXref instance3 = new PathwayXref("id_1", "name_1", PathwayXref.PathwayDatabase.REACTOME.getDatabaseName());
        instance3.addEntry(entry_instance1);
        assertTrue(instance1.getEntries().equals(instance3.getEntries()));
        assertTrue(instance1.equals(instance3));
        //
        Set<PathwayXref> pathwayXrefSet = new HashSet<PathwayXref>();
        assertNotNull(pathwayXrefSet);
        assertEquals(0, pathwayXrefSet.size());
        //
        pathwayXrefSet.add(instance1);
        pathwayXrefSet.add(instance2);
        pathwayXrefSet.add(instance3);
        assertEquals(2, pathwayXrefSet.size());
        assertTrue(pathwayXrefSet.contains(instance1));
        assertTrue(pathwayXrefSet.contains(instance2));
        assertTrue(pathwayXrefSet.contains(instance3));
    }

    private Entry buildEntry(String entryAc, String name, Signature signature) {
        return new Entry.Builder(entryAc)
                .name(name)
                .type(EntryType.FAMILY)
                .description("description")
                .signature(signature)
                .build();
    }
}