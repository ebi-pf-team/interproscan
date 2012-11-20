package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 */
public class LoadProteinFastaFile extends LoadFastaFileImpl<Protein> {

    protected void addToMoleculeCollection(String sequence, final String currentId, final Set<Protein> parsedMolecules) {
        sequence = WHITE_SPACE_PATTERN.matcher(sequence).replaceAll("");
        Protein thisProtein = new Protein(sequence);
        // Check if this sequence is already in the Set.  If it is, retrieve it.
        boolean alreadyExists = false;
        for (Protein existing : parsedMolecules) {
            if (existing.getMd5().equals(thisProtein.getMd5())) {
                thisProtein = existing;
                alreadyExists = true;
                break;
            }
        }
        // New sequence - add it to the collection.
        if (!alreadyExists) {
            parsedMolecules.add(thisProtein);
        }

        // Add the Xref to the Protein object. (Being added to a Set, so no risk of duplicates)
        thisProtein.addCrossReference(XrefParser.getProteinXref(currentId));
    }
}
