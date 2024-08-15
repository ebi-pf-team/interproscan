package uk.ac.ebi.interpro.scan.management.model.implementations.mobidb;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the MobiDB binary on the fasta file provided to the output file provided.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

public class RunMobiDBBinaryStep extends RunBinaryStep {

    private String fullPathToPython;

    private String fullPathToBinary;

    private String fastaFileNameTemplate;

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final List<String> command = new ArrayList<String>();
        final String fastaFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());

        if(this.getFullPathToPython().trim().isEmpty()){
            command.add("python3");
        }else{
            command.add(this.getFullPathToPython());
        }

        command.add(fullPathToBinary);
        command.addAll(getBinarySwitchesAsList());
        command.add(fastaFilePath);
        return command;
    }
}
