package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import uk.ac.ebi.interpro.scan.model.Protein;

import java.io.*;
import java.util.List;

/**
 * This class writes a fasta file, using the database
 * primary keys as sequence identifiers, THEREFORE this
 * writer only works with PERSISTED Proteins
 * User: mumdad
 * Date: 15-Nov-2009
 * Time: 12:11:20
 * To change this template use File | Settings | File Templates.
 */
public class WriteFastaFile implements Serializable {

    private int sequenceLineLength = 60;

    /**
     * Optional setter for the fasta sequence line length
     * (defaults to 60).
     * @param sequenceLineLength the fasta sequence line length
     */
    public void setSequenceLineLength(int sequenceLineLength) {
        this.sequenceLineLength = sequenceLineLength;
    }

    public void writeFastaFile(List<Protein> proteins, String filePath) throws IOException, FastaFileWritingException {
        BufferedWriter writer = null;
        try{
            File file = new File(filePath);
            if (! file.createNewFile()){
                return; // File already exists, so don't try to write it again.
            }
            writer = new BufferedWriter(new FileWriter(file));
            for (Protein protein : proteins){
                if (protein.getId() == null){
                    throw new FastaFileWritingException ("The WriteFastaFile class can only write out Protein objects that have already been persisted to the database as it uses the database primary key as the protein ID in the fasta file.", filePath);
                }
                // Write ID line.
                writer.write('>');
                writer.write(protein.getId().toString());
                writer.write('\n');
                String seq = protein.getSequence();
                for (int index = 0; index < seq.length(); index += sequenceLineLength){
                    if (seq.length() > index + sequenceLineLength){
                        writer.write(seq.substring(index, index + sequenceLineLength));
                    }
                    else {
                        writer.write(seq.substring(index));
                    }
                    writer.write('\n');
                }
            }
        }
        finally {
            if (writer != null){
                writer.close();
            }
        }
    }

    public class FastaFileWritingException extends Exception{
        
        private String filePath;

        /**
         * Constructs a new exception with the specified detail message.  The
         * cause is not initialized, and may subsequently be initialized by
         * a call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public FastaFileWritingException(String message, String filePath) {
            super(message);
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    @Override
    public String toString() {
        return "WriteFastaFile{" +
                "sequenceLineLength=" + sequenceLineLength +
                '}';
    }
}
