package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.GFFWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Write matches as output for InterProScan user in GFF (Generic Feature Format) version 3.
 * <p/>
 * GFF3 description (http://www.sequenceontology.org/gff3.shtml):
 * The format consists of 9 columns, separated by tabs (NOT spaces).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class ProteinMatchesGFFResultWriter extends ProteinMatchesResultWriter {

    protected GFFWriter gffWriter;

    protected final Map<String, String> identifierToSeqMap = new HashMap<String, String>();

    public ProteinMatchesGFFResultWriter(File file) throws IOException {
        super(file);
        this.gffWriter = new GFFWriter(super.fileWriter);
        //Write first line of file - always the same
        this.gffWriter.write("##gff-version 3");
        //##feature-ontology URI
        //This directive indicates that the GFF3 file uses the ontology of feature types located at the indicated URI or URL.
        this.gffWriter.write("##feature-ontology http://song.cvs.sourceforge.net/*checkout*/song/ontology/sofa.obo");
    }

    protected void addFASTASeqToMap(String key, String value) {
        if (identifierToSeqMap != null) {
            identifierToSeqMap.put(key, value);
        }
    }

    public Map<String, String> getIdentifierToSeqMap() {
        return identifierToSeqMap;
    }

    public void writeFASTASequence(String identifier, String sequence) throws IOException {
        gffWriter.writeFASTASequence(identifier, sequence);
    }
}