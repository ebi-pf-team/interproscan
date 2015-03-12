package uk.ac.ebi.interpro.scan.io;

import uk.ac.ebi.interpro.scan.io.sequence.FastaEntryWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents a GFF format writer.
 */
public class GFFWriter extends TSVWriter {

    public GFFWriter(Writer writer) {
        super(writer);
    }


    public void writeDirective(String directive) throws IOException{
        writer.write("##");
        writer.write(directive);
        writer.write("\n");
    }

    public void writeFASTASequence(String identifier, String sequence) throws IOException {
        FastaEntryWriter.writeFastaFileEntry(super.writer, identifier, sequence, 60);
    }
}