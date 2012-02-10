package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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

    public GFFResultWriterForProtSeqs(File file) throws IOException {
        super(file);
    }


    /**
     * Writes out all protein matches for the specified protein (GFF formatted).
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
        int sequenceLength = protein.getSequenceLength();
        String date = dmyFormat.format(new Date());
        Set<Match> matches = protein.getMatches();
        //Write sequence region information
        if (matches.size() > 0) {
            for (ProteinXref proteinXref : protein.getCrossReferences()) {
                //Get protein identifier and check if it is GFF3 valid
                String proteinIdForGFF = proteinXref.getIdentifier();
                proteinIdForGFF = super.getValidGFF3SeqId(proteinIdForGFF);
                //Get protein name
                String proteinName = proteinXref.getName();
                //Write sequence-region
                super.gffWriter.write("##sequence-region " + proteinIdForGFF + " 1 " + sequenceLength);
                writeReferenceLine(proteinIdForGFF, proteinName, sequenceLength);
                addFASTASeqToMap(proteinIdForGFF, protein.getSequence());

                processMatches(matches, proteinIdForGFF, date, protein, proteinIdForGFF);
            }//end protein xrefs loop
        }//end match size check
        return 0;
    }

    /**
     * Writes information about the target protein sequence (or reference sequence).
     */
    private void writeReferenceLine(final String seqId, final String proteinName, int end) throws IOException {
        GFF3Feature polypeptideFeature = new GFF3Feature(seqId, ".", "polypeptide", 1, end, "+");
        polypeptideFeature.addAttribute("ID", seqId);
        polypeptideFeature.addAttribute("Name", proteinName);
        this.gffWriter.write(polypeptideFeature.getGFF3FeatureLine());
    }
}