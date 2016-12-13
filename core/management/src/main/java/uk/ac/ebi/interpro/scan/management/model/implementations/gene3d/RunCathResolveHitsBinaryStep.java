package uk.ac.ebi.interpro.scan.management.model.implementations.gene3d;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 08/09/11
 * Time: 14:10
 */
public class RunCathResolveHitsBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunCathResolveHitsBinaryStep.class.getName());


    private String ssfInputFileTemplate;

    private String ssfOutputFileTemplate;

    private String fullPathToBinary;

    @Required
    public void setSsfInputFileTemplate(String ssfInputFileTemplate) {
        this.ssfInputFileTemplate = ssfInputFileTemplate;
    }

    @Required
    public void setSsfOutputFileTemplate(String ssfOutputFileTemplate) {
        this.ssfOutputFileTemplate = ssfOutputFileTemplate;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public String getSsfInputFileTemplate() {
        return ssfInputFileTemplate;
    }

    public String getSsfOutputFileTemplate() {
        return ssfOutputFileTemplate;
    }

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    /**
     * Implementations of this method should return a List<String> containing all the components of the command line to be called
     * including any arguments. The StepInstance and temporary file are provided to allow parameters to be built. Use
     * stepInstance.buildFullyQualifiedFilePath to assist building paths.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return elements of the command in a list.
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String inputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getSsfInputFileTemplate());
        final String outputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getSsfOutputFileTemplate());

        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToBinary());
        //command.addAll(this.getBinarySwitchesAsList());
        command.add("--input-format=hmmer_domtmblout");
        command.add("--output-file");
        command.add(outputFilePath);
        command.add(inputFilePath);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
