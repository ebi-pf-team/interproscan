package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfSubfamilyFileParser;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.hmmer.RunHmmerBinaryStep;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class runs HMMER 2 for PIRSF and reports any errors
 * spat out if the exit status != 0.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfRunHmmerBinaryStep extends RunHmmerBinaryStep {

    // File name for subfamilies
    private String subFamiliesFileName;

    // File parser
    private PirsfSubfamilyFileParser subfamilyFileParser;

    @Required
    public void setSubFamiliesFileName(String subFamiliesFileName) {
        this.subFamiliesFileName = subFamiliesFileName;
    }

    @Required
    public void setSubfamilyFileParser(PirsfSubfamilyFileParser subfamilyFileParser) {
        this.subfamilyFileParser = subfamilyFileParser;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        String subFamiliesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, subFamiliesFileName);
        Resource subFamilyMapFileResource = new FileSystemResource(subFamiliesFilePath);
        //Maps subfamilies to super families model accessions
        Map<String, String> subfamToSuperFamMap;
        try {
            subfamToSuperFamMap = subfamilyFileParser.parse(subFamilyMapFileResource);
            if (subfamToSuperFamMap.size() > 0) {
                return super.createCommand(stepInstance, temporaryFileDirectory);
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse sub family mapping file!", e);
        }
        return null;
    }
}