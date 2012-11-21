package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.NucleotideSequence;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 *         <p/>
 *         Subclasses the Fasta file loader, specailising for nucleotide sequence.
 */
public class LoadNucleotideFastaFile extends LoadFastaFileImpl<NucleotideSequence> {

    protected void addToMoleculeCollection(String sequence, String currentId, Set<NucleotideSequence> parsedMolecules) {
        sequence = WHITE_SPACE_PATTERN.matcher(sequence).replaceAll("");
        NucleotideSequence thisMolecule = new NucleotideSequence(sequence);
        // Check if this sequence is already in the Set.  If it is, retrieve it.
        boolean alreadyExists = false;
        for (NucleotideSequence existing : parsedMolecules) {
            if (existing.getMd5().equals(thisMolecule.getMd5())) {
                thisMolecule = existing;
                alreadyExists = true;
                break;
            }
        }
        // New sequence - add it to the collection.
        if (!alreadyExists) {
            parsedMolecules.add(thisMolecule);
        }

        // Add the identifier to the Protein object. (Being added to a Set, so no risk of duplicates)
        thisMolecule.addCrossReference(XrefParser.getNucleotideSequenceXref(currentId));
    }
}
