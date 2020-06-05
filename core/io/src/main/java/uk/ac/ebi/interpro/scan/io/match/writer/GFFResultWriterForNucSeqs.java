package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
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

    private static final Logger LOGGER = LogManager.getLogger(GFFResultWriterForNucSeqs.class.getName());
    private String nucleotideId;

    /**
     * For testing only *
     */
    protected GFFResultWriterForNucSeqs() {
        super();
    }

    public GFFResultWriterForNucSeqs(Path path, String interProScanVersion, boolean proteinSequence) throws IOException {
        super(path, interProScanVersion);
        this.proteinSequence = proteinSequence;
    }

    private String getNucleotideId() {
        return nucleotideId;
    }

    private void setNucleotideId(String nucleotideId) {
        this.nucleotideId = getValidGFF3SeqId(nucleotideId);
    }

    /**
     * Writes out a {@link NucleotideSequence} object to a GFF version 3 file
     *
     * @param nucleotideSequence containing orfs, proteins and matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(NucleotideSequence nucleotideSequence) throws IOException {
        writeSequenceRegionPart(nucleotideSequence);
        return 0;
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
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());
        Set<Match> matches = protein.getMatches();
        String proteinIdForGFF = null;
        proteinSequence = false;
        Utilities.verboseLog(1100, "proteinSequence in GFFResultWriterForNucSeqs: " + proteinSequence);

        List<String> proteinIdsFromGetOrf = getProteinAccessions(protein, proteinSequence);
        for (String proteinIdFromGetorf : proteinIdsFromGetOrf) {
            if (matches.size() > 0) {

                proteinIdFromGetorf = getValidGFF3SeqId(proteinIdFromGetorf);
                //System.out.println("------- ----oooooo----- ------- proteinIdFromGetorf : " + proteinIdFromGetorf);
                writeSequenceRegionPart(protein, sequenceLength, md5, proteinIdFromGetorf);
                //processMatches(matches, proteinIdForGFF, date, protein, getNucleotideId());
                //System.out.println("------- ----oooooo----- -------  write the matches for " + proteinIdFromGetorf);
                processMatches(matches, proteinIdForGFF, date, protein, proteinIdFromGetorf);
                //System.out.println("------- ----oooooo----- -------");
            }

        }
        return 0;
    }

    /**
     * I.   Iterate over all protein ORFs
     * II.  Get nucleic acids
     * III. Iterate over all nucleic acids xrefs
     * IV.  Iterate over all protein xrefs
     * V.   Compare protein xrefs with nucleic acid xrefs
     * VI.  If they match build a concatenation of nucleic acids separated by pipes
     * VII. If you finished iteration over all nucleic acids xrefs write sequence region to output
     *
     * @param nucleotideSequence
     * @throws IOException
     */
    private void writeSequenceRegionPart(final NucleotideSequence nucleotideSequence) throws IOException {
        // I.
        Utilities.verboseLog(1100, "writeSequenceRegionPart nucleotideSequence: " + nucleotideSequence.getMd5());
        for (OpenReadingFrame orf : nucleotideSequence.getOpenReadingFrames()) {
            Protein protein = orf.getProtein();
            int sequenceLength = protein.getSequenceLength();
            String md5 = protein.getMd5();
            String date = dmyFormat.format(new Date());
            Set<Match> matches = protein.getMatches();
            List<String> proteinIdsFromGetOrf = getProteinAccessions(protein, proteinSequence);
            for (String proteinIdFromGetorf : proteinIdsFromGetOrf) {
                proteinIdFromGetorf = getValidGFF3SeqId(proteinIdFromGetorf);
                if (matches.size() > 0) {
                    // II.
                    final StringBuilder concatenatedNucSeqIdentifiers = new StringBuilder();
                    // III.
                    for (final NucleotideSequenceXref nucleotideSequenceXref : nucleotideSequence.getCrossReferences()) {
                        String nucleotideSequenceXrefId = nucleotideSequenceXref.getIdentifier();
                        // IV.
                        for (ProteinXref proteinXref : protein.getCrossReferences()) {
                            // Getorf appends '_N' where N is an integer to the protein accession. We need to compare this to the nucleotide sequence ID, that does not have _N on the end, so first of all strip this off for the comparison.
                            String strippedProteinId = XrefParser.stripOfFinalUnderScore(proteinXref.getIdentifier());
                            // Get rid of those pesky version numbers too.
                            //strippedProteinId = XrefParser.stripOfVersionNumberIfExists(strippedProteinId);
                            // V.
                            if ((nucleotideSequenceXrefId.equals(strippedProteinId))) {
                                // VI.
                                if (concatenatedNucSeqIdentifiers.length() > 0) {
                                    concatenatedNucSeqIdentifiers.append(VALUE_SEPARATOR);
                                }
                                concatenatedNucSeqIdentifiers.append(nucleotideSequenceXrefId);
                            }
                        }
                    }
                    // VII.
                    String concatenatedNucSeqIdentifiersStr = concatenatedNucSeqIdentifiers.toString();
                    if (concatenatedNucSeqIdentifiersStr.length() > 0) {
                        setNucleotideId(concatenatedNucSeqIdentifiersStr);
                        super.gffWriter.write("##sequence-region " + concatenatedNucSeqIdentifiersStr + " 1 " + nucleotideSequence.getSequence().length());
                        super.gffWriter.write(getNucleicAcidLine(nucleotideSequence));
                        //Build protein identifier for GFF3
                        String proteinIdForGFF = buildProteinIdentifier(orf);
                        proteinIdForGFF = getValidGFF3SeqId(proteinIdForGFF);

                        //Write sequence to the FASTA part
                        addFASTASeqToMap(proteinIdForGFF, protein.getSequence());
                        //Write ORF
                        super.gffWriter.write(getORFLine(orf, proteinIdFromGetorf, proteinIdForGFF, sequenceLength));
                        //Write polypeptide
                        super.gffWriter.write(getPolypeptideLine(sequenceLength, proteinIdFromGetorf, proteinIdForGFF, md5));
                        processMatches(protein.getMatches(), proteinIdForGFF, date, protein, getNucleotideId());
                    } else {
                        throw new IllegalStateException("Cannot find the ORF object that maps to protein with MD5: " + protein.getMd5());
                    }
                }
            }
        }
    }

    /**
     * 1. Handles nucleic acid with multiple cross references (same sequence, different identifiers).
     * Example
     * ##sequence-region Wilf|A2YIW7 1 366
     * Wilf|A2YIW7	provided_by_user	nucleic_acid	1	366	.	+	.	Name=Wilf|A2YIW7;md5=e9b174d63adc63bab79c90fdbc8d1670;ID=Wilf|A2YIW7
     * Wilf|A2YIW7	getorf	ORF	1	366	.	+	.	Name=Wilf_5|A2YIW7_5;Target=pep_Wilf|A2YIW7_1_366 1 122;md5=e9b174d63adc63bab79c90fdbc8d1670;ID=orf_Wilf|A2YIW7_1_366
     * Wilf|A2YIW7	getorf	polypeptide	1	122	.	+	.	md5=f927b0d241297dcc9a1c5990b58bf3c4;ID=pep_Wilf|A2YIW7_1_366
     * <p/>
     * 2. And relations between different nucleic sequences sharing the same orf/protein
     * <p/>
     * I.   Iterate over all protein ORFs
     * II.  Get nucleic acids
     * III. Iterate over all nucleic acids xrefs
     * IV.  Iterate over all protein xrefs
     * V.   Compare protein xrefs with nucleic acid xrefs
     * VI.  If they match build a concatenation of nucleic acids separated by pipes
     * VII. If you finished iteration over all nucleic acids xrefs write sequence region to output
     *
     * @param protein
     * @throws IOException
     */
    private void writeSequenceRegionPart(final Protein protein, final int sequenceLength, final String md5,
                                         final String proteinIdFromGetorf) throws IOException {
        Utilities.verboseLog(1100, "writeSequenceRegionPart protein: " + protein.getMd5() + " proteinIdFromGetorf:" + proteinIdFromGetorf);
        // I.
        for (OpenReadingFrame orf : protein.getOpenReadingFrames()) {
            // II.
            final NucleotideSequence nucleotideSequence = orf.getNucleotideSequence();
            final StringBuilder concatenatedNucSeqIdentifiers = new StringBuilder();
            Set<String> nucSeqIdentifires = new HashSet<>();
            // III.
            for (final NucleotideSequenceXref nucleotideSequenceXref : nucleotideSequence.getCrossReferences()) {
                String nucleotideSequenceXrefId = nucleotideSequenceXref.getIdentifier();
                // IV.
                for (ProteinXref proteinXref : protein.getCrossReferences()) {
                    // Getorf appends '_N' where N is an integer to the protein accession. We need to compare this to the nucleotide sequence ID, that does not have _N on the end, so first of all strip this off for the comparison.
                    // but esl_translate doesnt have the _N feature, dont strip or ignore,
                    //TODO revisit
                    //String strippedProteinId = XrefParser.stripOfFinalUnderScore(proteinXref.getIdentifier());
                    String proteinxrefName = proteinXref.getName();
                    String strippedProteinId = XrefParser.getSource(proteinxrefName);

                    //XrefParser.getSource(proteinIdFromGetorf);
                    Utilities.verboseLog(1100, "strippedProteinId considered: " + strippedProteinId);
                    Utilities.verboseLog(1100, "proteinxrefName considered: " + proteinxrefName);
                    Utilities.verboseLog(1100, "proteinIdFromGetorf considered: " + proteinIdFromGetorf);

                    Utilities.verboseLog(1100, "nucleotideSequenceXrefId considered: " + nucleotideSequenceXrefId);
                    /*
                      Commented-out version number stripping to allow the short-term fix for nucleotide headers to work (IBU-2426)
                      TODO - consider if this is really necessary (may not be a good idea in allcases)
                     */
                    // Get rid of those pesky version numbers too.
                    //strippedProteinId = XrefParser.stripOfVersionNumberIfExists(strippedProteinId);
                    // V.
                    if ((nucleotideSequenceXrefId.equals(strippedProteinId))) {
                        // VIa.
                        nucSeqIdentifires.add(nucleotideSequenceXrefId);
                    }
                }
            }
            // VIb.
            for (String nucSeqId : nucSeqIdentifires){
                if (concatenatedNucSeqIdentifiers.length() > 0) {
                    concatenatedNucSeqIdentifiers.append(VALUE_SEPARATOR);
                }
                concatenatedNucSeqIdentifiers.append(nucSeqId);
            }
            // VII.
            String concatenatedNucSeqIdentifiersStr = concatenatedNucSeqIdentifiers.toString();
            Utilities.verboseLog(1100, "orf considered: " + orf.toString());
            Utilities.verboseLog(1100, "concatenatedNucSeqIdentifiersStr considered: " + concatenatedNucSeqIdentifiersStr);
            if (concatenatedNucSeqIdentifiersStr.length() > 0) {
                setNucleotideId(concatenatedNucSeqIdentifiersStr);
                String nucleotideSequenceId = getNucleotideId();
                Utilities.verboseLog(1100, "nucleotideSequenceId considered: " + nucleotideSequenceId +
                        " proteinIdFromGetorf: " + proteinIdFromGetorf);
                if (proteinIdFromGetorf.startsWith(nucleotideSequenceId)) {
                    Utilities.verboseLog(140,"Matching:" + "\t" + nucleotideSequenceId + "\t" + proteinIdFromGetorf);
                    super.gffWriter.write("##sequence-region " + concatenatedNucSeqIdentifiersStr + " 1 " + nucleotideSequence.getSequence().length());
                    super.gffWriter.write(getNucleicAcidLine(nucleotideSequence));
                    //Build protein identifier for GFF3
                    // eg. Bob_orf01
                    String proteinIdForGFF = buildProteinIdentifier(orf);
                    proteinIdForGFF = getValidGFF3SeqId(proteinIdForGFF);


                    Utilities.verboseLog(1100, "proteinIdForGFF considered: " + proteinIdForGFF);
                    //Write sequence to the FASTA part
                    addFASTASeqToMap(proteinIdForGFF, protein.getSequence());
                    //Write ORF
                    super.gffWriter.write(getORFLine(orf, proteinIdFromGetorf, proteinIdForGFF, sequenceLength));
                    //Write polypeptide
                    super.gffWriter.write(getPolypeptideLine(sequenceLength, proteinIdFromGetorf, proteinIdForGFF, md5));
                } else {
                    Utilities.verboseLog(110,"NOT matching:" + "\t" + nucleotideSequenceId + "\t" + proteinIdFromGetorf);
                }
            } else {
                Utilities.verboseLog(1100, "protein considered: " + protein.toString());
                throw new IllegalStateException("Cannot find the ORF object that maps to protein with MD5: " + protein.getMd5());
            }
        }
    }

    private List<String> getORFLine(OpenReadingFrame orf, String proteinIdFromGetorf, String proteinIdForGFF, int proteinLength) {
        if (orf == null) {
            throw new IllegalArgumentException("A null orf has been passed in.");
        }
        final String seqId = proteinIdFromGetorf;
        final String strand = (NucleotideSequenceStrand.SENSE.equals(orf.getStrand()) ? "+" : "-");
        final String orfIdentifier = buildOrfIdentifier(orf);
        Utilities.verboseLog(1100, "seqId in getORFLine : " + seqId + " proteinIdFromGetorf: " + proteinIdFromGetorf);
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
        return "orf_" + getIdentifierSuffix(orf);
    }

    /**
     * Generates an protein identifier used in GFF3.
     */
    private String buildProteinIdentifier(OpenReadingFrame orf) {
        return "pep_" + getIdentifierSuffix(orf);
    }

    private String getIdentifierSuffix(OpenReadingFrame orf) {
        if (orf == null) {
            LOGGER.warn("Called GFFResultWriterForNucSeqs.getIdentifierSuffix() with a null ORF???");
            return "";
        }
        StringBuilder sb = new StringBuilder(getNucleotideId());
        sb.append("_")
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
    private List<String> getPolypeptideLine(int sequenceLength, String proteinIdFromGetorf, String proteinIdForGFF, String md5) {
        //String seqId = getORFSequenceId(proteinIdFromGetorf);
        String seqId = proteinIdFromGetorf;
        Utilities.verboseLog(1100, "seqId in getPolypeptideLine : " + seqId + " proteinIdFromGetorf: " + proteinIdFromGetorf);
        GFF3Feature polypeptideFeature = new GFF3Feature(seqId, "getorf", "polypeptide", 1, sequenceLength, "+");
        polypeptideFeature.addAttribute(GFF3Feature.ID_ATTR, proteinIdForGFF);
        polypeptideFeature.addAttribute(GFF3Feature.MD5_ATTR, md5);
        return polypeptideFeature.getGFF3FeatureLine();
    }


    String getORFSequenceId(String proteinIdFromGetorf) {
        String nucleotideId = getNucleotideId();
        final String seqId = nucleotideId + "_" + proteinIdFromGetorf;
        return seqId;
    }
}