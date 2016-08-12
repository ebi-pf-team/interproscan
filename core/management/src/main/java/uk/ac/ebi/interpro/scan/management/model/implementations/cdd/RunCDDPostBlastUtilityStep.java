package uk.ac.ebi.interpro.scan.management.model.implementations.cdd;




import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;


import java.util.ArrayList;
import java.util.List;

/**
 * Parses the output of CDD and stores the results to the database
 * (as filtered results - there is currently no second filtering step.)
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 5.16
 */

public class RunCDDPostBlastUtilityStep extends RunBinaryStep {
    private static final Logger LOGGER = Logger.getLogger(RunCDDPostBlastUtilityStep.class.getName());

    private String fullPathToBinary;

    private String fastaFileNameTemplate;

    private String dataConfigFilePath;

    private String cddDataPath;



    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    public void setDataConfigFilePath(String dataConfigFilePath) {
        this.dataConfigFilePath = dataConfigFilePath;
    }

    public String getCddDataPath() {
        return cddDataPath;
    }

    public void setCddDataPath(String cddDataPath) {
        this.cddDataPath = cddDataPath;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.fastaFileNameTemplate);

        final List<String> command = new ArrayList<String>();
        command.add(fullPathToBinary);
		command.add("-i");
	    command.add(fastaFilePathName);
		command.add("-d");
        command.add(this.cddDataPath);
        command.addAll(getBinarySwitchesAsList());
        Utilities.verboseLog(10, "command: " + command.toString());
        return command;
    }

}
