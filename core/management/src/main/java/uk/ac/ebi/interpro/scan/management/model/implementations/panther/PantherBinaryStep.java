package uk.ac.ebi.interpro.scan.management.model.implementations.panther;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs PANTHER binary.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class PantherBinaryStep extends RunBinaryStep {

    private String scriptPath;
    private String modelDirectory;
    private String blastPath;
    private String hmmerPath;
    private String fastaFileNameTemplate;

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    @Required
    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getModelDirectory() {
        return modelDirectory;
    }

    @Required
    public void setModelDirectory(String modelDirectory) {
        this.modelDirectory = modelDirectory;
    }

    public String getBlastPath() {
        return blastPath;
    }

    @Required
    public void setBlastPath(String blastPath) {
        this.blastPath = blastPath;
    }

    public String getHmmerPath() {
        return hmmerPath;
    }

    @Required
    public void setHmmerPath(String hmmerPath) {
        this.hmmerPath = hmmerPath;
    }

    /**
     * Returns command line for runPanther
     *
     * Example: 
     * /ebi/sp/pro1/interpro/programmers/rpetry/Onion/cvsebi-uniparc/onion/src/runPanther
                -l /ebi/sp/pro1/interpro/data/members/panther/7.0/
                -D I 
                -E 1e-3
                -B /ebi/sp/pro1/interpro/production/iprscan4.3/bin/binaries/blast/blastall
                -H /ebi/extserv/bin/hmmer-2.3.2/binaries/hmmsearch
                -T /tmp/
                -n
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return Command line.
     */
    @Override protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePath
                = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        List<String> command = new ArrayList<String>();
        // Panther script
        command.add(this.getScriptPath());
        // Models
        command.add("-l");
        command.add(this.getModelDirectory());
        // Arguments
        command.addAll(this.getBinarySwitchesAsList());
        // BLAST
        command.add("-B");
        command.add(this.getBlastPath());
        // HMMER
        command.add("-H");
        command.add(this.getHmmerPath());
        // FASTA file
        command.add("-i");
        command.add(fastaFilePath);
        // 
        return command;
    }

}