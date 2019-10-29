package uk.ac.ebi.interpro.scan.management.model.implementations.hamap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.sequence.FastaSequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceRecord;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.FastaFileWriter;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.Transient;
import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the output of hmmer and stores the results to file
 * ()
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class HamapCreateMiniDatabaseStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(HamapCreateMiniDatabaseStep.class.getName());

    private String filteredFastaInputFileNameTemplate;

    private String outputFileTemplate;
    private String outputFileNameTbloutTemplate;

    @Transient
    private final FastaFileWriter fastaFileWriter = new FastaFileWriter();

    private String fastaFileNameTemplate;

//
//    @Required
//    public void setProteinDAO(ProteinDAO proteinDAO) {
//        this.proteinDAO = proteinDAO;
//    }

    public String getOutputFileNameTbloutTemplate() {
        return outputFileNameTbloutTemplate;
    }

    @Required
    public void setOutputFileNameTbloutTemplate(String outputFileNameTbloutTemplate) {
        this.outputFileNameTbloutTemplate = outputFileNameTbloutTemplate;
    }


    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    public String getOutputFileTemplate() {
        return outputFileTemplate;
    }

    @Required
    public void setOutputFileTemplate(String outputFileTemplate) {
        this.outputFileTemplate = outputFileTemplate;
    }

    public String getFilteredFastaInputFileNameTemplate() {
        return filteredFastaInputFileNameTemplate;
    }

    public void setFilteredFastaInputFileNameTemplate(String filteredFastaInputFileNameTemplate) {
        this.filteredFastaInputFileNameTemplate = filteredFastaInputFileNameTemplate;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory being the directory in which the raw file is being stored.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        final long startTime = System.currentTimeMillis();
        //do we need to skip
        if (doSkipRun) {
            Utilities.verboseLog(10, "doSkipRun - step: "  + this.getId());
            return;
        }

        delayForNfs();
        final String fileNameTblout = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileNameTbloutTemplate);
        final String fastaFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFileNameTemplate);
        final String filteredFastaInputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, filteredFastaInputFileNameTemplate);

        InputStream isTblout = null;
        InputStream isFastaFile = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        BufferedReader reader2 = null;


        try {
            isTblout = new FileInputStream(fileNameTblout);

            LOGGER.debug("hmmer3 search output in tblout format: " + fileNameTblout);
            LOGGER.debug("Initial FASTA file input: " + fileNameTblout);
            LOGGER.debug("Writing proteins to FASTA file..." + filteredFastaInputFileName);

            writer = new BufferedWriter(new FileWriter(filteredFastaInputFileName));
            reader = new BufferedReader(new InputStreamReader(isTblout));
            String line;
            HashSet <String> uniqueIdentifiers = new HashSet<String>();
            Pattern patternGetIdentifier = Pattern.compile("^\\S+");
            Pattern patternGetComment = Pattern.compile("^#");

            //get the unique list of identifiers
            while ((line = reader.readLine()) != null) {
                if (patternGetComment.matcher(line).find()) {
                    continue;
                } else {
                    Matcher matcher = patternGetIdentifier.matcher(line);
                    if (matcher.find()) {
                        String identifier = matcher.group();
                        uniqueIdentifiers.add(identifier);
//                        writer.write(">" +  identifier + "\n");
                    }
                }
            }
            LOGGER.debug("Found " + uniqueIdentifiers.size() + " unique hits");

            //get all the input sequences
            isFastaFile = new FileInputStream(fastaFileName);
            final Map<String, SequenceRecord> sequences = new HashMap<String, SequenceRecord>();
            reader = new BufferedReader(new InputStreamReader(isFastaFile));
            SequenceReader sequenceReader = new FastaSequenceReader(
                    new SequenceReader.Listener() {
                        @Override public void mapRecord(SequenceRecord r) {
                            //SequenceRecord sequence = r.getSequence();
                            sequences.put(r.getId(), r); // Use sequence id as key
                        }
                    }
            );
            sequenceReader.read(isFastaFile);
            for (String key: uniqueIdentifiers){
                SequenceRecord sequenceRecord =  sequences.get(key);

                if (sequenceRecord != null){
                    writer.write(">" + sequenceRecord.getId() + "\n");
                    writer.write(sequenceRecord.getSequence() + "\n");
                }else{
                    LOGGER.debug("Sequence for key " + key + " is null");
                }
            }

        }
        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse hamapFillter file " + fileNameTblout, e);
        } finally {
            if (isTblout != null) {
                try {
                    isTblout.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the hamapFillter output file located at " + fileNameTblout, e);
                    throw new IllegalStateException("IOException thrown when attempting to close the InputStream from the hamapFillter output file.", e);
                }
            }
            if (writer != null) {
                try {
                 writer.close();
                } catch (IOException e) {
                    throw new IllegalStateException("IOException thrown when attempting to close writer");
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException("IOException thrown when attempting to close reader");
                }
            }
            final long endTime = System.currentTimeMillis();
            LOGGER.debug("HamapCreateMiniDatabaseStep takes  " + (endTime - startTime) + "ms");
        }


    }

}
