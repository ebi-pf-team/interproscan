package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.NucleotideSequence;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.persistence.Query;
import java.util.*;

/**
 * @author Phil Jones
 *         Date: 21/06/11
 *         Time: 16:43
 */
public class NucleotideSequenceDAOImpl extends GenericDAOImpl<NucleotideSequence, Long> implements NucleotideSequenceDAO {

    private static final Logger LOGGER = Logger.getLogger(NucleotideSequenceDAOImpl.class.getName());

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public NucleotideSequenceDAOImpl() {
        super(NucleotideSequence.class);
    }

    /**
     * SELECT * FROM NUCLEOTIDE_SEQUENCE s
     * inner join NUCLEOTIDE_SEQUENCE_XREF x
     * on s.ID=x.SEQUENCE_ID
     * where x.IDENTIFIER like '%AACH01000026.1%';
     *
     * @return
     */
    public NucleotideSequence retrieveByXrefIdentifier(String identifier) {
        final Query query =
                entityManager.createQuery(
                        "SELECT s FROM NucleotideSequence s INNER JOIN s.xrefs x " +
                                "WHERE x.identifier like :identifier");
        query.setParameter("identifier", '%' + identifier + '%');
        @SuppressWarnings("unchecked") List<NucleotideSequence> list = query.getResultList();
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Inserts new Sequences.
     * If there are NucleotideSequence objects with the same MD5 / sequence in the database,
     * this method updates these, rather than inserting the new ones.
     * <p/>
     * Note that this method inserts the new NucleotideSequence objects AND and new NucleotideSequenceXrefs
     * (possibly updating an existing NucleotideSequence object if necessary with the new NucleotideSequenceXref.)
     *
     * @param newSequences being a List of new NucleotideSequence objects to insert
     * @return a new PersistedNucleotideSequences containing all of the inserted / updated NucleotideSequence objects.
     *         (Allows the caller to retrieve the primary keys for the NucleotideSequences).
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public PersistedNucleotideSequences insertNewNucleotideSequences(Collection<NucleotideSequence> newSequences) {
        PersistedNucleotideSequences persistedNucleotideSequences = new PersistedNucleotideSequences();
        if (newSequences.size() > 0) {
            // Create a List of MD5s (just as Strings) to query the database with
            final List<String> newMd5s = new ArrayList<String>(newSequences.size());
            for (NucleotideSequence newSequence : newSequences) {
                newMd5s.add(newSequence.getMd5());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("MD5 of new nucleotide sequence: " + newSequence.getMd5());
                }
            }
            // Retrieve any proteins AND associated xrefs that have the same MD5 as one of the 'new' proteins
            // being inserted and place in a Map of MD5 to Protein object.
            final Map<String, NucleotideSequence> md5ToExistingSequence = new HashMap<String, NucleotideSequence>();
            final Query query = entityManager.createQuery("select n from NucleotideSequence n left outer join fetch n.xrefs where n.md5 in (:md5)");
            query.setParameter("md5", newMd5s);
            for (NucleotideSequence existingSequence : (List<NucleotideSequence>) query.getResultList()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found 1 existing nucleotide sequence with MD5: " + existingSequence.getMd5());
                }
                md5ToExistingSequence.put(existingSequence.getMd5(), existingSequence);
            }

            // Now have the List of 'new' Nucleotide sequence, and a list of existing Nucleotide sequence that match
            // them. Insert / update Nucleotide sequence as appropriate.
            for (NucleotideSequence candidate : newSequences) {

                // Nucleotide sequence ALREADY EXISTS in the DB. - update cross references and save.
                if (md5ToExistingSequence.keySet().contains(candidate.getMd5())) {
                    // This Nucleotide sequence is already in the database - add any new Xrefs and update.
                    NucleotideSequence existingSequence = md5ToExistingSequence.get(candidate.getMd5());
                    boolean updateRequired = false;
                    if (candidate.getCrossReferences() != null) {
                        for (NucleotideSequenceXref xref : candidate.getCrossReferences()) {
                            // Add any NEW cross references.
                            if (!existingSequence.getCrossReferences().contains(xref)) {
                                existingSequence.addCrossReference(xref);
                                updateRequired = true;
                            }
                        }
                    }
                    if (updateRequired) {
                        // Nucleotide sequence is NOT new, but CHANGED (new Xrefs)
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Merging nucleotide sequence with new Xrefs: " + existingSequence);
                        }
                        entityManager.merge(existingSequence);
                    }
                    persistedNucleotideSequences.addPreExistingSequence(existingSequence);
                }
                // Nucleotide sequence IS NEW - save it.
                else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Saving new nucleotide sequence: " + candidate);
                    }
                    entityManager.persist(candidate);
                    persistedNucleotideSequences.addNewSequence(candidate);
                    // Check for this new Nucleotide sequence next time through the loop, just in case the new source of
                    // Nucleotide sequences is redundant (e.g. a FASTA file with sequences repeated).
                    md5ToExistingSequence.put(candidate.getMd5(), candidate);
                }
            }
        }
        // Finally return all the persisted Nucleotide sequence objects (new or existing)
        entityManager.flush();
        return persistedNucleotideSequences;
    }
}
