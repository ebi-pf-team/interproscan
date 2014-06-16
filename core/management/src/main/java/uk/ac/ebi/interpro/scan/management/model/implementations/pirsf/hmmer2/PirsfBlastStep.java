package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs PIRSF blastall binary.
 *
 * @author Antony Quinn
 * @author Gift Nuka
 * @version $Id$
 */
public final class PirsfBlastStep extends RunBinaryStep {

    private String fastaFilePathTemplate;

    private String binary;

    private Resource blastDbFileResource;

    private String blastDbFileResourceString;

    @Required
    public void setBinary(String binary) {
        this.binary = binary;
    }

    @Required
    public void setFastaFilePathTemplate(String fastaFilePathTemplate) {
        this.fastaFilePathTemplate = fastaFilePathTemplate;
    }

    @Required
    public void setBlastDbFileResource(Resource blastDbFileResource) {
        this.blastDbFileResource = blastDbFileResource;
        if (blastDbFileResource != null) {
            try {
                File blastFile = blastDbFileResource.getFile();
                this.blastDbFileResourceString = blastFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns command line to run PIRSF BLAST
     * <p/>
     * Example:
     * ./blastall -p blastp -F F -e 0.0005 -b 300 -v 300 -m 8
     * -d /tmp/blast/test.fasta
     * -i /tmp/blast/test_proteins.fasta
     * -o /tmp/blast/test.out
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return Command line.
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        String blastInputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFilePathTemplate);

        List<String> command = new ArrayList<String>();
        //Path to blastall binary
//        command.add(binaryPathString);
//        //BLAST command
        //command.add(this.ggetFullPathToBinary());

        command.add(binary);
        command.addAll(this.getBinarySwitchesAsList());
        //Arguments
//        command.add("-p");
//        command.add("blastp");
//        //
//        command.add("-F");
//        command.add("F");
//        //e-Value cut off
//        command.add("-e");
//        command.add("0.0005");
//        //
//        command.add("-b");
//        command.add("300");
//        //
//        command.add("-v");
//        command.add("300");
//        //
//        command.add("-m");
//        command.add("8");
        //BLAST database
        command.add("-d");
        command.add(blastDbFileResourceString);
        //Input file
        command.add("-i");
        command.add(blastInputFilePathName);

        return command;
    }

}