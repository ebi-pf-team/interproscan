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

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interface that defines additional functionality for Protein Data Access.
 * 
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 13:24:50
 *
 * @author Phil Jones, EMBL-EBI
 */
public interface ProteinDAO extends GenericDAO<Protein, Long> {

    /**
     * Retrieves a Protein object by primary key and also retrieves any associated cross references.
     * @param id being the primary key of the required Protein.
     * @return The Protein, with cross references loaded. (Xrefs are LAZY by default) or null if the
     * primary key is not present in the database.
     */
    public Protein getProteinAndCrossReferencesByProteinId(Long id);

    /**
     * Retrieves a Protein object by primary key and also retrieves any associated matches.
     * @param id being the primary key of the required Protein.
     * @return The Protein, with matches loaded. (matches are LAZY by default) or null if the
     * primary key is not present in the database.
     */
    public Protein getProteinAndMatchesById (Long id);

    /**
     * Retrieves a List of Proteins that are part of the TransactionSlice passed in as argument.
     * TODO - Consider this very carefully.  If the TransactionSlice includes all the proteins in the database, this will make a nasty mess.
     * @return a List of Proteins that are part of the TransactionSlice passed in as argument.
     */
    public List<Protein> getProteinsBetweenIds(long bottom, long top);

    /**
     * Inserts new Proteins.
     * If there are Protein objects with the same MD5 / sequence in the database,
     * this method updates these proteins, rather than inserting the new ones.
     *
     * Note that this method inserts the new Protein objects AND and new Xrefs
     * (possibly updating an existing Protein object if necessary with the new Xref.)
     * @param newProteins being a List of new Protein objects to insert
     * @return a new List<Protein> containing all of the inserted / updated Protein objects.
     * (Allows the caller to retrieve the primary keys for the proteins).
     */
    public PersistedProteins insertNewProteins(Collection<Protein> newProteins);

    /**
     * Instances of this class are returned from the insert method above.
     */
    public class PersistedProteins {

        private final Set<Protein> preExistingProteins = new HashSet<Protein>();

        private final Set<Protein> newProteins = new HashSet<Protein>();

        void addPreExistingProtein(Protein protein) {
            preExistingProteins.add(protein);
        }

        void addNewProtein(Protein protein){
            newProteins.add(protein);
        }

        public Set<Protein> getPreExistingProteins() {
            return preExistingProteins;
        }

        public Set<Protein> getNewProteins() {
            return newProteins;
        }

        public Long updateBottomProteinId(Long bottomProteinId){
            for (Protein newProtein : newProteins){
                if (bottomProteinId == null || bottomProteinId > newProtein.getId()){
                    bottomProteinId = newProtein.getId();
                }
            }
            return bottomProteinId;
        }

        public Long updateTopProteinId(Long topProteinId){
            for (Protein newProtein : newProteins){
                if (topProteinId == null || topProteinId < newProtein.getId()){
                    topProteinId = newProtein.getId();
                }
            }
            return topProteinId;
        }

    }
}
