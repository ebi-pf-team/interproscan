package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;

import java.io.Serializable;
import java.util.List;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunHmmer3Step extends Step<RunHmmer3StepInstance, RunHmmer3StepExecution> implements Serializable {

    private String fullPathToBinary;

    private String fullPathToHmmFile;

    private List<String> binarySwitches;

    private String hmmerOutputFilePathTemplate;

    private String fastaFilePathNameTemplate;

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public List<String> getBinarySwitches() {
        return binarySwitches;
    }

    @Required
    public void setBinarySwitches(List<String> binarySwitches) {
        this.binarySwitches = binarySwitches;
    }

    public String getFullPathToHmmFile() {
        return fullPathToHmmFile;
    }

    @Required
    public void setFullPathToHmmFile(String fullPathToHmmFile) {
        this.fullPathToHmmFile = fullPathToHmmFile;
    }

    public String getHmmerOutputFilePathTemplate() {
        return hmmerOutputFilePathTemplate;
    }

    @Required
    public void setHmmerOutputFilePathTemplate(String hmmerOutputFilePathTemplate) {
        this.hmmerOutputFilePathTemplate = hmmerOutputFilePathTemplate;
    }

    public String getFastaFilePathNameTemplate() {
        return fastaFilePathNameTemplate;
    }

    @Required
    public void setFastaFilePathNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFilePathNameTemplate = fastaFilePathNameTemplate;
    }
}
