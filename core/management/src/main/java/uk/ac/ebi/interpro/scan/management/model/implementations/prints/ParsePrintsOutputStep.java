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
import uk.ac.ebi.interpro.scan.io.match.prints.parsemodel.PrintsProtein;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.*;
import java.util.*;

public class ParsePrintsOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParsePrintsOutputStep.class.getName());

    private String printsOutputFileNameTemplate;

    private String pathToCutOffFile;

    //private PrintsFilteredMatchDAO printsMatchDAO;

    private PrintsMatchParser parser;

    private float defaultCutOff = Float.parseFloat("1e-04");

    @Required
    public void setPrintsOutputFileNameTemplate(String printsOutputFileNameTemplate) {
        this.printsOutputFileNameTemplate = printsOutputFileNameTemplate;
    }

    @Required
    public void setPathToCutOffFile(String pathToCutOffFile) {
        this.pathToCutOffFile = pathToCutOffFile;
    }

    //    @Required
//    public void setPrintsMatchDAO(PrintsFilteredMatchDAO printsMatchDAO) {
//        this.printsMatchDAO = printsMatchDAO;
//    }

    @Required
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
        Map evalCutoffs = null;
        try {
            evalCutoffs = readPrintsParsingFile(pathToCutOffFile, defaultCutOff);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to initialise Prints hierarchy file to determine cutoff values.");
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new IllegalStateException ("InterruptedException thrown by ParsePrintsOutputStep while having a snooze to allow NFS to catch up.");
        }
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, printsOutputFileNameTemplate);
        InputStream is = null;
        try{
            is = new FileInputStream(fileName);
            Set<PrintsProtein> printsProteins = parser.parse(is, fileName, evalCutoffs);
            //printsMatchDAO.persist(printsProteins);
        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse Prints file " + fileName, e);
        } finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error ("Unable to close connection to the Prints output file located at " + fileName, e);
                }
            }
        }
    }

    public static Map<String, Object> readPrintsParsingFile(String pathToCutOffFile, float defaultCutOff) throws IOException {
        // Example of FingerPRINTShierarchy.db content:
        // The vast majority of motifs have a cutoff of 1e-04, so we will only store those whose cutoff is different
        // Need to store Fingerprint motif name and cutoff evalue
        // VIRIONINFFCT|PR00349|1e-04|0|
        // Y414FAMILY|PR01048|1e-04|0|
        BufferedReader fReader = new BufferedReader(new FileReader(pathToCutOffFile));
        String printsFileCommentCharacter = "#";
        String in;
        Map<String, Object> ret = new HashMap<String, Object>();
        while ((in = fReader.readLine()) != null) {
             if (!in.startsWith(printsFileCommentCharacter)) {
                String[] line = in.split("\\|");
                Float checkCutoff = Float.parseFloat(line[2]);
                if (checkCutoff != defaultCutOff) {
                    ret.put(line[0], checkCutoff);
                }
            }
        }
        return ret;
    }
}
