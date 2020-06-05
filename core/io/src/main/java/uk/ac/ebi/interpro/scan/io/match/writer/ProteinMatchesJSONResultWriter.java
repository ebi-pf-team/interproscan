package uk.ac.ebi.interpro.scan.io.match.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;


/**
 * Write matches as output for InterProScan user in JSON format.
 */
public class ProteinMatchesJSONResultWriter implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(ProteinMatchesJSONResultWriter.class.getName());

    protected BufferedWriter fileWriter;

    ObjectMapper mapper;
    ObjectWriter objectWriter;

    protected DateFormat dmyFormat;
    protected static final Charset characterSet = Charset.defaultCharset();

    public ProteinMatchesJSONResultWriter(Path path, boolean isSlimOutput) throws IOException {
        this.fileWriter = Files.newBufferedWriter(path, characterSet);
        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
        mapper = new ObjectMapper();
        objectWriter = (isSlimOutput ? mapper.writer() : mapper.writerWithDefaultPrettyPrinter());

    }

    public void header(String interProScanVersion) throws IOException{
        fileWriter.write("{\n \"interproscan-version\": \"" + interProScanVersion + "\",\n");
        fileWriter.write("\"results\": [ ");
    }

    public void footer() throws IOException{
        fileWriter.write(" ]\n");
        fileWriter.write("}\n");
    }
    /**
     * Writes out a set of proteins to a JSON file
     *
     * @throws IOException in the event of I/O problem writing out the file.
     */
    public void write(final IMatchesHolder matchesHolder,  final String sequenceType, final boolean isSlimOutput) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // E.g. matches for un-integrated signatures have no InterPro entry assigned

        Set<? extends OutputListElement> list = null;
        if (sequenceType.equalsIgnoreCase("n") && matchesHolder instanceof NucleicAcidMatchesHolder) {
            list = ((NucleicAcidMatchesHolder) matchesHolder).getNucleotideSequences();
        } else if (matchesHolder instanceof ProteinMatchesHolder){
            list = ((ProteinMatchesHolder) matchesHolder).getProteins();
        }
        else {
            LOGGER.error("Expected ProteinMatchesHolder or NucleicAcidMatchesHolder, found " + matchesHolder.getClass().getName());
        }

//        if (isSlimOutput) {
//            // TODO Exclude null values from slim output? This doesn't work anyway!
//            mapper.configOverride(Protein.class).setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
//            mapper.configOverride(Entry.class).setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
//            mapper.configOverride(Signature.class).setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null));
//        }
        ObjectWriter objectWriter = (isSlimOutput ? mapper.writer() : mapper.writerWithDefaultPrettyPrinter());
        fileWriter.write("{\n \"interproscan-version\": \"" + matchesHolder.getInterProScanVersion() + "\",\n");
        fileWriter.write("\"results\": [ ");
        if (list != null && list.size() > 0) {
            final int len = list.size();
            int i = 0;
            for (OutputListElement obj : list) {
                String json = objectWriter.writeValueAsString(obj);
                fileWriter.write(json);
                i++;
                if (i < len) {
                    fileWriter.write(","); // More proteins/nucleotide sequences to follow
                }
            }
        }
        fileWriter.write(" ]\n");
        fileWriter.write("}\n");
    }

    public int write(OutputListElement protein) throws IOException {
        String json = objectWriter.writeValueAsString(protein);
        fileWriter.write(json);
        return 0;
    }

    public int write(String outputString) throws IOException {
        fileWriter.write(outputString);
        return 0;
    }

    public void close() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

}
