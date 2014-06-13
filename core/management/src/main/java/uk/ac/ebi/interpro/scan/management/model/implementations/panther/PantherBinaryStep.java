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
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
public final class PantherBinaryStep extends RunBinaryStep {

    private String scriptPath;
    private String modelDirectory;
    private String blastPath;
    private String hmmerPath;
    private String fastaFileNameTemplate;
    private String perlCommand;
    private String perlLibrary;
    private String perlScriptTempDir;

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

    public String getPerlCommand() {
        return perlCommand;
    }

    @Required
    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    public String getPerlLibrary() {
        return perlLibrary;
    }

    @Required
    public void setPerlLibrary(String perlLibrary) {
        this.perlLibrary = perlLibrary;
    }

    public String getPerlScriptTempDir() {
        return perlScriptTempDir;
    }

    public void setPerlScriptTempDir(String perlScriptTempDir) {
        this.perlScriptTempDir = perlScriptTempDir;
    }

    /**
     * Returns command line for runPanther
     * <p/>
     * Example:
     * perl -I data/panther/7.0/lib bin/panther/7.0/pantherScore.pl
     * -l path to the Panther model directory
     * -D I
     * -E 1e-3
     * -B bin/blast/2.2.6/blastall
     * -H bin/hmmer/hmmer2/2.3.2/hmmsearch
     * -T temp/
     * -n
     * -i UPI000000004D.fasta
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return Command line.
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePath
                = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        List<String> command = new ArrayList<String>();
        //Add command
        command.add(this.getPerlCommand());
        //Add Perl parameter
        command.add("-I");
        command.add(this.getPerlLibrary());
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
        // Temporary directory
        command.add("-T");
        if (this.getPerlScriptTempDir() != null) {
            command.add(this.getPerlScriptTempDir());
        } else {
            command.add(temporaryFileDirectory);
        }
        return command;
    }
}
