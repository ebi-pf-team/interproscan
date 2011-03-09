package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoader;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 14-Nov-2009
 * Time: 09:27:14
 */
public class LoadFastaFileImpl implements LoadFastaFile {

    private static final Logger LOGGER = Logger.getLogger(LoadFastaFileImpl.class.getName());

    private ProteinLoader proteinLoader;

    @Override
    @Required
    public void setProteinLoader(ProteinLoader proteinLoader) {
        this.proteinLoader = proteinLoader;
    }

    @Override
    @Transactional
    public void loadSequences(InputStream fastaFileInputStream, ProteinLoadListener proteinLoaderListener) {
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
                            if (sequencesParsed++ % 50 == 0 && LOGGER.isDebugEnabled()) {
                                LOGGER.debug("About to call the ProteinLoader.store method (logging once every 50 sequences).");
                                LOGGER.debug("Current id: " + currentId);
                                LOGGER.debug("Current sequence: '" + currentSequence + "'");
                            }
                            if (LOGGER.isDebugEnabled()) {
                                Matcher seqCheckMatcher = Protein.AMINO_ACID_PATTERN.matcher(currentSequence);
                                if (!seqCheckMatcher.matches()) {
                                    LOGGER.warn("Strange sequence parsed from FASTA file, does not match the Protein AMINO_ACID_PATTERN regex:\n" + currentSequence);
                                }
                            }
                            final String seq = currentSequence.toString();
                            if (seq.trim().length() > 0) {
                                proteinLoader.store(seq.replaceAll("\\s+", ""), currentId);
                            }
                            currentSequence.delete(0, currentSequence.length());
                        }
                        if (line.length() > 1) {
                            currentId = line.substring(1).trim();
                        } else {
                            LOGGER.error("Found an empty ID line in the FASTA file on line " + lineNumber);
                            currentId = null;
                        }

                    } else {
                        // must be a sequence line.
                        currentSequence.append(line.trim());
                    }
                }
            }
            // Store the final record (if there were any at all!)
            if (currentId != null) {
                proteinLoader.store(currentSequence.toString(), currentId);
                LOGGER.debug("About to call ProteinLoader.persist().");
            }
            proteinLoader.persist(proteinLoaderListener);
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
