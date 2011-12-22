package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

/**
 * Represents an abstract protein matches file writer.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class ProteinMatchesResultWriter implements IProteinMatchesWriter {

    protected FileWriter fileWriter;

    protected final String VALUE_SEPARATOR = "|";

    protected boolean mapToInterProEntries;
    protected boolean mapToGO;
    protected boolean mapToPathway;

    protected DateFormat dmyFormat;

    public ProteinMatchesResultWriter(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("The file being written to already exists and cannot be deleted: " + file.getAbsolutePath());
            }
        }
        this.fileWriter = new FileWriter(file);
        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
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

    public void setMapToInterProEntries(boolean mapToInterProEntries) {
        this.mapToInterProEntries = mapToInterProEntries;
    }

    public void setMapToGo(boolean mapToGO) {
        this.mapToGO = mapToGO;
    }

    public void setMapToPathway(boolean mapToPathway) {
        this.mapToPathway = mapToPathway;
    }
}