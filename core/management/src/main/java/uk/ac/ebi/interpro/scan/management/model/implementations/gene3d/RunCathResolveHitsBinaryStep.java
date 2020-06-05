package uk.ac.ebi.interpro.scan.management.model.implementations.gene3d;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gift Nuka
 *
 */
public class RunCathResolveHitsBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunCathResolveHitsBinaryStep.class.getName());


    private String crhInputFileTemplate;

    private String crhOutputFileTemplate;

    private String fullPathToBinary;

    private boolean forceHmmsearch = true;

    @Required
    public void setCrhInputFileTemplate(String crhInputFileTemplate) {
        this.crhInputFileTemplate = crhInputFileTemplate;
    }

    @Required
    public void setCrhOutputFileTemplate(String ssfOutputFileTemplate) {
        this.crhOutputFileTemplate = ssfOutputFileTemplate;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public String getCrhInputFileTemplate() {
        return crhInputFileTemplate;
    }

    public String getCrhOutputFileTemplate() {
        return crhOutputFileTemplate;
    }

    public String getFullPathToBinary() {
        return fullPathToBinary;
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
        final String inputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getCrhInputFileTemplate());
        final String outputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getCrhOutputFileTemplate());

        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToBinary());
        command.addAll(this.getBinarySwitchesAsList());
//        command.add("--input-format=hmmer_domtmblout");
//        command.add("--worst-permissible-evalue 0.001");

        if (forceHmmsearch || Utilities.getSequenceCount() > 10){
            //use hmmsearch output
            Utilities.verboseLog(1100, "Use Hmmsearch  ");
            command.add("--input-format=hmmsearch_out");
        }else{
            //use hmmscan output
            Utilities.verboseLog(1100, "Use hmmscan  ");
            command.add("--input-format=hmmscan_out");
        }
        command.add("--hits-text-to-file");
        command.add(outputFilePath);
        command.add(inputFilePath);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
