package uk.ac.ebi.interpro.scan.io.match.writer;

import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Write matches as output in GFF (Generic Feature Format) version 3. This writer is specific
 * for nucleotide sequence scans (back tracking from protein match to nucleotide sequence).
 * <p/>
 * GFF3 description (http://www.sequenceontology.org/gff3.shtml):
 * The format consists of 9 columns, separated by tabs (NOT spaces).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class GFFResultWriterForNucSeqs extends ProteinMatchesGFFResultWriter {


    public GFFResultWriterForNucSeqs(File file) throws IOException {
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
                        addAdditionalAttr(signature, gffAttributes);
                    }
                    //
                    gffFeature.add(StringUtils.collectionToDelimitedString(gffAttributes, ";"));
                    super.gffWriter.write(gffFeature);
                    //Add match sequence to the map
                    StringBuilder matchId = new StringBuilder("match" + match.getId());
                    if (locations.size() > 1) {
                        matchId.append("_").append(locStart).append("_").append(locEnd);
                    }
                    addFASTASeqToMap(matchId.toString(), protein.getSequence().substring(locStart, locEnd));
                }
            }
        }
        return 0;
    }
}