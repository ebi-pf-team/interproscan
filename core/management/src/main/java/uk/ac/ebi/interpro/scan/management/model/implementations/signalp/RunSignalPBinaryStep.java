package uk.ac.ebi.interpro.scan.management.model.implementations.signalp;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Run the SignalP binary with supplied parameters.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunSignalPBinaryStep extends RunBinaryStep {
    private static final Logger LOGGER = Logger.getLogger(RunSignalPBinaryStep.class.getName());

    private String perlCommand;
    private String fullPathToSignalPBinary;
    private String fastaFileNameTemplate;
    private String perlLibrary;

    @Required
    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    @Required
    public void setFullPathToSignalPBinary(String fullPathToSignalPBinary) {
        this.fullPathToSignalPBinary = fullPathToSignalPBinary;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    @Required
    public void setPerlLibrary(String perlLibrary) {
        this.perlLibrary = perlLibrary;
    }

    /**
     * Create the command ready to run the binary.
     * <p/>
     * Example:
     * <p/>
     * perl bin/signalp/4.0/signalp -I bin/signalp/4.0/lib  -t euk -f summary -c 70 bin/signalp/4.0/test/euk10.fsa
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return The command
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.fastaFileNameTemplate);
        List<String> command = new ArrayList<String>();
        command.add(this.perlCommand); // Run the perl script using installed version of Perl
        //Add Perl parameter
        command.add("-I");
        command.add(this.perlLibrary);
        command.add(this.fullPathToSignalPBinary);
        command.addAll(this.getBinarySwitchesAsList());
        command.add("-T");
        command.add(temporaryFileDirectory);
        command.add(fastaFilePathName);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
