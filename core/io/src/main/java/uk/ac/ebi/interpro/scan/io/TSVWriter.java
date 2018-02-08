package uk.ac.ebi.interpro.scan.io;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class TSVWriter implements AutoCloseable {

    protected Writer writer;

    public TSVWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeComment(String comment) throws IOException {
        writer.write(comment + "\n");
    }

    public void write(String... columns) throws IOException {
        writer.write(StringUtils.arrayToDelimitedString(columns, "\t") + "\n");
    }

    public void write(List<String> columns) throws IOException {
        writer.write(StringUtils.collectionToDelimitedString(columns, "\t") + "\n");
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}