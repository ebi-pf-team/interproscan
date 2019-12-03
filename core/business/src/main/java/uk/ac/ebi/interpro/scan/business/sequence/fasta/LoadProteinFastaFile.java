package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 *         <p/>
 *         Subclasses the Fasta File Loader, specialising for protein sequence.
 */
public class LoadProteinFastaFile extends LoadFastaFileImpl<Protein> {

    protected void addToMoleculeCollection(String sequence, final String currentId, final Set<Protein> parsedMolecules) {
        sequence = WHITE_SPACE_PATTERN.matcher(sequence).replaceAll("");
        Protein thisProtein = new Protein(sequence);

        // Check if this sequence is already in the Set.  If it is, retrieve it.
        boolean isMoleculeAdded = parsedMolecules.add(thisProtein);
        if (!isMoleculeAdded) {
            for (Protein existing : parsedMolecules) {
                if (existing.getMd5().equals(thisProtein.getMd5())) {
                    thisProtein = existing;
                    break;
                }
            }
        }

        // Add the Xref to the Protein object. (Being added to a Set, so no risk of duplicates)
        thisProtein.addCrossReference(XrefParser.getProteinXref(currentId));
    }
}
