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

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * A generic DAO implementation that can be used with any model class.
 * Just need to inject the Model class name to use.
 * <p/>
 * Provides the basic CRUD methods.
 * <p/>
 * T is the model type (e.g. Protein, Model, Signature etc.)
 * PK is the type of the primary key (normally {@link java.lang.Long})
 * <p/>
 * Based on the pattern described in
 * <a href ="http://www.ibm.com/developerworks/java/library/j-genericdao.html">Don't repeat the DAO!</a>
 * by Per Mellqvist (per@mellqvist.name) in IBM Developer Works Technical Library, 12 May 2006.
 *
 * @author Phil Jones, EMBL-EBI
 * @author Antony Quinn
 */

public class GenericDAOImpl<T, PK extends Serializable>
        implements GenericDAO<T, PK> {

    private static final Logger LOGGER = Logger.getLogger(GenericDAOImpl.class.getName());

    protected EntityManager entityManager;

    /**
     * The Class of the concrete object.
     */
    protected Class<T> modelClass;

    /**
     * The unqualified class name of T, used to build JSQL queries.
     */
    protected String unqualifiedModelClassName;

    /**
     * Required by Spring.
     */
    private GenericDAOImpl() {
    }

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     *
     * @param modelClass the model that the DOA instance handles.
     */
    public GenericDAOImpl(Class<T> modelClass) {
        this.modelClass = modelClass;
        this.unqualifiedModelClassName = modelClass.getSimpleName();
    }

    @PersistenceContext
    protected void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Insert a new Model instance.
     *
     * @param newInstance being a new instance to persist.
     * @return the inserted Instance.  This MAY NOT be the same object as
     *         has been passed in, for sub-classes that check for the pre-existence of the object
     *         in the database.
     */
    // TODO: change the propagation. Deliberately broken by David
    @Transactional
    public T insert(T newInstance) {
        if (entityManager.contains(newInstance)) {
            LOGGER.debug("The Entity that you are attempting to store has already been persisted.");
            return newInstance;
        }
        entityManager.persist(newInstance);
        return newInstance;
    }

    /**
     * Insert a List of new Model instances.
     *
     * @param newInstances being a List of instances to persist.
     * @return the Collection of persisted instances.
     *         This MAY NOT contain the same objects as
     *         have been passed in, for sub-classes that check for the pre-existence of the object
     *         in the database.
     */
    @Transactional
    public Collection<T> insert(Collection<T> newInstances) {
        for (T newInstance : newInstances) {
            if (entityManager.contains(newInstance)) {
                LOGGER.debug("The Entity that you are attempting to store has already been persisted.");
                continue;
            }
//            LOGGER.debug(newInstance.toString());
            entityManager.persist(newInstance);
        }
        return newInstances;
    }

    /**
     * Update the instance into the database
     *
     * @param modifiedInstance being an attached or unattached, persisted object that has been modified.
     */
    @Transactional
    public void update(T modifiedInstance) {
        entityManager.merge(modifiedInstance);
    }

    /**
     * Retrieve an object that was previously persisted to the database using
     * the indicated id as primary key
     *
     * @param id being the primary key value of the required object.
     * @return a single instance of the object with the specified primary key,
     *         or null if it does not exist.
     */
    @Transactional(readOnly = true)
    public T read(PK id) {
        String queryString = String.format("select o from %s o where o.id = :id", unqualifiedModelClassName);
        Query query = this.entityManager.createQuery(queryString);
        query.setParameter("id", id);

        // Originally this made use of query.getSingleResult
        // however this method throws an Exception if there is no
        // matching object, which seems like overkill.  Modified to return
        // null if there is no matching object.

        @SuppressWarnings("unchecked") List<T> results = query.getResultList();
        if (results.size() == 0) {
            return null;
        } else return results.get(0);
    }

    @Transactional(readOnly = true)
    public T readSpecific(String id) {
        String queryString = String.format("select o from %s o where o.modelId = :modelId", unqualifiedModelClassName);
        Query query = this.entityManager.createQuery(queryString);
        query.setParameter("modelId", id);

        // Originally this made use of query.getSingleResult
        // however this method throws an Exception if there is no
        // matching object, which seems like overkill.  Modified to return
        // null if there is no matching object.

        @SuppressWarnings("unchecked") List<T> results = query.getResultList();
        if (results.size() == 0) {
            return null;
        } else return results.get(0);
    }

    /**
     * Retrieve an object that was previously persisted to the database using
     * the indicated id as primary key and go deep on the fields listed.
     * <p/>
     * TODO - Could make use of reflection to determine if the field name passed in is accessible via a public getter.
     *
     * @param id         being the primary key value of the required object.
     * @param deepFields being the names of the fields to retrieve with the main object.
     * @return a single instance of the object with the specified primary key,
     *         or null if it does not exist, with the lazy objects initialised.
     */
    @Transactional(readOnly = true)
    public T readDeep(PK id, String... deepFields) {
        StringBuffer queryString = new StringBuffer("select o from ");
        queryString.append(unqualifiedModelClassName)
                .append(" o ");

        for (String field : deepFields) {
            queryString.append(" left outer join fetch o.")
                    .append(field);
        }

        queryString.append(" where o.id = :id");

        Query query = this.entityManager.createQuery(queryString.toString());
        query.setParameter("id", id);

        // Originally this made use of query.getSingleResult
        // however this method throws an Exception if there is no
        // matching object, which seems like overkill.  Modified to return
        // null if there is no matching object.

        @SuppressWarnings("unchecked") List<T> results = query.getResultList();
        if (results.size() == 0) {
            return null;
        } else return results.get(0);
    }

    /**
     * Remove an object from persistent storage in the database
     *
     * @param persistentObject being the (attached or unattached) object to be deleted.
     */
    @Transactional
    public void delete(T persistentObject) {
        if (!entityManager.contains(persistentObject)) {
            persistentObject = entityManager.merge(persistentObject);
        }
        entityManager.remove(persistentObject);
    }

    /**
     * Returns a count of all instances of the type.  Note that select count(object) JSQL
     * returns a Long object.
     *
     * @return a count of all instances of the type.
     */
    @Transactional(readOnly = true)
    public Long count() {
        String queryString = String.format("select count(o) from %s o", unqualifiedModelClassName);
        Query query = this.entityManager.createQuery(queryString);
        return (Long) query.getSingleResult();
    }

    /**
     * Returns a List of all the instances of T in the database.
     *
     * @return a List of all the instances of T in the database.
     */
    @Transactional(readOnly = true)
    public List<T> retrieveAll() {
        String queryString = String.format("select o from %s o", unqualifiedModelClassName);
        Query query = this.entityManager.createQuery(queryString);
        @SuppressWarnings("unchecked") List<T> results = query.getResultList();
        return results;
    }

    /**
     * Deletes all instances of class T in the database.
     *
     * @return the number of rows affected by this operation.
     */
    @Transactional
    public int deleteAll() {
//        String queryString = String.format("delete from %s", unqualifiedModelClassName);
//        Query query = this.entityManager.createQuery(queryString);
//        return query.executeUpdate();
        List<T> allEntities = retrieveAll();
        for (T entity : allEntities) {
            delete(entity);
        }
        return allEntities.size();
    }

    /**
     * Returns the highest primary key value for the Model class.
     *
     * @return the highest primary key value for the Model class.
     */
    @Transactional(readOnly = true)
    public Long getMaximumPrimaryKey() {
        String queryString = String.format("select max(id) from %s", unqualifiedModelClassName);
        Query query = entityManager.createQuery(queryString);
        return (Long) query.getSingleResult();
    }

    /**
     * Experimental - included to allow explicit flush following DAO transaction.
     */
    @Transactional
    public void flush() {
        entityManager.flush();
    }

}
