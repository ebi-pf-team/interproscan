package uk.ac.ebi.interpro.scan.io.sequence;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads FASTA file.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class FastaSequenceReader implements SequenceReader {

    // NOTE: Use Java NIO if profiling identifies I/O as bottleneck
    // NOTE: [http://java.sun.com/docs/books/tutorial/essential/io/file.html]    

    private static final char RECORD_START = '>';
    private static final int RECORD_START_LEN = Character.valueOf(RECORD_START).toString().length();

    private final SequenceReader.Listener listener;

    private FastaSequenceReader() {
        listener = null;
    }

    public FastaSequenceReader(SequenceReader.Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        this.listener = listener;
    }

    @Override
    public void read(Resource resource) throws IOException {
        if (resource == null) {
            throw new IllegalStateException("The Resource passed in to the FastaSequenceReader.read() method must not be null.");
        }
        try {
            read(resource.getInputStream());
        } catch (IOException e) {
            throw (new IOException("Could not read " + resource.getDescription(), e));
        }
    }

    @Override
    public void read(InputStream file) throws IOException {
        String line, idLine = null;
        final StringBuilder sequence = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));
        while ((line = reader.readLine()) != null) {
            if (line.charAt(0) == RECORD_START) {
                // If we have a sequence then we're not at the first ID line in the file
                if (sequence.length() > 0) {
                    listener.mapRecord(createRecord(idLine, sequence.toString()));
                }
                idLine = line;
                sequence.delete(0, sequence.length());  // Reset
            } else {
                // Must be sequence
                if (idLine == null) {
                    throw new IllegalStateException("No ID line found in input stream");
                }
                sequence.append(line);
            }
        }
        // Handle the final record
        if (idLine != null) {
            listener.mapRecord(createRecord(idLine, sequence.toString()));
        }
    }

    private SequenceRecord createRecord(String idLine, String sequence) {
        if (sequence.length() == 0) {
            throw new IllegalArgumentException("No sequence found for: '" + idLine + "'");
        }
        final char ID_LINE_SEP = '|';                   // ID line separator
        idLine = idLine.substring(RECORD_START_LEN);    // Get everything after '>'
        if (idLine.indexOf(ID_LINE_SEP) > 0) {
            // Assume we have a UniProt-like record, for example:
            // >sp|P38398|BRCA1_HUMAN Breast cancer type 1 susceptibility protein OS=Homo sapiens GN=BRCA1 PE=1 SV=2
            final int POS_ID = 1; // Position of ID, eg. "P38398"
            final int POS_DESC = 2; // Position of description, eg. "BRCA1_HUMAN Breast cancer ..."
            int pos = 0;    // separator position
            StringBuilder id = new StringBuilder();
            StringBuilder description = new StringBuilder();
            for (char c : idLine.toCharArray()) {
                if (c == ID_LINE_SEP) {
                    pos++;
                }
                // Get ID, eg. "P38398"
                if (pos == POS_ID && c != ID_LINE_SEP) {
                    id.append(c);
                }
                // Get description, eg. "BRCA1_HUMAN Breast cancer ..."
                else if (pos == POS_DESC && c != ID_LINE_SEP) {
                    description.append(c);
                }
            }
            return new SequenceRecord(id.toString(), description.toString(), sequence);
        } else {
            // Assume the only thing on the idLine is the protein ID, for example:
            // >P38398
            return new SequenceRecord(idLine, sequence);
        }
    }

}
