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

package uk.ac.ebi.interpro.scan.genericjpadao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static junit.framework.TestCase.*;

/**
 * Developed using IntelliJ IDEA.
 * User: phil
 * Date: 19-Jun-2009
 * Time: 10:11:05
 *
 * @author Phil Jones, EMBL-EBI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class GenericDAOTest {

    /**
     * Using these static constants may see seem silly,
     * however the asserEquals method is not good at autoboxing / unboxing
     * so having these Long objects for comparison is handy...
     */
    private static final Long Long_0 = 0l;
    private static final Long Long_1 = 1l;
    private static final Long Long_3 = 3l;

    private static final String INITIAL_VALUE = "INITIAL_VALUE";
    private static final String MODIFIED_VALUE = "MODIFIED_VALUE";

    private static final String[] ARRAY_OF_VALUES = new String[]{"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};


    @Resource(name = "genericDAO")
    private ModelObjectDAO dao;

    public void setDao(ModelObjectDAO dao) {
        this.dao = dao;
    }

    /**
     * Utility method to empty the table between tests (should be called as the first
     * activity in each test.)
     * <p/>
     * Methods GenericDAO   .deleteAll()
     */
    private void emptyTable() {
        dao.deleteAll();
        assertEquals("There should be no proteins in the Protein table following a call to dao.deleteAll", Long_0, dao.count());
    }

    /**
     * This test exercises insertion and retrieval.
     * <p/>
     * Methods GenericDAO   .insert(T persistentObject)
     * .read(PK primaryKey)
     */
    @Test
    public void storeAndRetrieveObject() {
        emptyTable();
        ModelObject persistable = new ModelObject(INITIAL_VALUE);
        dao.insert(persistable);
        assertNotNull("The primary key for the persisted object should not be null", persistable.getId());
        Long primaryKey = persistable.getId();

        // Test
        ModelObject retrievedPersistable = dao.read(primaryKey);
        assertEquals("The value stored is not the same as the original value set.", persistable, retrievedPersistable);
    }

    /**
     * This test exercises insertion, update and retrieval.
     * <p/>
     * Methods GenericDAO   .insert(T persistentObject)
     * .update(T persistentObject)
     * .read(PK primaryKey)
     */
    @Test
    public void modifyAndRetrieveObject() {
        emptyTable();
        ModelObject persistable = new ModelObject(INITIAL_VALUE);
        dao.insert(persistable);
        ModelObject retrieved = dao.read(persistable.getId());
        assertEquals("The initially set value is not as expected.", INITIAL_VALUE, retrieved.getTestFieldOne());
        retrieved.setTestFieldOne(MODIFIED_VALUE);
        dao.update(retrieved);
        ModelObject retrievedAfterMod = dao.read(persistable.getId());
        assertEquals("The modified value is not as expected following retrieval from the database.", MODIFIED_VALUE, retrievedAfterMod.getTestFieldOne());
        assertEquals("There should only be one record in the table after these changes.", Long_1, dao.count());
    }

    /**
     * Exercises GenericDAO .count()
     * .read(PK primaryKey)
     * .retrieveAll()
     * .getMaximumPrimaryKey()
     */
    @Test
    public void testCountAndMaximumPrimaryKey() {
        emptyTable();
        Long maxPrimaryKey = Long.MIN_VALUE;
        for (String value : ARRAY_OF_VALUES) {
            ModelObject persistable = new ModelObject(value);
            dao.insert(persistable);
            if (persistable.getId() > maxPrimaryKey) {
                maxPrimaryKey = persistable.getId();
            }
        }
        assertEquals("The maxium primary key reported is not as expected", maxPrimaryKey, dao.getMaximumPrimaryKey());
        assertEquals("The number of stored objects is not as expected", new Long(ARRAY_OF_VALUES.length), dao.count());
        List<ModelObject> retrievedObjects = dao.retrieveAll();
        assertEquals("The number of retrieved objects does not equal the number stored.", ARRAY_OF_VALUES.length, retrievedObjects.size());
        for (ModelObject retrieved : retrievedObjects) {
            assertNotNull("The List of retrieved objects should not contain any null objects", retrieved);
        }
    }

    /**
     * Attempts to insert three objects independently, wrapped in an outer transaction.
     * The third insert will fail (breaks not-null constraint) which should cause
     * then entire transaction to roll back.
     * <p/>
     * Tested in the following query.
     */
    @Test
    public void testNestedTransactionRollback() {
        emptyTable();
        assertEquals("The number of stored objects is not as expected", Long_0, dao.count());
        boolean exceptionThrown = false;
        try {
            dao.nestedTransaction(true);
        }
        catch (Exception e) {    // This should be thrown
            exceptionThrown = true;
        }
        assertTrue("An Exception should have been thrown by the nestedTransaction method, which attempts to insert a null value in a non-null column.", exceptionThrown);
        // Now check that there are no objects in the database.
        assertEquals("There should be no objects in the database following a rollback.", Long_0, dao.count());
    }

    /**
     * Attempts to insert three objects independently, wrapped in an outer transaction.
     * The third insert will fail (breaks not-null constraint) which should cause
     * then entire transaction to roll back.
     * <p/>
     * Tested in the following query.
     */
    @Test
    public void testNestedTransactionCommit() {
        emptyTable();
        assertEquals("The number of stored objects is not as expected", Long_0, dao.count());
        boolean exceptionThrown = false;
        try {
            dao.nestedTransaction(false);
        }
        catch (Exception e) {    // This should NOT be thrown
            exceptionThrown = true;
        }
        assertFalse("An Exception should not have been thrown by the nestedTransaction method.", exceptionThrown);
        // Now check that there are 3 objects in the database.
        assertEquals("There should be 3 objects in the database following a commit.", Long_3, dao.count());
    }

    /**
     * Test the readDeep method that allows retrieval of FetchType.LAZY related objects.
     */
    @Test
    public void testReadDeep() {
        final int RELATED_OBJECT_COUNT = 10;

        emptyTable();
        ModelObject persistable = new ModelObject(INITIAL_VALUE);
        for (int i = 0; i < RELATED_OBJECT_COUNT; i++) {
            new RelatedModelObject(persistable, "value_" + i);
        }
        assertNotNull(persistable.getRelatedObjects());
        assertEquals(RELATED_OBJECT_COUNT, persistable.getRelatedObjects().size());

        dao.insert(persistable);
        Long pk = persistable.getId();

        // Should have persisted a ModelObject with 10 RelatedModelObjects. Try to retrieve them.
        ModelObject retrieved = dao.readDeep(pk, "relatedObjects");

        assertNotNull(retrieved);
        assertNotNull(retrieved.getRelatedObjects());
        assertEquals(RELATED_OBJECT_COUNT, retrieved.getRelatedObjects().size());
    }

    /**
     * Exercises GenericDAO .delete(T persistentObject)
     * .count()
     * .read(PK primaryKey)
     */
    @Test
    public void testDelete() {
        emptyTable();
        ModelObject toDelete = null;
        for (String value : ARRAY_OF_VALUES) {
            ModelObject persistable = new ModelObject(value);
            dao.insert(persistable);
            if ("three".equals(persistable.getTestFieldOne())) {
                toDelete = persistable;
            }
        }
        if (toDelete != null) {
            Long primaryKeyOfDeletable = toDelete.getId();

            assertEquals("All of the objects should be present prior to deleteion.", new Long(ARRAY_OF_VALUES.length), dao.count());
            // Now delete one of the objects.
            dao.delete(toDelete);
            assertEquals("One of the inserted objects has been removed, so the count should be decremented.", new Long(ARRAY_OF_VALUES.length - 1), dao.count());

            ModelObject shouldBeNull = dao.read(primaryKeyOfDeletable);
            assertNull("The primary key of the deleted object should return null.", shouldBeNull);
        } else {
            fail("There should be an object to delete, however the expected reference is null");
        }
    }
}
