package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.GFFWriter;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.GoXref;
import uk.ac.ebi.interpro.scan.model.PathwayXref;
import uk.ac.ebi.interpro.scan.model.Signature;

import java.io.File;
import java.io.IOException;
import java.util.*;


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

    protected void addAdditionalAttr(Signature signature, final List<String> gffAttributes) {
        Entry interProEntry = signature.getEntry();
        if (interProEntry != null) {
            gffAttributes.add("interPro_entry=" + interProEntry.getAccession());
            gffAttributes.add("interPro_entry_desc=" + interProEntry.getDescription());
            if (mapToGO) {
                Collection<GoXref> goXRefs = interProEntry.getGoXRefs();
                if (goXRefs != null && goXRefs.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (GoXref xref : goXRefs) {
                        if (sb.length() > 0) {
                            sb.append(VALUE_SEPARATOR);
                        }
                        sb.append(xref.getIdentifier()); // Just writeComment the GO identifier to the output
                    }
                    gffAttributes.add("go_entries=" + sb.toString());
                }
            }
            if (mapToPathway) {
                Collection<PathwayXref> pathwayXRefs = interProEntry.getPathwayXRefs();
                if (pathwayXRefs != null && pathwayXRefs.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (PathwayXref xref : pathwayXRefs) {
                        if (sb.length() > 0) {
                            sb.append(VALUE_SEPARATOR);
                        }
                        sb.append(xref.getDatabaseName() + ": " + xref.getIdentifier());
                    }
                    gffAttributes.add("pathways=" + sb.toString());
                }
            }
        }
    }

    /**
     * Writes information about the target protein sequence (or reference sequence).
     *
     * @param accession
     * @param sequenceLength
     * @throws IOException
     */
    protected void writeReferenceLine(String accession, int sequenceLength) throws IOException {
        final List<String> referenceLine = new ArrayList<String>();
        referenceLine.add(accession);
        referenceLine.add(".");
        referenceLine.add("polypeptide");
        referenceLine.add("1");
        referenceLine.add("" + sequenceLength);
        referenceLine.add(".");
        referenceLine.add("+");
        referenceLine.add("0");
        referenceLine.add("ID=" + accession);
        this.gffWriter.write(referenceLine);
    }
}