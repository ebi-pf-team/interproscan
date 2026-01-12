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

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.kvstore.LevelDBStore;

import org.iq80.leveldb.DB;

import java.util.*;

/**
 * Interface that defines additional functionality for Protein Data Access.
 * <p/>
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 13:24:50
 *
 * @author Phil Jones, EMBL-EBI
 * @author Gift Nuka
 */
public interface ProteinDAO extends GenericKVDAO<Protein> {

    @Transactional
    void persist(final Map<String, Protein> keyToProteinMap);

    @Transactional
    void insert(String key, Protein protein);

    @Transactional
    void insertProteinNotInLookup(String key, Protein protein);

    @Transactional
    void persist(byte[] key, byte[] protein);

    @Transactional
    void persistProteinNotInLookup(byte[] key, byte[] protein);

    @Transactional(readOnly = true)
    Protein getProtein(String key);

    @Transactional(readOnly = true)
    Protein getProteinNotInLookup(String key);

    @Transactional(readOnly = true)
    List<Protein> getProteins() throws Exception;

    @Transactional(readOnly = true)
    List<Protein> getProteinsNotInLookup() throws Exception;

    @Transactional(readOnly = true)
    int getProteinsNotInLookupCount() throws Exception;

    @Transactional(readOnly = true)
    List<Protein> getProteins(long bottom, long top);

    @Transactional(readOnly = true)
    Map<String, Protein> getKeyToProteinMap() throws Exception;

    void setProteinIdsWithoutLookupHit(Map<Long, Protein> proteinIdsWithoutLookupHit);

    @Transactional
    Set<Protein> getProteinsWithoutLookupHit();

    List<Protein> getProteinsWithoutLookupHitBetweenIds(long bottom, long top);

    void checkKVDBStores();

    DB getLevelDBStore();

    void closeKVDBStores();

    /**
     * Retrieves a Protein object by primary key and also retrieves any associated cross references.
     *
     * @param id being the primary key of the required Protein.
     * @return The Protein, with cross references loaded. (ProteinXrefs are LAZY by default) or null if the
     *         primary key is not present in the database.
     */
    @Transactional(readOnly = true)
    Protein getProteinAndCrossReferencesByProteinId(Long id);

    /**
     * Retrieves a List of Proteins that are part of the TransactionSlice passed in as argument.
     * TODO - Consider this very carefully.  If the TransactionSlice includes all the proteins in the database, this will make a nasty mess.
     *
     * @return a List of Proteins that are part of the TransactionSlice passed in as argument.
     */
    @Transactional(readOnly = true)
    List<Protein> getProteinsBetweenIds(long bottom, long top);

    /**
     * Queries a list of proteins by the specified protein identifiers. For further information please have a look at
     * method with name getProteinsBetweenIds().
     * Note that if the input list is null or empty then an empty list of proteins shall be returned.
     *
     * @param proteinIds Set of protein identifiers.
     */
    @Transactional(readOnly = true)
    List<Protein> getProteinsByIds(Set<Long> proteinIds);

    @Transactional(readOnly = true)
    Protein getProteinById(Long proteinId);

    /**
     * Inserts new Proteins.
     * If there are Protein objects with the same MD5 / sequence in the database,
     * this method updates these proteins, rather than inserting the new ones.
     * <p/>
     * Note that this method inserts the new Protein objects AND and new ProteinXrefs
     * (possibly updating an existing Protein object if necessary with the new ProteinXref.)
     *
     * @param newProteins being a List of new Protein objects to insert
     * @return a new List<Protein> containing all of the inserted / updated Protein objects.
     *         (Allows the caller to retrieve the primary keys for the proteins).
     */
    @Transactional
    PersistedProteins insertNewProteins(Collection<Protein> newProteins);


    /**
     * Retrieve all proteins with their matches and cross references for a slice or proteins
     *
     * @param bottom
     * @param top    //     * @deprecated Doesn't actually work
     * @return
     */
    @Transactional(readOnly = true)
    List<Protein> getProteinsAndMatchesAndCrossReferencesBetweenIds(long bottom, long top);

    /**
     * Instances of this class are returned from the insert method above.
     */
    class PersistedProteins {

        private final Set<Protein> preExistingProteins = new HashSet<Protein>();

        private final Set<Protein> newProteins = new HashSet<Protein>();

        void addPreExistingProtein(Protein protein) {
            preExistingProteins.add(protein);
        }

        void addNewProtein(Protein protein) {
            newProteins.add(protein);
        }

        public Set<Protein> getPreExistingProteins() {
            return preExistingProteins;
        }

        public Set<Protein> getNewProteins() {
            return newProteins;
        }

        public Long updateBottomProteinId(Long bottomProteinId) {
            for (Protein newProtein : newProteins) {
                if (bottomProteinId == null || bottomProteinId > newProtein.getId()) {
                    bottomProteinId = newProtein.getId();
                }
            }
            return bottomProteinId;
        }

        public Long updateTopProteinId(Long topProteinId) {
            for (Protein newProtein : newProteins) {
                if (topProteinId == null || topProteinId < newProtein.getId()) {
                    topProteinId = newProtein.getId();
                }
            }
            return topProteinId;
        }
    }
}
