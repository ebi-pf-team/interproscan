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
public class RunSignalPTempOptionCheckStep extends RunBinaryStep {
    private static final Logger LOGGER = Logger.getLogger(RunSignalPTempOptionCheckStep.class.getName());

    private String perlCommand;
    private String fullPathToSignalPBinary;

    @Required
    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    @Required
    public void setFullPathToSignalPBinary(String fullPathToSignalPBinary) {
        this.fullPathToSignalPBinary = fullPathToSignalPBinary;
    }

    /**
     * Create the command ready to run the Perl script.
     * <p/>
     * Example:
     * <p/>
     * perl bin/signalp/4.0/signalp -T
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return The command
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        List<String> command = new ArrayList<String>();
        command.add(this.perlCommand); // Run the perl script using installed version of Perl
        //Add Perl parameter
        command.add(this.fullPathToSignalPBinary);
        command.add("-T");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
