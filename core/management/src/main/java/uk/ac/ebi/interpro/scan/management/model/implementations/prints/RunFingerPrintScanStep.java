package uk.ac.ebi.interpro.scan.management.model.implementations.prints;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs fingerprintscan and reports any errors
 * spat out if the exit status != 0.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunFingerPrintScanStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunFingerPrintScanStep.class.getName());

    private String fullPathToBinary;

    private String fullPathToHmmFile;

    private String fastaFileNameTemplate;

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public String getFullPathToHmmFile() {
        return fullPathToHmmFile;
    }

    @Required
    public void setFullPathToHmmFile(String fullPathToHmmFile) {
        this.fullPathToHmmFile = fullPathToHmmFile;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    /**
     * current command line from Onion:
     * /ebi/sp/pro1/interpro/binaries/64_bit_Linux/fingerPRINTScan /ebi/sp/pro1/interpro/data/members/prints/40.0/prints.pval xxxxx.fasta -e 0.0001 -d 10 -E 257043 84355444 -fj  -o 15
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return
     */

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToBinary());
        command.add(this.getFullPathToHmmFile());
        command.add(fastaFilePathName);
        command.addAll(this.getBinarySwitchesAsList());
        return command;
    }
}
