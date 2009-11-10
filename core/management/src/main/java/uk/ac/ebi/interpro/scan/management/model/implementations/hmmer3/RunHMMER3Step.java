package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.Step;
import org.springframework.beans.factory.annotation.Required;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunHMMER3Step extends Step {

    private String fullPathToBinary;

    private String fullPathToHmmFile;

    private String fullPathOfOutputFile;

    private String binarySwitches;

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public String getBinarySwitches() {
        return binarySwitches;
    }

    @Required
    public void setBinarySwitches(String binarySwitches) {
        this.binarySwitches = binarySwitches;
    }

    public String getFullPathToHmmFile() {
        return fullPathToHmmFile;
    }

    @Required
    public void setFullPathToHmmFile(String fullPathToHmmFile) {
        this.fullPathToHmmFile = fullPathToHmmFile;
    }

    public String getFullPathOfOutputFile() {
        return fullPathOfOutputFile;
    }

    @Required
    public void setFullPathOfOutputFile(String fullPathOfOutputFile) {
        this.fullPathOfOutputFile = fullPathOfOutputFile;
    }
}
