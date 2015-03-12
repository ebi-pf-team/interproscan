package uk.ac.ebi.interpro.scan.management.model.implementations.superfamily;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;


/**
 * This step defines running the SuperFamily Perl script.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunSuperFamilyAss3Step extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunSuperFamilyAss3Step.class.getName());

    private String perlCommand;
    private String fullPathToSuperFamilyAss3PerlScript;
    private String fullPathToSelfHitsFile;
    private String fullPathToClaFile;
    private String fullPathToModelTabFile;
    private String fullPathToPDBJ95DFile;
    private String fastaFileNameTemplate;
    private String hmmer3ResultsFileNameTemplate;
    private String binaryOutputFileNameTemplate;

    @Required
    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    @Required
    public void setFullPathToSuperFamilyAss3PerlScript(String fullPathToSuperFamilyAss3PerlScript) {
        this.fullPathToSuperFamilyAss3PerlScript = fullPathToSuperFamilyAss3PerlScript;
    }

    @Required
    public void setFullPathToSelfHitsFile(String fullPathToSelfHitsFile) {
        this.fullPathToSelfHitsFile = fullPathToSelfHitsFile;
    }

    @Required
    public void setFullPathToClaFile(String fullPathToClaFile) {
        this.fullPathToClaFile = fullPathToClaFile;
    }

    @Required
    public void setFullPathToModelTabFile(String fullPathToModelTabFile) {
        this.fullPathToModelTabFile = fullPathToModelTabFile;
    }

    @Required
    public void setFullPathToPDBJ95DFile(String fullPathToPDBJ95DFile) {
        this.fullPathToPDBJ95DFile = fullPathToPDBJ95DFile;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    @Required
    public void setHmmer3ResultsFileNameTemplate(String hmmer3ResultsFileNameTemplate) {
        this.hmmer3ResultsFileNameTemplate = hmmer3ResultsFileNameTemplate;
    }

    @Required
    public void setBinaryOutputFileNameTemplate(String binaryOutputFileNameTemplate) {
        this.binaryOutputFileNameTemplate = binaryOutputFileNameTemplate;
    }

    /**
     * Create the command ready to run the binary.
     * <p/>
     * Example:
     * <p/>
     * perl ass3.pl -e 0.0001 -s data/superfamily/1.75/self_hits.tab -r data/superfamily/1.75/dir.cla.scop.txt_1.75 -m data/superfamily/1.75/model.tab -p data/superfamily/1.75/pdbj95d  -t n  -f 1 INPUT_SEQUENCE_FILE HMMER3_OUTPUT_FILE PP_OUTPUT_FILE
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return The command
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {

        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.fastaFileNameTemplate);
        final String hmmer3ResultsFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.hmmer3ResultsFileNameTemplate);
        final String binaryOutputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.binaryOutputFileNameTemplate);

        List<String> command = new ArrayList<String>();
        command.add(this.perlCommand); // Run the perl script using installed version of Perl
        command.add(this.fullPathToSuperFamilyAss3PerlScript);
        command.addAll(this.getBinarySwitchesAsList());
        command.add("-s");
        command.add(this.fullPathToSelfHitsFile);
        command.add("-r");
        command.add(this.fullPathToClaFile);
        command.add("-m");
        command.add(this.fullPathToModelTabFile);
        command.add("-p");
        command.add(this.fullPathToPDBJ95DFile);
        command.add(fastaFilePathName); // Input sequences
        command.add(hmmer3ResultsFilePathName); // Hmmer3 output from previous step
        command.add(binaryOutputFilePathName); // Output file for this binary run
        // Note: Superclasses getOutputFileNameTemplate() contains STDOUT from the binary - keep for logging purposes but will probably be empty!

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
