package uk.ac.ebi.interpro.scan.management.model.implementations.prosite;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * This step defines running the Prosite Perl script.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunPrositeProfilesBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunPrositeProfilesBinaryStep.class.getName());

    private String fullPathToPython;

    private String fullPathToPfscanBinary;
    private String fullPathToPfsearchBinary;
   private String fullPathToRunPrositeBinary;

    private String fastaFileNameTemplate;

    private String fullPathToConfirmatoryProfiles;

    private Boolean usePfsearch = false;

   private String modelsDir;

    public String getFullPathToPython() {
	return fullPathToPython;
    }

    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    public void setModelsDir(String modelsDir) {
        this.modelsDir = modelsDir;
    }

   public String getModelsDir() {
        return modelsDir;
   }

    public String getFullPathToPfscanBinary() {
        return fullPathToPfscanBinary;
    }

    @Required
    public void setFullPathToPfscanBinary(String fullPathToPfscanBinary) {
        this.fullPathToPfscanBinary = fullPathToPfscanBinary;
    }

    //pfsearch
    public String getFullPathToPfsearchBinary() {
        return fullPathToPfsearchBinary;
    }

    @Required
    public void setFullPathToPfsearchBinary(String fullPathToPfsearchBinary) {
        this.fullPathToPfsearchBinary = fullPathToPfsearchBinary;
    }


    public void setFullPathToRunPrositeBinary(String fullPathToRunPrositeBinary) {
        this.fullPathToRunPrositeBinary = fullPathToRunPrositeBinary;
    }
   public String getFullPathToRunPrositeBinary() {
       return fullPathToRunPrositeBinary;
   }

    public String getFullPathToConfirmatoryProfiles() {
        return fullPathToConfirmatoryProfiles;
    }

    public void setFullPathToConfirmatoryProfiles(String fullPathToConfirmatoryProfiles) {
        this.fullPathToConfirmatoryProfiles = fullPathToConfirmatoryProfiles;
    }

    public void setUsePfsearch(Boolean usePfsearch) {
        this.usePfsearch = usePfsearch;
    }

    /**
     * current command lines from Onion:
     * <p/>
     * HAMAP:               /ebi/sp/pro1/interpro/binaries/scripts/ps_scan.pl -d /ebi/production/interpro/data/members/hamap/180510/hamap.prf --pfscan /ebi/sp/pro1/interpro/binaries/64_bit_Linux/pfscan -l -1 -o gff
     * Prosite Profiles:    /ebi/sp/pro1/interpro/binaries/scripts/ps_scan.pl -d /ebi/sp/pro1/interpro/data/members/prosite/20.52/prosite.dat --pfscan /ebi/sp/pro1/interpro/binaries/64_bit_Linux/pfscan -s -m -o gff
     * Prosite Patterns:    /ebi/sp/pro1/interpro/binaries/scripts/ps_scan.pl -d /ebi/sp/pro1/interpro/data/members/prosite/20.52/prosite.dat --pfscan  /ebi/sp/pro1/interpro/binaries/64_bit_Linux/pfscan -r -b /ebi/sp/pro1/interpro/data/members/prosite/20.52/evaluator.dat -s -o ipro
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return
     */

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate()); 
        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToPython());
        command.add(this.getFullPathToRunPrositeBinary());
        command.add(this.getModelsDir());
        command.add(fastaFilePathName);
        command.add(outputFilePathName);
        command.add(this.getFullPathToPfsearchBinary());
        command.addAll(this.getBinarySwitchesAsList());

        Utilities.verboseLog(30, "command: " + command);
        return command;
    }
}
