package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoader;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Phil Jones
 *         Date: 14-Nov-2009
 *         Time: 09:27:14
 *         <p/>
 *         Parses Fasta file (Protein or nucleic acid) and uses a SequenceLoader to load the sequences
 *         into the database.
 *         <p/>
 *         T is "Protein" or "NucleotideSequence"
 */
public abstract class LoadFastaFileImpl<T> implements LoadFastaFile {

    private static final Logger LOGGER = Logger.getLogger(LoadFastaFileImpl.class.getName());

    private SequenceLoader<T> sequenceLoader;

    protected static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");


    @Override
    @Required
    public void setSequenceLoader(SequenceLoader sequenceLoader) {
        this.sequenceLoader = sequenceLoader;
    }


    @Override
    @Transactional
    public void loadSequences(InputStream fastaFileInputStream, SequenceLoadListener sequenceLoaderListener,Map<String, SignatureLibraryRelease> analysisJobMap, boolean useMatchLookupService) {
        sequenceLoader.setUseMatchLookupService(useMatchLookupService);
        LOGGER.debug("Entered LoadFastaFileImpl.loadSequences() method");
        int sequencesParsed = 0;
        try (BufferedReader  reader = new BufferedReader(new InputStreamReader(fastaFileInputStream))) {
            String currentId = null;
            final StringBuffer currentSequence = new StringBuffer();
            int lineNumber = 0;
            String line;
            boolean foundIdLine = false;

            final Set<T> parsedMolecules = new HashSet<>();

            Utilities.verboseLog("start Parsing  input file stream");
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.length() > 0) {
                    if ('>' == line.charAt(0)) {
                        // Found ID line.
                        foundIdLine = true;
                        // Store previous record, if it exists.
                        if (currentId != null) {
                            if (LOGGER.isDebugEnabled()) {
                                if (sequencesParsed % 500 == 0) {
                                    LOGGER.debug("Stored " + sequencesParsed + " sequences.");
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace("Current id: " + currentId);
                                        LOGGER.trace("Current sequence: '" + currentSequence + "'");
                                    }
                                }
                                if (LOGGER.isTraceEnabled()) {
                                    Matcher seqCheckMatcher = Protein.AMINO_ACID_PATTERN.matcher(currentSequence);
                                    if (!seqCheckMatcher.matches()) {
                                        LOGGER.warn("Strange sequence parsed from FASTA file, does not match the Protein AMINO_ACID_PATTERN regex:\n" + currentSequence);
                                    }
                                }
                            }
                            final String seq = currentSequence.toString();
                            if (seq.trim().length() > 0) {
                                addToMoleculeCollection(seq, currentId, parsedMolecules);
                                sequencesParsed++;
                            }
                            currentSequence.delete(0, currentSequence.length());
                            if (sequencesParsed % 4000 == 0) {
                                if (sequencesParsed % 16000 == 0) {
                                    //TODO use utilities.verboselog
                                    Utilities.verboseLog("Parsed " + sequencesParsed + " sequences");
                                    //System.out.println(sdf.format(Calendar.getInstance().getTime()) + " Parsed " + sequencesParsed + " sequences");
                                }else{
                                    if(LOGGER.isInfoEnabled()){
                                        LOGGER.info( "Parsed " + sequencesParsed + " sequences");
                                    }
                                }

                            }
                        }
                        currentId = parseId(line, lineNumber);
                    } else {
                        // must be a sequence line.
                        if (foundIdLine) {
                            currentSequence.append(line.trim());
                        }
                        else {
                            // The sequence had no FASTA header, fatal user input error!
                            LOGGER.fatal("A FASTA input sequence had no header. Stopping now.");
                            System.out.println("Error: All input sequences should include their FASTA header lines.");
                            System.out.println("In the provided input, no FASTA header could be found before line: " + line);
                            System.out.println("No seqeuences have been processed.");
                            System.exit(999);
                            // Note: This doesn't stop this sort of issue, but we can't account for everything!
                            // > Seq 1
                            // Seq1Sequence
                            //
                            // Seq2Sequence
                        }
                    }
                }
            }
            // Store the final record (if there were any at all!)
            if (currentId != null) {
                addToMoleculeCollection(currentSequence.toString(), currentId, parsedMolecules);
                LOGGER.debug("About to call SequenceLoader.persist().");
            }
            Utilities.verboseLog("Completed Parsing " + sequencesParsed + " sequences");

            // Now iterate over Proteins and store using Sequence Loader.
            LOGGER.info( "Store and persist the sequences");
            sequenceLoader.storeAll(parsedMolecules, analysisJobMap);
            sequenceLoader.persist(sequenceLoaderListener, analysisJobMap);
            LOGGER.info( "Store and persist the sequences ...  completed");
            Utilities.verboseLog("Store and persist the sequences ...  completed");
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the fastaFileInputStream. ", e);
        }
    }

    /**
     * Parses out an ID line, replaces white space with underscores in IDs
     *
     * @param line
     * @param lineNumber
     * @return
     */
    private String parseId(String line, int lineNumber) {
        String currentId = null;
        if (line.length() > 1) {
            currentId = line.substring(1).trim();
        }

        if (currentId == null || currentId.isEmpty()) {
            LOGGER.error("Found an empty ID line in the FASTA file on line " + lineNumber);
            throw new IllegalStateException("Found an empty ID line in the FASTA file on line " + lineNumber);
        } else if (currentId.length() > 255) {
            // ID line is too long to fit in the database column, so trim it!
            // TODO Really this line should be parsed properly!
            currentId = currentId.substring(0, 255);
        }
        return currentId;
    }

    protected abstract void addToMoleculeCollection(String sequence, final String currentId, final Set<T> parsedMolecules);
}
