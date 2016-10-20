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

package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import javax.persistence.Query;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Implements additional functionality for Protein Data Access.
 * <p/>
 * User: pjones
 * Date: 03-Jul-2009
 * Time: 11:21:55
 *
 * @author Phil Jones, EMBL-EBI
 */
public class ProteinDAOImpl extends GenericDAOImpl<Protein, Long> implements ProteinDAO {

    private static final Logger LOGGER = Logger.getLogger(ProteinDAOImpl.class.getName());

    /**
     * For the method  getProteinsAndMatchesAndCrossReferencesBetweenIds below,
     * this List contains the unqualified (simple) class names of all the concrete
     * sub-classes of the Match class, allowing them to be queried in turn.
     */
    private static final List<String> CONCRETE_MATCH_CLASSES = new ArrayList<String>();

    static {
        final Reflections reflections = new Reflections("uk.ac.ebi.interpro.scan.model");
        final Set<Class<? extends Match>> allClasses = reflections.getSubTypesOf(Match.class);
        for (Class clazz : allClasses) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {  // Concrete only.
                CONCRETE_MATCH_CLASSES.add(clazz.getSimpleName());
            }
        }
    }

    /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public ProteinDAOImpl() {
        super(Protein.class);
    }

    /**
     * Retrieves a Protein object by primary key and also retrieves any associated cross references.
     *
     * @param id being the primary key of the required Protein.
     * @return The Protein, with cross references loaded. (Xrefs are LAZY by default) or null if the
     *         primary key is not present in the database.
     */
    @Transactional(readOnly = true)
    public Protein getProteinAndCrossReferencesByProteinId(Long id) {
        Query query = entityManager.createQuery("select p from Protein p left outer join fetch p.crossReferences where p.id = :id");
        query.setParameter("id", id);
        return (Protein) query.getSingleResult();
    }

    /**
     * Retrieves all Proteins, cross references and matches for a range
     *
     * @param bottom range lower bound (included)
     * @param top    range upper bound (included)
     * @return The Protein, with matches loaded. (matches are LAZY by default) or null if the
     *         primary key is not present in the database.
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Protein> getProteinsAndMatchesAndCrossReferencesBetweenIds(long bottom, long top) {
        Query query = entityManager.createQuery("select distinct p from Protein p " +
                "left outer join fetch p.matches " +
                "left outer join fetch p.crossReferences");
        //        "left outer join fetch p.crossReferences where p.id >= :bottom and p.id <= :top");
        //ignore the ranges
        //query.setParameter("bottom", bottom);
        //query.setParameter("top", top);
        List<Protein> matchingProteins = (List<Protein>) query.getResultList();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Querying proteins with IDs in range: " + bottom + " to " + top);
            LOGGER.trace("Matching protein count: " + matchingProteins.size());
            for (Protein protein : matchingProteins) {
                LOGGER.trace("Protein ID: " + protein.getId() + " MD5: " + protein.getMd5());
                LOGGER.trace("Has " + protein.getMatches().size() + " matches");
                for (ProteinXref xref : protein.getCrossReferences()) {
                    LOGGER.trace("Xref: " + xref.getIdentifier());
                }
            }
        }
        return matchingProteins;
    }


    /**
     * Retrieves a List of Proteins that are part of the TransactionSlice passed in as argument.
     * TODO - Consider this very carefully.  If the TransactionSlice includes all the proteins in the database, this will make a nasty mess.
     *
     * @return a List of Proteins that are part of the TransactionSlice passed in as argument.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Protein> getProteinsBetweenIds(long bottom, long top) {
        Query query = entityManager.createQuery("select p from Protein p where p.id >= :bottom and p.id <= :top");
        query.setParameter("bottom", bottom);
        query.setParameter("top", top);
        return (List<Protein>) query.getResultList();
    }

    @Transactional(readOnly = true)
    public List<Protein> getProteinsByIds(Set<Long> proteinIds) {
        List<Protein> proteins = new ArrayList<Protein>();
        if (proteinIds != null && proteinIds.size() > 0) {
            Query query = entityManager.createQuery("select p from Protein p where p.id in(:ids)");
            query.setParameter("ids", proteinIds);
            proteins = query.getResultList();
        }
        return proteins;
    }

    /**
     * Insert a new Protein instance.
     *
     * @param newInstance being a new instance to persist.
     */
    @Transactional
    @Override
    public Protein insert(Protein newInstance) {
        Collection<Protein> proteinList = insert(Collections.singleton(newInstance));
        assert proteinList != null;
        assert proteinList.size() == 1;
        entityManager.flush();
        return proteinList.iterator().next();
    }

    /**
     * Insert a List of new Protein instances.
     *
     * @param newInstances being a List of instances to persist.
     * @return the Collection of persisted instances.
     *         This MAY NOT contain the same objects as
     *         have been passed in, for sub-classes that check for the pre-existence of the object
     *         in the database.
     */
    @Override
    @Transactional
    public Collection<Protein> insert(Collection<Protein> newInstances) {
        final PersistedProteins persistedProteins = insertNewProteins(newInstances);
        final Collection<Protein> allProteins = new ArrayList<Protein>(persistedProteins.getNewProteins());
        allProteins.addAll(persistedProteins.getPreExistingProteins());
        entityManager.flush();
        return allProteins;
    }

    /**
     * Inserts new Proteins.
     * If there are Protein objects with the same MD5 / sequence in the database,
     * this method updates these proteins, rather than inserting the new ones.
     * <p/>
     * Note that this method inserts the new Protein objects AND and new Xrefs
     * (possibly updating an existing Protein object if necessary with the new Xref.)
     *
     * @param newProteins being a List of new Protein objects to insert
     * @return a new List<Protein> containing all of the inserted / updated Protein objects.
     *         (Allows the caller to retrieve the primary keys for the proteins).
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public PersistedProteins insertNewProteins(Collection<Protein> newProteins) {
        PersistedProteins persistentProteins = new PersistedProteins();
        if (newProteins.size() > 0) {
            // Create a List of MD5s (just as Strings) to query the database with
            final List<String> newMd5s = new ArrayList<String>(newProteins.size());
            for (Protein newProtein : newProteins) {
                newMd5s.add(newProtein.getMd5());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("MD5 of new protein: " + newProtein.getMd5());
                }
            }
            // Retrieve any proteins AND associated xrefs that have the same MD5 as one of the 'new' proteins
            // being inserted and place in a Map of MD5 to Protein object.
            final Map<String, Protein> md5ToExistingProtein = new HashMap<String, Protein>();
            final Query query = entityManager.createQuery("select p from Protein p left outer join fetch p.crossReferences where p.md5 in (:md5)");
            query.setParameter("md5", newMd5s);
            for (Protein existingProtein : (List<Protein>) query.getResultList()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found 1 existing protein with MD5: " + existingProtein.getMd5());
                }
                md5ToExistingProtein.put(existingProtein.getMd5(), existingProtein);
            }

            // Now have the List of 'new' proteins, and a list of existing proteins that match
            // them. Insert / update proteins as appropriate.
            for (Protein candidate : newProteins) {

                // PROTEIN ALREADY EXISTS in the DB. - update cross references and save.
                if (md5ToExistingProtein.keySet().contains(candidate.getMd5())) {
                    // This protein is already in the database - add any new Xrefs and update.
                    Protein existingProtein = md5ToExistingProtein.get(candidate.getMd5());
                    boolean updateRequired = false;
                    if (candidate.getCrossReferences() != null) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Protein TO BE STORED has xrefs:");
                        }
                        for (ProteinXref xref : candidate.getCrossReferences()) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(xref.getIdentifier());
                            }
                            // Add any NEW cross references.
                            if (!existingProtein.getCrossReferences().contains(xref)) {
                                if (LOGGER.isTraceEnabled()) {
                                    LOGGER.trace("Adding " + xref.getIdentifier() + " and setting updateRequired = true");
                                }
                                existingProtein.addCrossReference(xref);
                                updateRequired = true;
                            }
                        }
                    }
                    if (updateRequired) {
                        // PROTEIN is NOT new, but CHANGED (new Xrefs)
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Merging protein with new Xrefs: " + existingProtein.getMd5());
                        }
                        entityManager.merge(existingProtein);
                    }
                    persistentProteins.addPreExistingProtein(existingProtein);
                }
                // PROTEIN IS NEW - save it.
                else {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Saving new protein: " + candidate.getMd5());
                    }
                    entityManager.persist(candidate);
                    persistentProteins.addNewProtein(candidate);
                    // Check for this new protein next time through the loop, just in case the new source of
                    // proteins is redundant (e.g. a FASTA file with sequences repeated).
                    md5ToExistingProtein.put(candidate.getMd5(), candidate);
                }
            }
        }
        // Finally return all the persisted Protein objects (new or existing)
        entityManager.flush();
        return persistentProteins;
    }
}
