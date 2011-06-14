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

    private String fullPathToProDomBlast3iPerlScript;

    private String fullPathToBlastBinary;

    private String fastaFileNameTemplate;

    private String fullPathToProDomIprFile;

    private String fullPathToTempDirectory;

    @Required
    public void setFullPathToProDomBlast3iPerlScript(String fullPathToProDomBlast3iPerlScript) {
        this.fullPathToProDomBlast3iPerlScript = fullPathToProDomBlast3iPerlScript;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    @Required
    public void setFullPathToBlastBinary(String fullPathToBlastBinary) {
        this.fullPathToBlastBinary = fullPathToBlastBinary;
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
     * Current command line from Onion:
     * <p/>
     * /ebi/sp/pro1/Onion/calcs/PRODOM/ProDomBlast3i.pl -P /ebi/production/interpro/Onion/blast/blast-2.2.19/bin/ -p blastp -d /ebi/production/interpro/Onion/prodom/2006.1/prodom.ipr -s SEQUENCE_FILE -t /tmp/ -h 0 -f  > OUTPUT_FILE
     * <p/>
     * Example (relative URLs):
     * <p/>
     * perl ProDomBlast3i.pl -P ../../blast/2.2.19 -p blastp -d ../../../data/prodom/2006.1/prodom.ipr -s ../../../data/prodom/prodom_test.seqs -t /tmp/prodom -h 0 -f -o test.out
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return
     */

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.fastaFileNameTemplate);
        List<String> command = new ArrayList<String>();
        command.add("perl"); // Run the perl script using installed version of Perl
        command.add("-I");
        command.add("bin/prodom/2006.1");
        command.add("bin/prodom/2006.1/calcs");
        command.add("bin/prodom/2006.1/calcs/XML_BLAST");
        command.add("data/prodom/2006.1");
        command.add(this.fullPathToProDomBlast3iPerlScript);
        command.add("-P");
        command.add(this.fullPathToBlastBinary);
        command.add("-d");
        command.add(this.fullPathToProDomIprFile);
        command.add("-s");
        command.add(fastaFilePathName);
//        command.add("-t");
//        command.add(this.fullPathToTempDirectory);
        command.addAll(this.getBinarySwitchesAsList());
        return command;
    }
}
