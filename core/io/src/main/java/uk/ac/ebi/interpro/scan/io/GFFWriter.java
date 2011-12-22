package uk.ac.ebi.interpro.scan.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

/**
 * Represents a GFF format writer.
 */
public class GFFWriter extends TSVWriter {

    public GFFWriter(Writer writer) {
        super(writer);
    }

    public void writeFASTASequence(String identifier, String sequence) throws IOException {
        super.writer.write(">" + identifier + "\n" + sequence + "\n");
    }
}