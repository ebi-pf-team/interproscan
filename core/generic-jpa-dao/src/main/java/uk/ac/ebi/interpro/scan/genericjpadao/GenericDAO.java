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

import java.io.Serializable;
import java.util.List;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 02-Jul-2009
 * Time: 11:17:23
 *
 * @author Phil Jones, EMBL-EBI
 *
 * Based upon the pattern described in
 * http://www.ibm.com/developerworks/java/library/j-genericdao.html
 * by Per Mellqvist (per@mellqvist.name), System architect, Freelance
 */
public interface GenericDAO <T, PK extends Serializable> {


    /**
     * Insert a new Model instance.
     * @param newInstance being a new instance to persist.
     */
    void insert(T newInstance);
    /**
     * Update the instance into the database
     *
     * @param modifiedInstance being an attached or unattached, persisted object that has been modified.
     */
    void update(T modifiedInstance);

    /** Retrieve an object that was previously persisted to the database using
     *  the indicated id as primary key
     * @param id being the primary key value of the required object.
     * @return a single instance of the object with the specified primary key,
     * or null if it does not exist.
     */
    T read(PK id);

    /**
     * Remove an object from persistent storage in the database
     * @param persistentObject being the (attached or unattached) object to be deleted.
     */
    void delete(T persistentObject);

    /**
     * Returns a count of all instances of the type.  Note that select count(object) JSQL
     * returns a Long object.
     * @return a count of all instances of the type.
     */
    Long count();

    /**
     * Returns a List of all the instances of T in the database.
     * @return a List of all the instances of T in the database.
     */
    List<T> retrieveAll();

    /**
     * Deletes all instances of class T in the database.
     * @return the number of rows affected by this operation.
     */
    int deleteAll();

    /**
     * Returns the highest primary key value for the Model class.
     * @return the highest primary key value for the Model class.
     */
    Long getMaximumPrimaryKey();
}
