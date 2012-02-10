package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import uk.ac.ebi.interpro.scan.io.sequence.FastaEntryWriter;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class writes a fasta file, using the database
 * primary keys as sequence identifiers, THEREFORE this
 * tsvWriter only works with PERSISTED Proteins
 * <p/>
 * TODO: Shouldn't there be only 1 fasta file writer ({@link uk.ac.ebi.interpro.scan.io.sequence.FastaSequenceWriter})?
 *
 * @author Phil Jones
 *         Date: 15-Nov-2009
 *         Time: 12:11:20
 *         To change this template use File | Settings | File Templates.
 */
public class FastaFileWriter implements Serializable {

    private int sequenceLineLength = 60;

    private Pattern alphabetPattern;

    private Map<String, String> residueSubstitutions;

    /**
     * If an analysis is picky about the amino acid codes that it can handle,
     * this Pattern can be initialised to check the sequence.  This can be left
     * null for non-picky analyses (i.e. most of them).
     *
     * @param alphabet the alphabet to which the sequence must be restricted.
     *                 The base 20 amino acids are: ARNDCEQGHILKMFPSTWYV
     */
    public void setValidAlphabet(final String alphabet) {
        alphabetPattern = Pattern.compile("^[" + alphabet + "]+$");
    }

    /**
     * If an alphabetPattern has been provided, can optionally provide this residue subsitution
     * to allow a sequence to be "rescued" so that it can be analysed. E.g. Phobius will not
     * accept Pyrrolisine residues, in which case this can be configured to convert O to K.
     * (Pyrrolisine to Lysine).
     *
     * @param residueSubstitutions a Map of start -> end residues
     */
    public void setResidueSubstitutions(Map<String, String> residueSubstitutions) {
        this.residueSubstitutions = residueSubstitutions;
    }

    private boolean deviatesFromAlphabet(final String sequence) {
        return alphabetPattern != null && !alphabetPattern.matcher(sequence).matches();
    }

    /**
     * Optional setter for the fasta sequence line length
     * (defaults to 60).
     *
     * @param sequenceLineLength the fasta sequence line length
     */
    public void setSequenceLineLength(int sequenceLineLength) {
        this.sequenceLineLength = sequenceLineLength;
    }

    public void writeFastaFile(List<Protein> proteins, String filePath) throws IOException, FastaFileWritingException {
        BufferedWriter writer = null;
        try {
            final File file = new File(filePath);
            if (file.exists()) {
                // File already exists - delete it.  Must be from a previous run that did not finish successfully,
                // so need to clean up and continue.
                if (!file.delete()) {
                    throw new IllegalStateException("Unable to delete the old fasta file at path " + file.getAbsolutePath());
                }
            }
            if (!file.createNewFile()) {
                throw new IllegalStateException("Unable to create new fasta file at " + file.getAbsolutePath());
            }
            writer = new BufferedWriter(new FileWriter(file));
            for (Protein protein : proteins) {
                String seq = protein.getSequence();
                // Analyses such as Phobius & TMHMM break if they are given non-standard amino acids
                // such as Pyrrolysine (O).
                if (deviatesFromAlphabet(seq)) {
                    // Attempt to replace disallowed residues
                    if (residueSubstitutions != null) {
                        for (String from : residueSubstitutions.keySet()) {
                            seq = seq.replaceAll(from, residueSubstitutions.get(from));
                        }
                    }
                    if (deviatesFromAlphabet(seq)) {
                        // OK, so even following the substitution, the sequence still contains non-standard codes.
                        // Do not attempt to analyse this sequence.
                        continue;
                    }
                }
                if (protein.getId() == null) {
                    throw new FastaFileWritingException("The FastaFileWriter class can only write out Protein objects that have already been persisted to the database as it uses the database primary key as the protein ID in the fasta file.", filePath);
                }
                FastaEntryWriter.writeFastaFileEntry(writer, protein.getId().toString(), seq, sequenceLineLength);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public class FastaFileWritingException extends Exception {

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
        return "FastaFileWriter{" +
                "sequenceLineLength=" + sequenceLineLength +
                '}';
    }
}
