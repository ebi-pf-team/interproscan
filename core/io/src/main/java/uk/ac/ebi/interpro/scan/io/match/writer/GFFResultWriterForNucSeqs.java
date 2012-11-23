package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.log4j.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(GFFResultWriterForNucSeqs.class.getName());
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
        String proteinIdFromGetorf = getProteinAccession(protein);
        int sequenceLength = protein.getSequenceLength();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());
        Set<Match> matches = protein.getMatches();
        String proteinIdForGFF = null;
        if (matches.size() > 0) {
            proteinIdFromGetorf = super.getValidGFF3SeqId(proteinIdFromGetorf);
            //Write nucleotide sequences and ORFs
            //Write nucleic acid and returns ORF of interest
            final OpenReadingFrame orf = writeNucleicAcidLine(protein);
            if (orf != null) {
                //Build protein identifier for GFF3
                proteinIdForGFF = buildProteinIdentifier(orf);
                proteinIdForGFF = super.getValidGFF3SeqId(proteinIdForGFF);

                //Write sequence to the FASTA part
                addFASTASeqToMap(proteinIdForGFF, protein.getSequence());
                //Write ORF
                super.gffWriter.write(getORFLine(orf, proteinIdFromGetorf, proteinIdForGFF, sequenceLength));
                //Write polypeptide
                super.gffWriter.write(getPolypeptideLine(sequenceLength, proteinIdForGFF, md5));
            }
            processMatches(matches, proteinIdForGFF, date, protein, getNucleotideId());

        }
        return 0;
    }

    private OpenReadingFrame writeNucleicAcidLine(Protein protein) throws IOException {
        for (OpenReadingFrame orf : protein.getOpenReadingFrames()) {
//            String fastaProteinId = GetOrfDescriptionLineParser.getIdentifier(proteinId);
            final NucleotideSequence nucleotideSequence = orf.getNucleotideSequence();
            for (final NucleotideSequenceXref nucleotideSequenceXref : nucleotideSequence.getCrossReferences()) {
                // Iterate over protein xrefs
                for (ProteinXref proteinXref : protein.getCrossReferences()) {
                    // Getorf appends '_N' where N is an integer to the protein accession. We need to compare this to the nucleotide sequence ID, that does not have _N on the end, so first of all strip this off for the comparison.
                    String strippedProteinId = proteinXref.getIdentifier();
                    int finalUndescorePos = strippedProteinId.lastIndexOf('_');
                    if (finalUndescorePos < 1) {
                        throw new IllegalStateException("Appear to have a protein accession without _N appended to it!: " + strippedProteinId);
                    }
                    strippedProteinId = strippedProteinId.substring(0, finalUndescorePos);
                    // Get rid of those pesky version numbers too.
                    int finalDotPos = strippedProteinId.lastIndexOf('.');
                    if (finalDotPos > 0) {
                        strippedProteinId = strippedProteinId.substring(0, finalDotPos);
                    }

                    if ((nucleotideSequenceXref.getIdentifier().equals(strippedProteinId))) {
                        //Write nucleic acid
                        setNucleotideId(nucleotideSequenceXref.getIdentifier());
                        super.gffWriter.write(getNucleicAcidLine(nucleotideSequence));
                        return orf;
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot find the ORF object that maps to protein with PK / MD5: " + protein.getId() + " / " + protein.getMd5());
    }

    private List<String> getORFLine(OpenReadingFrame orf, String proteinIdFromGetorf, String proteinIdForGFF, int proteinLength) {
        if (orf == null) {
            throw new IllegalArgumentException("A null orf has been passed in.");
        }
        final String seqId = getNucleotideId();
        final String strand = (NucleotideSequenceStrand.SENSE.equals(orf.getStrand()) ? "+" : "-");
        final String orfIdentifier = buildOrfIdentifier(orf);
        GFF3Feature orfFeature = new GFF3Feature(seqId, "getorf", "ORF", orf.getStart(), orf.getEnd(), strand);
        orfFeature.addAttribute(GFF3Feature.ID_ATTR, orfIdentifier);
        orfFeature.addAttribute(GFF3Feature.NAME_ATTR, proteinIdFromGetorf);
        orfFeature.addAttribute(GFF3Feature.TARGET_ATTR, proteinIdForGFF + " 1" + " " + proteinLength);
        NucleotideSequence ntSeq = orf.getNucleotideSequence();
        if (orf.getNucleotideSequence() != null) {
            orfFeature.addAttribute(GFF3Feature.MD5_ATTR, ntSeq.getMd5());
        }
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
        return new StringBuilder("pep_").append(getIdentifierSuffix(orf)).toString();
    }

    private String getIdentifierSuffix(OpenReadingFrame orf) {
        if (orf == null) {
            LOGGER.warn("Called GFFResultWriterForNucSeqs.getIdentifierSuffix() with a null ORF???");
            return "";
        }
        StringBuilder sb = new StringBuilder(getNucleotideId());
        sb
                .append("_")
                .append(orf.getStart())
                .append("_")
                .append(orf.getEnd())
                .append((NucleotideSequenceStrand.ANTISENSE.equals(orf.getStrand()) ? "_r" : ""));
        return sb.toString();
    }

    private List<String> getNucleicAcidLine(NucleotideSequence nucleotideSeq) {
        String seqId = getNucleotideId();
        int end = nucleotideSeq.getSequence().length();
        GFF3Feature nucleicAcidFeature = new GFF3Feature(seqId, "provided_by_user", "nucleic_acid", 1, end, "+");
        nucleicAcidFeature.addAttribute(GFF3Feature.ID_ATTR, getNucleotideId());
        nucleicAcidFeature.addAttribute(GFF3Feature.NAME_ATTR, getNucleotideId());
        nucleicAcidFeature.addAttribute(GFF3Feature.MD5_ATTR, nucleotideSeq.getMd5());
        return nucleicAcidFeature.getGFF3FeatureLine();
    }

    /**
     * Writes information about the target protein sequence (or reference sequence).
     */
    private List<String> getPolypeptideLine(int sequenceLength, String proteinIdForGFF, String md5) {
        String seqId = getNucleotideId();
        GFF3Feature polypeptideFeature = new GFF3Feature(seqId, "getorf", "polypeptide", 1, sequenceLength, "+");
        polypeptideFeature.addAttribute(GFF3Feature.ID_ATTR, proteinIdForGFF);
        polypeptideFeature.addAttribute(GFF3Feature.MD5_ATTR, md5);
        return polypeptideFeature.getGFF3FeatureLine();
    }
}
