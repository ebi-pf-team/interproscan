package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.Step;

/**
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileStep extends Step {

    private String fullPathToFastaFile;

    public String getFullPathToFastaFile() {
        return fullPathToFastaFile;
    }

    public void setFullPathToFastaFile(String fullPathToFastaFile) {
        this.fullPathToFastaFile = fullPathToFastaFile;
    }
}
