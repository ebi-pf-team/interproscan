package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoader;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Phil Jones
 *         Date: 14-Nov-2009
 *         Time: 09:27:14
 *         <p/>
 *         Parses Fasta file (Protein or nucleic acid) and uses a SequenceLoader to load the sequences
 *         into the database.
 */
public class LoadFastaFileImpl implements LoadFastaFile {

    private static final Logger LOGGER = Logger.getLogger(LoadFastaFileImpl.class.getName());

    private SequenceLoader sequenceLoader;

    @Override
    @Required
    public void setSequenceLoader(SequenceLoader sequenceLoader) {
        this.sequenceLoader = sequenceLoader;
    }

    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    @Transactional
    public void loadSequences(InputStream fastaFileInputStream, SequenceLoadListener sequenceLoaderListener, String analysisJobNames) {
        LOGGER.debug("Entered LoadFastaFileImpl.loadSequences() method");
        BufferedReader reader = null;
        int sequencesParsed = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(fastaFileInputStream));
            String currentId = null;
            final StringBuffer currentSequence = new StringBuffer();
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.length() > 0) {
                    if ('>' == line.charAt(0)) {
                        // Found ID line.
                        // Store previous record, if it exists.
                        if (currentId != null) {
                            if (LOGGER.isDebugEnabled()) {
                                if (++sequencesParsed % 500 == 0) {
                                    LOGGER.debug("Stored " + sequencesParsed + " sequences.");
                                    LOGGER.debug("Current id: " + currentId);
                                    LOGGER.debug("Current sequence: '" + currentSequence + "'");
                                }
                                Matcher seqCheckMatcher = Protein.AMINO_ACID_PATTERN.matcher(currentSequence);
                                if (!seqCheckMatcher.matches()) {
                                    LOGGER.warn("Strange sequence parsed from FASTA file, does not match the Protein AMINO_ACID_PATTERN regex:\n" + currentSequence);
                                }
                            }
                            final String seq = currentSequence.toString();
                            if (seq.trim().length() > 0) {
                                sequenceLoader.store(WHITE_SPACE_PATTERN.matcher(seq).replaceAll(""), analysisJobNames, currentId);
                            }
                            currentSequence.delete(0, currentSequence.length());
                        }
                        if (line.length() > 1) {
                            currentId = line.substring(1).trim();
                        }

                        if (currentId == null || currentId.isEmpty()) {
                            LOGGER.error("Found an empty ID line in the FASTA file on line " + lineNumber);
                            currentId = null;
                        } else if (currentId.length() > 255) {
                            // ID line is too long to fit in the database column, so trim it!
                            // TODO Really this line should be parsed properly!
                            currentId = currentId.substring(0, 255);
                        }


                    } else {
                        // must be a sequence line.
                        currentSequence.append(line.trim());
                    }
                }
            }
            // Store the final record (if there were any at all!)
            if (currentId != null) {
                sequenceLoader.store(currentSequence.toString(), analysisJobNames, currentId);
                LOGGER.debug("About to call SequenceLoader.persist().");
            }
            sequenceLoader.persist(sequenceLoaderListener, analysisJobNames);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the fastaFileInputStream. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close reader the fasta file input stream ", e);
                }
            }
        }
    }
}
