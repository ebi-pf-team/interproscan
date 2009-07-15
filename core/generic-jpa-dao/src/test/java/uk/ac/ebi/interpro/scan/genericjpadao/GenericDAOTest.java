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

import static junit.framework.TestCase.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Developed using IntelliJ IDEA.
 * User: phil
 * Date: 19-Jun-2009
 * Time: 10:11:05
 *
 * @author Phil Jones, EMBL-EBI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-DAOTest-config.xml"})
public class GenericDAOTest {

    private static final Long LONG_ZERO = 0l;

    @Resource (name= "genericDAO")
    private GenericDAO<ModelObject, Long> dao;

    public void setDao(GenericDAO<ModelObject, Long> dao) {
        this.dao = dao;
    }

    private void emptyTable(){
        dao.deleteAll();
        assertEquals("There should be no proteins in the Protein table following a call to dao.deleteAll", LONG_ZERO, dao.count());
    }

    /**
     * This test exercises insertion and retrieval.
     */
    @Test
    public void storeAndRetrieveObject(){
        emptyTable();
        ModelObject persistable= new ModelObject("Test Value");
        dao.insert(persistable);
        assertNotNull("The primary key for the persisted object should not be null", persistable.getId());
        Long primaryKey = persistable.getId();

        // Test
        ModelObject retrievedPersistable = dao.read(primaryKey);
        assertEquals("The value stored is not the same as the original value set.", persistable, retrievedPersistable);
    }

}
