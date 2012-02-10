package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.getorf.GetOrfDescriptionLineParser;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
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
    private String nucleotideId;

    /**
     * For testing only *
     */
    protected GFFResultWriterForNucSeqs() {
        super();
    }

    public GFFResultWriterForNucSeqs(File file) throws IOException {
        super(file);
    }

    private String getNucleotideId() {
        return nucleotideId;
    }

    private void setNucleotideId(String nucleotideId) {
        this.nucleotideId = super.getValidGFF3SeqId(nucleotideId);
    }

    /**
     * Writes out a Protein object to a GFF version 3 file
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
        int sequenceLength = protein.getSequenceLength();
        String date = dmyFormat.format(new Date());
        Set<Match> matches = protein.getMatches();
        String proteinIdFromGetorf;
        String proteinIdForGFF = null;
        if (matches.size() > 0) {
            for (ProteinXref proteinXref : protein.getCrossReferences()) {
                proteinIdFromGetorf = proteinXref.getIdentifier();
                //Write nucleotide sequences and ORFs
                Set<OpenReadingFrame> orfs = protein.getOpenReadingFrames();
                if (orfs != null && orfs.size() > 0) {
                    //Write nucleic acid and returns ORF of interest
                    OpenReadingFrame orf = writeNucleotideAcidLine(orfs, proteinXref.getIdentifier());
                    //Build protein identifier for GFF3
                    proteinIdForGFF = buildProteinIdentifier(orf);
                    proteinIdForGFF = super.getValidGFF3SeqId(proteinIdForGFF);

                    //Write sequence to the FASTA part
                    addFASTASeqToMap(proteinIdForGFF, protein.getSequence());
                    //Write ORF
                    super.gffWriter.write(getORFLine(orf, proteinIdFromGetorf, proteinIdForGFF, protein.getSequenceLength()));
                    //Write polypeptide
                    super.gffWriter.write(getPolypeptideLine(sequenceLength, proteinIdForGFF));
                }
                processMatches(matches, proteinIdForGFF, date, protein, getNucleotideId());

            }//end protein xrefs loop
        }
        return 0;
    }

    private OpenReadingFrame writeNucleotideAcidLine(Set<OpenReadingFrame> orfs, String proteinId) throws IOException {
        for (OpenReadingFrame orf : orfs) {
            String id = GetOrfDescriptionLineParser.getIdentifier(proteinId);
            NucleotideSequence ntSeq = orf.getNucleotideSequence();
            for (NucleotideSequenceXref xref : ntSeq.getCrossReferences()) {
                String identifier = xref.getIdentifier();
                String name = xref.getName();
                if ((identifier.equals(id) || identifier.contains(id) || (name != null && name.equals(id))) && proteinId.contains("" + orf.getStart()) && proteinId.contains("" + orf.getEnd())) {
                    //Write nucleic acid
                    setNucleotideId(xref.getIdentifier());
                    super.gffWriter.write(getNucleicAcidLine(ntSeq));
                    return orf;
                }
            }
        }
        return null;
    }

    private List<String> getORFLine(OpenReadingFrame orf, String proteinIdFromGetorf, String proteinIdForGFF, int proteinLength) {
        final String seqId = getNucleotideId();
        final String strand = (orf.getStrand().equals(NucleotideSequenceStrand.SENSE) ? "+" : "-");
        final String orfIdentifier = buildOrfIdentifier(orf);
        GFF3Feature orfFeature = new GFF3Feature(seqId, "getorf", "ORF", orf.getStart(), orf.getEnd(), strand);
        orfFeature.addAttribute("ID", orfIdentifier);
        orfFeature.addAttribute("Name", proteinIdFromGetorf);
        orfFeature.addAttribute("Target", proteinIdForGFF + " 1" + " " + proteinLength);
        return orfFeature.getGFF3FeatureLine();
    }

    /**
     * Generates an ORF identifier used in GFF3.
     */
    private String buildOrfIdentifier(OpenReadingFrame orf) {
        return new StringBuilder("orf_").append(getIdentifierSuffix(orf)).toString();
    }

    /**
     * Generates an protein identifier used in GFF3.
     */
    private String buildProteinIdentifier(OpenReadingFrame orf) {
        return new StringBuilder("pp_").append(getIdentifierSuffix(orf)).toString();
    }

    private String getIdentifierSuffix(OpenReadingFrame orf) {
        StringBuilder sb = new StringBuilder(getNucleotideId());
        sb.append("_").append(orf.getStart()).append("_").append(orf.getEnd())
                .append((orf.getStrand().equals(NucleotideSequenceStrand.ANTISENSE) ? "_r" : ""));
        return sb.toString();
    }

    private List<String> getNucleicAcidLine(NucleotideSequence nucleotideSeq) {
        String seqId = getNucleotideId();
        int end = nucleotideSeq.getSequence().length();
        GFF3Feature nucleicAcidFeature = new GFF3Feature(seqId, "provided_by_user", "nucleic_acid", 1, end, "+");
        nucleicAcidFeature.addAttribute("ID", getNucleotideId());
        nucleicAcidFeature.addAttribute("Name", getNucleotideId());
        return nucleicAcidFeature.getGFF3FeatureLine();
    }

    /**
     * Writes information about the target protein sequence (or reference sequence).
     */
    private List<String> getPolypeptideLine(int sequenceLength, String proteinIdForGFF) {
        String seqId = getNucleotideId();
        int end = sequenceLength;
        GFF3Feature polypeptideFeature = new GFF3Feature(seqId, "getorf", "polypeptide", 1, end, "+");
        polypeptideFeature.addAttribute("ID", proteinIdForGFF);
        return polypeptideFeature.getGFF3FeatureLine();
    }
}