package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        boolean first = true;
        try {
            reader = new BufferedReader(new InputStreamReader(fastaFileInputStream));
            String currentId = null;
            final StringBuffer currentSequence = new StringBuffer();
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.length() > 0) {
                    if ('>' == line.charAt(0)) {
                        // Found ID line.
                        // Store previous record, if it exists.
                        if (currentId != null) {
                            if (first && LOGGER.isDebugEnabled()) {
                                first = false;
                                LOGGER.debug("About to call the ProteinLoader.store method (logged first time only).");
                                LOGGER.debug("Current sequence: " + currentSequence);
                                LOGGER.debug("Current id: " + currentId);
                            }
                            proteinLoader.store(currentSequence.toString(), currentId);
                            currentSequence.delete(0, currentSequence.length());
                        }
                        currentId = line.substring(1).trim();
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
                proteinLoader.persist(proteinLoaderListener);
            }
        }
        catch (IOException e) {
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
