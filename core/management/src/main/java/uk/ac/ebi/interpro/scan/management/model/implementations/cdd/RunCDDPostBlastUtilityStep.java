package uk.ac.ebi.interpro.scan.management.model.implementations.cdd;




import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

//import uk.ac.ebi.interpro.scan.util.Utilities;

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


    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.fastaFileNameTemplate);

        final List<String> command = new ArrayList<String>();
        command.add(fullPathToBinary);
		command.add("-i");
	    command.add(fastaFilePathName);
		command.add("-c");
        command.add(this.dataConfigFilePath);
        command.addAll(getBinarySwitchesAsList());
        System.out.println("command: " + command.toString());
        return command;
    }

//    /**
//     * Implementations of RunBinaryStep may optionally override this method to
//     * return an InputStream that can be piped into the command.
//     * <p/>
//     * Coils overrides this method as the ncoils binary expects the fasta file to be piped in
//     * to the command.
//     */
//    @Override
//    protected InputStream createCommandInputStream(StepInstance stepInstance, String temporaryFileDirectory) throws IOException {
//        final String fastaFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFileNameTemplate);
//        return new FileInputStream(new File(fastaFilePath));
//    }
}
