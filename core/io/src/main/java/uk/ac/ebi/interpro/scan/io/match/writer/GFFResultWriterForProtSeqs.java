package uk.ac.ebi.interpro.scan.io.match.writer;

import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Write matches as output in GFF (Generic Feature Format) version 3.
 * <p/>
 * GFF3 description (http://www.sequenceontology.org/gff3.shtml):
 * The format consists of 9 columns, separated by tabs (NOT spaces).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class GFFResultWriterForProtSeqs extends ProteinMatchesGFFResultWriter {

    public GFFResultWriterForProtSeqs(File file) throws IOException {
        super(file);
    }


    /**
     * Writes out a Protein object to a GFF version 3 file
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
        //Get protein accession
        String proteinAcc = getProteinAccession(protein);
        int sequenceLength = protein.getSequenceLength();
        String date = dmyFormat.format(new Date());
        Set<Match> matches = protein.getMatches();
        //Write sequence region information
        if (matches.size() > 0) {
            //##sequence-region seqid start end
            super.gffWriter.write("##sequence-region " + proteinAcc + " 1 " + sequenceLength);
            writeReferenceLine(proteinAcc, sequenceLength);
            addFASTASeqToMap(proteinAcc, protein.getSequence());
        }
        for (Match match : matches) {
            final Signature signature = match.getSignature();
            final String signatureAc = signature.getAccession();
            final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
            final String analysis = signatureLibrary.getName();
            final String description = match.getSignature().getDescription();

            Set<Location> locations = match.getLocations();
            if (locations != null) {
                for (Location location : locations) {
                    String score = "-";
                    String status = "T";

                    if (location instanceof HmmerLocation) {
                        score = Double.toString(((HmmerLocation) location).getEvalue());
                    }

                    final List<String> gffFeature = new ArrayList<String>();
                    gffFeature.add(proteinAcc);
                    gffFeature.add(analysis);
                    gffFeature.add("protein_match");
                    int locStart = location.getStart();
                    int locEnd = location.getEnd();
                    gffFeature.add(Integer.toString(locStart));
                    gffFeature.add(Integer.toString(locEnd));
                    gffFeature.add(score);
                    gffFeature.add("+");
                    gffFeature.add("0");
                    //Building attributes for the last column in the GFF table
                    final List<String> gffAttributes = new ArrayList<String>();
                    gffAttributes.add("ID=match" + match.getId());
                    gffAttributes.add("Target=" + proteinAcc);
                    gffAttributes.add("Name=" + signatureAc);
                    if (description != null) {
                        gffAttributes.add("signature_desc=" + description);
                    }
                    gffAttributes.add("status=" + status);
                    gffAttributes.add("date=" + date);
                    if (mapToInterProEntries) {
                        addAditionalAttr(signature, gffAttributes);
                    }
                    //
                    gffFeature.add(StringUtils.collectionToDelimitedString(gffAttributes, ";"));
                    super.gffWriter.write(gffFeature);
                    //Add match sequence to map
                    StringBuffer matchId = new StringBuffer("match" + match.getId());
                    if (locations.size() > 1) {
                        matchId.append("_" + locStart + "_" + locEnd);
                    }
                    addFASTASeqToMap(matchId.toString(), protein.getSequence().substring(locStart, locEnd));
                }
            }
        }
        return 0;
    }

    private void addAditionalAttr(Signature signature, final List<String> gffAttributes) {
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
    private void writeReferenceLine(String accession, int sequenceLength) throws IOException {
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
        super.gffWriter.write(referenceLine);
    }
}