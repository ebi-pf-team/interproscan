package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Write matches as output in GFF (Generic Feature Format) version 3. This writer is specific
 * for protein sequence scans.
 * <p/>
 * GFF3 description (http://www.sequenceontology.org/gff3.shtml):
 * The format consists of 9 columns, separated by tabs (NOT spaces).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class GFFResultWriterForProtSeqs extends ProteinMatchesGFFResultWriter {

    private boolean writeFullGFF = true;

    public GFFResultWriterForProtSeqs(Path path) throws IOException {
        super(path);
    }

    public GFFResultWriterForProtSeqs(Path path, boolean writeFullGFF) throws IOException {
        super(path, writeFullGFF);
        this.writeFullGFF = false;
    }


    /**
     * Writes out all protein matches for the specified protein (GFF formatted).
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
        List<String> proteinIdsForGFF = getProteinAccessions(protein);

        int sequenceLength = protein.getSequenceLength();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());
        Set<Match> matches = protein.getMatches();
        //Write sequence region information
        for (String proteinIdForGFF: proteinIdsForGFF) {
            if (matches.size() > 0) {
                //Check if protein accessions are GFF3 valid
                proteinIdForGFF = ProteinMatchesGFFResultWriter.getValidGFF3SeqId(proteinIdForGFF);
                //Write sequence-region
                super.gffWriter.write("##sequence-region " + proteinIdForGFF + " 1 " + sequenceLength);
                if (writeFullGFF) {
                    writeReferenceLine(proteinIdForGFF, sequenceLength, md5);
                    addFASTASeqToMap(proteinIdForGFF, protein.getSequence());
                }
                processMatches(matches, proteinIdForGFF, date, protein, proteinIdForGFF, writeFullGFF);
            }//end match size check
        }
        return 0;
    }

    /**
     * Writes information about the target protein sequence (or reference sequence).
     */
    private void writeReferenceLine(final String seqId, final int end, final String md5)
            throws IOException {
        GFF3Feature polypeptideFeature = new GFF3Feature(seqId, ".", "polypeptide", 1, end, "+");
        polypeptideFeature.addAttribute(GFF3Feature.ID_ATTR, seqId);
//        polypeptideFeature.addAttribute(GFF3Feature.NAME_ATTR, proteinName);
        polypeptideFeature.addAttribute(GFF3Feature.MD5_ATTR, md5);
        this.gffWriter.write(polypeptideFeature.getGFF3FeatureLine());
    }
}