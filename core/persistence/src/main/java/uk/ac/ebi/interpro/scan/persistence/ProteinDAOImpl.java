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
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;

import javax.persistence.Query;
import java.util.List;

/**
 * Implements additional functionality for Protein Data Access.
 *
 * User: pjones
 * Date: 03-Jul-2009
 * Time: 11:21:55
 *
 * @author Phil Jones, EMBL-EBI
 */
public class ProteinDAOImpl extends GenericDAOImpl<Protein, Long> implements ProteinDAO {

    /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public ProteinDAOImpl(){
        super(Protein.class);
    }

     /**
     * Retrieves a Protein object by primary key and also retrieves any associated cross references.
     * @param id being the primary key of the required Protein.
     * @return The Protein, with cross references loaded. (Xrefs are LAZY by default) or null if the
     * primary key is not present in the database.
     */
    @Transactional(readOnly = true)
    public Protein getProteinAndCrossReferencesById (Long id){
        Query query = entityManager.createQuery("select p from Protein p join fetch p.crossReferences where p.id = :id");
        query.setParameter("id", id);
        return (Protein) query.getSingleResult();
    }

    /**
     * Retrieves a List of Proteins that are part of the TransactionSlice passed in as argument.
     * TODO - Consider this very carefully.  If the TransactionSlice includes all the proteins in the database, this will make a nasty mess.
     * @param slice defining a Transaction.
     * @return a List of Proteins that are part of the TransactionSlice passed in as argument.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Protein> getProteinsInTransactionSlice(TransactionSlice slice) {
        Query query = entityManager.createQuery("select p from Protein p where p.id >= :bottom and p.id <= :top");
        query.setParameter("bottom", slice.getBottom());
        query.setParameter("top", slice.getTop());
        return (List<Protein>) query.getResultList();
    }
}
