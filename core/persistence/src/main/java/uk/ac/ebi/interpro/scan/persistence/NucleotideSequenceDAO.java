package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.NucleotideSequence;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Phil Jones
 *         Date: 21/06/11
 *         Time: 16:42
 */
public interface NucleotideSequenceDAO extends GenericDAO<NucleotideSequence, Long> {

    /**
     * Retrieves nucleotide sequence by cross reference (more precisely by cross reference identifier).
     *
     * @return
     */
    public NucleotideSequence retrieveByXrefIdentifier(String identifier);

    /**
     * Retrieves nucleotide sequence by cross reference (more precisely by cross reference name).
     *
     * @return
     */
    public NucleotideSequence retrieveByXrefName(String name);


    /**
     * Inserts new Sequences.
     * If there are NucleotideSequence objects with the same MD5 / sequence in the database,
     * this method updates these, rather than inserting the new ones.
     * <p/>
     * Note that this method inserts the new NucleotideSequence objects AND and new NucleotideSequenceXrefs
     * (possibly updating an existing NucleotideSequence object if necessary with the new NucleotideSequenceXref.)
     *
     * @param newSequences being a List of new NucleotideSequence objects to insert
     * @return a new List<NucleotideSequence> containing all of the inserted / updated NucleotideSequence objects.
     *         (Allows the caller to retrieve the primary keys for the NucleotideSequences).
     */
    public PersistedNucleotideSequences insertNewNucleotideSequences(Collection<NucleotideSequence> newSequences);

    /**
     * Instances of this class are returned from the insert method above.
     */
    public class PersistedNucleotideSequences {

        private final Set<NucleotideSequence> preExistingSequences = new HashSet<NucleotideSequence>();

        private final Set<NucleotideSequence> newSequences = new HashSet<NucleotideSequence>();

        void addPreExistingSequence(NucleotideSequence protein) {
            preExistingSequences.add(protein);
        }

        void addNewSequence(NucleotideSequence protein) {
            newSequences.add(protein);
        }

        public Set<NucleotideSequence> getPreExistingSequences() {
            return preExistingSequences;
        }

        public Set<NucleotideSequence> getNewSequences() {
            return newSequences;
        }

        public Long updateBottomNucleotideSequenceId(Long bottomNucleotideSequenceId) {
            for (NucleotideSequence newSequence : newSequences) {
                if (bottomNucleotideSequenceId == null || bottomNucleotideSequenceId > newSequence.getId()) {
                    bottomNucleotideSequenceId = newSequence.getId();
                }
            }
            return bottomNucleotideSequenceId;
        }

        public Long updateTopNucleotideSequenceId(Long topNucleotideSequenceId) {
            for (NucleotideSequence newSequence : newSequences) {
                if (topNucleotideSequenceId == null || topNucleotideSequenceId < newSequence.getId()) {
                    topNucleotideSequenceId = newSequence.getId();
                }
            }
            return topNucleotideSequenceId;
        }
    }

}
