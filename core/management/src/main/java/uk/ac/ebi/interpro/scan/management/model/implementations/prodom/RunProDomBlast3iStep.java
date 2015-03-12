package uk.ac.ebi.interpro.scan.management.model.implementations.prodom;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;


/**
 * This step defines running the ProDom Perl script.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunProDomBlast3iStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunProDomBlast3iStep.class.getName());

    private String perlCommand;
    private String fullPathToProDomBlast3iPerlScript;
    private String fullPathToBlast;
    private String fastaFileNameTemplate;
    private String fullPathToProDomIprFile;
    private String fullPathToTempDirectory;

    @Required
    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    @Required
    public void setFullPathToProDomBlast3iPerlScript(String fullPathToProDomBlast3iPerlScript) {
        this.fullPathToProDomBlast3iPerlScript = fullPathToProDomBlast3iPerlScript;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    @Required
    public void setFullPathToBlast(String fullPathToBlast) {
        this.fullPathToBlast = fullPathToBlast;
    }

    @Required
    public void setFullPathToProDomIprFile(String fullPathToProDomIprFile) {
        this.fullPathToProDomIprFile = fullPathToProDomIprFile;
    }

    /**
     * Optional - ProDom uses /tmp by default
     *
     * @param fullPathToTempDirectory
     */
    public void setFullPathToTempDirectory(String fullPathToTempDirectory) {
        this.fullPathToTempDirectory = fullPathToTempDirectory;
    }

    /**
     * Create the command ready to run the binary.
     * <p/>
     * Example:
     * <p/>
     * perl -I bin/prodom/2006.1 bin/prodom/2006.1/ProDomBlast3i.pl -P bin/blast/2.2.19 -d data/prodom/temp/prodom.ipr -s temp/x/jobProDom-2006.1/000000000001_000000000006.fasta -p blastp -h 0 -f
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
        command.add("-I");
        command.add("bin/prodom/2006.1");
        command.add(this.fullPathToProDomBlast3iPerlScript);
        command.add("-P");
        command.add(this.fullPathToBlast);
        command.add("-d");
        command.add(this.fullPathToProDomIprFile);
        command.add("-s");
        command.add(fastaFilePathName);
        if (this.fullPathToTempDirectory != null && !this.fullPathToTempDirectory.equals("")) {
            command.add("-t");
            command.add(this.fullPathToTempDirectory);
        }
        command.addAll(this.getBinarySwitchesAsList());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
