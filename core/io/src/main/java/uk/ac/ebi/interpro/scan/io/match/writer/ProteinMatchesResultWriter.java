package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents an abstract protein matches file writer.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class ProteinMatchesResultWriter implements ProteinMatchesWriter {

    protected BufferedWriter fileWriter;

    protected final String VALUE_SEPARATOR = "|";

    protected boolean mapToInterProEntries;
    protected boolean mapToGO;
    protected boolean mapToPathway;
    protected String interProScanVersion = "Unknown";

    protected DateFormat dmyFormat;

    protected static final Charset characterSet = Charset.defaultCharset();

    protected ProteinMatchesResultWriter() {
    }

    public ProteinMatchesResultWriter(Path path) throws IOException {
        //int bufferSize = 8192;
        //this.fileWriter = new BufferedWriter(new FileWriter(file), bufferSize);
        this.fileWriter = Files.newBufferedWriter(path, characterSet);
        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
    }

    public ProteinMatchesResultWriter(Path path, String interProScanVersion) throws IOException {
        this(path);
        this.interProScanVersion = interProScanVersion;
    }

    public void close() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

    protected String getProteinAccession(Protein protein) {
        StringBuilder proteinXRef = new StringBuilder();
        Set<ProteinXref> crossReferences = protein.getCrossReferences();
        for (ProteinXref crossReference : crossReferences) {
            if (proteinXRef.length() > 0) proteinXRef.append(VALUE_SEPARATOR);
            proteinXRef.append(crossReference.getIdentifier());
        }
        return proteinXRef.toString();
    }

    protected List<String> getProteinAccessions(Protein protein) {
        Set<ProteinXref> crossReferences = protein.getCrossReferences();
        List<String> proteinXRefs = new ArrayList<>(crossReferences.size());
        for (ProteinXref crossReference : crossReferences) {
            proteinXRefs.add(crossReference.getIdentifier());
        }
        return proteinXRefs;

    }

    public String getInterProScanVersion() {
        return interProScanVersion;
    }

    public void setMapToInterProEntries(boolean mapToInterProEntries) {
        this.mapToInterProEntries = mapToInterProEntries;
    }

    public void setMapToGO(boolean mapToGO) {
        this.mapToGO = mapToGO;
    }

    public void setMapToPathway(boolean mapToPathway) {
        this.mapToPathway = mapToPathway;
    }

}
