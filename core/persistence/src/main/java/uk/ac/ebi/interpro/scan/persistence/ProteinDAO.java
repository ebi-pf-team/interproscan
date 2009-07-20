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
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import java.util.List;

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
    public Protein getProteinAndCrossReferencesById (Long id);

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
     * @param slice defining a Transaction.
     * @return a List of Proteins that are part of the TransactionSlice passed in as argument.
     */
    public List<Protein> getProteinsInTransactionSlice (TransactionSlice slice);

}
