package uk.ac.ebi.interpro.scan.io.match.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesJSONResultWriter.class.getName());

    protected BufferedWriter fileWriter;

    protected DateFormat dmyFormat;
    protected static final Charset characterSet = Charset.defaultCharset();

    public ProteinMatchesJSONResultWriter(Path path) throws IOException {
        this.fileWriter = Files.newBufferedWriter(path, characterSet);
        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
    }


    /**
     * Writes out a set of proteins to a JSON file
     *
     * @throws IOException in the event of I/O problem writing out the file.
     */
    public void write(final IMatchesHolder matchesHolder, final String sequenceType, final boolean isSlimOutput) throws IOException {
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
        fileWriter.write("[");
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
        fileWriter.write("]");
    }

    public void close() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

}
