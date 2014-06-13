package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Run the pirsf.pl perl script (used for HMMER3 version of PIRSF introduced in version 2.85).
 */
public class PirsfBinaryStep extends RunBinaryStep {

    private String scriptPath;
    private String fastaFileNameTemplate;
    private String hmmerPath;
    private String sfHmmAllPath;
    private String pirsfDatPath;
    private String perlCommand;

    public String getScriptPath() {
        return scriptPath;
    }

    @Required
    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    public String getHmmerPath() {
        return hmmerPath;
    }

    @Required
    public void setHmmerPath(String hmmerPath) {
        this.hmmerPath = hmmerPath;
    }

    public String getSfHmmAllPath() { return sfHmmAllPath; }

    @Required
    public void setSfHmmAllPath(String sfHmmAllPath) { this.sfHmmAllPath = sfHmmAllPath; }

    public String getPirsfDatPath() { return pirsfDatPath; }

    @Required
    public void setPirsfDatPath(String pirsfDatPath) { this.pirsfDatPath = pirsfDatPath; }

    public String getPerlCommand() {
        return perlCommand;
    }

    @Required
    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    /**
     * Returns command line for running the PIRSF Perl script.
     * <p/>
     * Example (from interproscan/core/jms-implementation/support-mini-x86-32):
     *      perl ./bin/pirsf/2.85/pirsf.pl
     *      --fasta ../src/test/resources/data/pirsf/2.85/test_pirsf_hmmer3.fasta
     *      -path bin/hmmer/hmmer3/3.1b1/
     *      --hmmlib data/pirsf/2.85/sf_hmm_all
     *      -dat data/pirsf/2.85/pirsf.dat
     *      --outfmt i5
     *      -cpu 4
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
        // PIRSF script
        command.add(this.getScriptPath());
        // Input FASTA file
        command.add("--fasta");
        command.add(fastaFilePath);
        // Path to HMMER3 binaries
        command.add("-path");
        command.add(this.getHmmerPath());
        // Path to sf_hmm_all
        command.add("--hmmlib");
        command.add(this.getSfHmmAllPath());
        // Path to pirsf.dat
        command.add("-dat");
        command.add(this.getPirsfDatPath());
        // Arguments
        command.addAll(this.getBinarySwitchesAsList());
        return command;
    }
}
