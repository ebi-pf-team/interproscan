package uk.ac.ebi.interpro.scan.management.model.implementations.smart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.postprocessing.smart.SmartResourceManager;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer.RunHmmerBinaryStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 05/04/12
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public class RunSmartBinaryStep extends RunHmmerBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunSmartBinaryStep.class.getName());


    private SmartResourceManager smartResourceManager;

    @Required
    public void setSmartResourceManager(SmartResourceManager smartResourceManager) {
        this.smartResourceManager = smartResourceManager;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());

        List<String> command = new ArrayList<>();
        command.add(this.getFullPathToHmmsearchBinary());
        command.addAll(this.getBinarySwitchesAsList());
        if (!smartResourceManager.isLicensed()) {
            // Increase the stringency, to keep out the poor matches.
            command.add("-E");
            command.add("0.01");
            command.add("-Z");
            command.add("350000");
        }
        command.add(this.getFullPathToHmmFile());
        command.add(fastaFilePathName);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }

    private boolean resourceExists(Resource resource) {
        return resource != null && resource.exists() && resource.isReadable();
    }
}
