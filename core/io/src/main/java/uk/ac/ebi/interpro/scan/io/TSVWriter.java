package uk.ac.ebi.interpro.scan.io;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Writer;

public class TSVWriter {

    protected Writer writer;

    public TSVWriter(Writer writer) {
        this.writer=writer;
    }

    public void write(String... columns) throws IOException {
        writer.write(StringUtils.arrayToDelimitedString(columns,"\t"));
        writer.write("\n");
    }

    public void close() throws IOException {
        writer.close();
    }

}
