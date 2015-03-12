package uk.ac.ebi.interpro.scan.io.sequence;

import java.io.IOException;
import java.io.Writer;

/**
 * Simple tool class, which provides a method to write a FASTA file entry (identifier and sequence).
 * Each sequence line length is limit by a specified parameter.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class FastaEntryWriter {

    /**
     * Writes a FASTA file entry (identifier and sequence).
     * Each sequence line length is limit by a specified parameter.
     *
     * @throws java.io.IOException
     */
    public static void writeFastaFileEntry(Writer writer, String identifier, String sequence, int lineLength) throws IOException {
        writer.write('>');
        writer.write(identifier);
        writer.write('\n');

        for (int index = 0; index < sequence.length(); index += lineLength) {
            if (sequence.length() > index + lineLength) {
                writer.write(sequence.substring(index, index + lineLength));
            } else {
                writer.write(sequence.substring(index));
            }
            writer.write('\n');
        }
    }
}