package uk.ac.ebi.interpro.scan.management.model.implementations.prints;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Jun 11, 2010
 * Time: 1:46:05 PM
 */

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.prints.PrintsMatchParser;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.PrintsRawMatchDAO;

import java.io.*;
import java.util.*;

public class ParsePrintsOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParsePrintsOutputStep.class.getName());

    private String printsOutputFileNameTemplate;

    private String pathToCutOffFile;

    private PrintsRawMatchDAO printsMatchDAO;

    private PrintsMatchParser parser;

    private double defaultCutOff = log10(1e-04);

    private String signatureLibraryRelease;

    @Required
    public void setPrintsOutputFileNameTemplate(String printsOutputFileNameTemplate) {
        this.printsOutputFileNameTemplate = printsOutputFileNameTemplate;
    }

    public String getPrintsOutputFileNameTemplate() {
        return printsOutputFileNameTemplate;
    }

    @Required
    public void setPathToCutOffFile(String pathToCutOffFile) {
        this.pathToCutOffFile = pathToCutOffFile;
    }

    public String getPathToCutOffFile() {
        return pathToCutOffFile;
    }

    @Required
    public void setPrintsRawMatchDAO(PrintsRawMatchDAO printsMatchDAO) {
        this.printsMatchDAO = printsMatchDAO;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public void setParser(PrintsMatchParser parser) {
        this.parser = parser;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     * @throws Exception could be anything thrown by the execute method.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        Map evalCutoffs;
        try {
            evalCutoffs = readPrintsParsingFile(pathToCutOffFile, defaultCutOff);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to initialise Prints hierarchy file to determine cutoff values.");
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new IllegalStateException("InterruptedException thrown by ParsePrintsOutputStep while having a snooze to allow NFS to catch up.");
        }
        InputStream inputStreamParser = null;
        try {
            final String printsOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, printsOutputFileNameTemplate);
            inputStreamParser = new FileInputStream(printsOutputFilePath);
            final PrintsMatchParser parser = this.parser;
            Set <RawProtein<PrintsRawMatch>> parsedResults = parser.parse(inputStreamParser, printsOutputFilePath, evalCutoffs, signatureLibraryRelease);
            printsMatchDAO.insertProteinMatches(parsedResults);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse Prints file " + printsOutputFileNameTemplate, e);
        } finally {
            if (inputStreamParser != null) {
                try {
                    inputStreamParser.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the Prints output file located at " + printsOutputFileNameTemplate, e);
                }
            }
        }
    }

    public static Map<String, Object> readPrintsParsingFile(String cutoffFile, double defaultCutOff) throws IOException {
        // Example of FingerPRINTShierarchy.db content:
        // The vast majority of motifs have a cutoff of 1e-04, so we will only store those whose cutoff is different
        // Need to store Fingerprint motif name and cutoff evalue
        // VIRIONINFFCT|PR00349|1e-04|0|
        // Y414FAMILY|PR01048|1e-04|0|
        BufferedReader fReader = null;
        Map<String, Object> ret = new HashMap<String, Object>();
        try {
            fReader = new BufferedReader(new FileReader(new File(cutoffFile)));
            String printsFileCommentCharacter = "#";
            String in;
            while ((in = fReader.readLine()) != null) {
                if (!in.startsWith(printsFileCommentCharacter)) {
                    String[] line = in.split("\\|");
                    double checkCutoff = log10(Double.parseDouble(line[2]));
                    if (checkCutoff != defaultCutOff) {
                        ret.put(line[0], checkCutoff);
                    }
                }
            }
        }
        finally {
            if (fReader != null) {
                fReader.close();
            }
        }
        return ret;
    }

    public static double log10(double x) {
        return Math.log(x) / Math.log(10.0);
    }

}
