package uk.ac.ebi.interpro.scan.management.model.implementations.gene3d;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gift Nuka
 *
 */
public class RunAssignCathFamilyBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunAssignCathFamilyBinaryStep.class.getName());


    private String inputFileTemplate;

    private String domainToFamilyMapFile;
    private String discontinuousRegionsFile;

    private String outputFileTemplate;

    private String fullPathToPython;

    private String fullPathToBinary;

    private boolean forceHmmsearch = true;

    public String getDomainToFamilyMapFile() {
        return domainToFamilyMapFile;
    }

    @Required
    public void setDomainToFamilyMapFile(String domainToFamilyMapFile) {
        this.domainToFamilyMapFile = domainToFamilyMapFile;
    }

    public String getDiscontinuousRegionsFile() {
        return discontinuousRegionsFile;
    }

    @Required
    public void setDiscontinuousRegionsFile(String discontinuousRegionsFile) {
        this.discontinuousRegionsFile = discontinuousRegionsFile;
    }

    @Required
    public void setInputFileTemplate(String inputFileTemplate) {
        this.inputFileTemplate = inputFileTemplate;
    }

    @Required
    public void setOutputFileTemplate(String outputFileTemplate) {
        this.outputFileTemplate = outputFileTemplate;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public String getInputFileTemplate() {
        return inputFileTemplate;
    }

    public String getOutputFileTemplate() {
        return outputFileTemplate;
    }

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public boolean isForceHmmsearch() {
        return forceHmmsearch;
    }

    public void setForceHmmsearch(boolean forceHmmsearch) {
        this.forceHmmsearch = forceHmmsearch;
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
        final String inputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileTemplate());
        final String domainToFamilyMapFilePath = domainToFamilyMapFile; //stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getDomainToFamilyMapFile());

        final String discontinuousRegionsFilePath = discontinuousRegionsFile; //stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getDiscontinuousRegionsFile());

        final String outputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileTemplate());

        List<String> command = new ArrayList<String>();
        if (this.getFullPathToPython() != null && ! this.getFullPathToPython().isEmpty()){
            command.add(this.getFullPathToPython());
        }else{
            command.add("python3");
        }
        command.add(this.getFullPathToBinary());
//        command.addAll(this.getBinarySwitchesAsList());
//        command.add("--input-format=hmmer_domtmblout");
//        command.add("--worst-permissible-evalue 0.001");

//        domain_to_family_map_file = sys.argv[1]
//        discontinuous_regs_file = sys.argv[2]
//        infile = sys.argv[3]
//        outfile = sys.argv[4]

//        command.add("--output-file");
        command.add(domainToFamilyMapFilePath);
        command.add(discontinuousRegionsFilePath);
        command.add(inputFilePath);
        command.add(outputFilePath);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
