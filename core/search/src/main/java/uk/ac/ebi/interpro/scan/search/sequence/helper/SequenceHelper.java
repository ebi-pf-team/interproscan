package uk.ac.ebi.interpro.scan.search.sequence.helper;

import java.util.regex.Pattern;

/**
 * Utilities for dealing with sequences.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class SequenceHelper {

    /**
     * Any letter of the alphabet allowed, in UniParc at least:
     *
     * (1) Following are allowed in addition to the 20 standard amino acids:
     *     Selenocysteine	                    U
     *     Pyrrolysine	                        O
     *
     * (2) Placeholders are used where chemical or crystallographic analysis of a peptide or protein
     *     cannot conclusively determine the identity of a residue:
     *     Asparagine or aspartic acid		    B
     *     Glutamine or glutamic acid		    Z
     *     Leucine or Isoleucine		        J
     *     Unspecified or unknown amino acid	X
     */
    private static final String AMINO_ACID = "^[A-Z]";

    // If we want to check if a string is a sequence, the string must be at least 30 characters long
    // (estimated from http://en.wikipedia.org/wiki/Longest_word_in_English)
    private static final String MIN_PROTEIN_LENGTH = "{30,}";

    // Match anything
    private static final String ANY_CHAR   = ".*";

    // Line breaks are not significant (http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#DOTALL)
    private static final Pattern MIN_LEN_PROTEIN_PATTERN = Pattern.compile(AMINO_ACID + MIN_PROTEIN_LENGTH + ANY_CHAR, Pattern.DOTALL);

    private static final Pattern FASTA_HEADER_PATTERN = Pattern.compile(">.+\\s*", Pattern.MULTILINE);

    private static final Pattern WHITESPACE_PATTERN   = Pattern.compile("\\s+");

    // Not instantiable
    private SequenceHelper() {
    }

    public static boolean isProteinSequence(String query) {
        // Remove header line if present
        String s = FASTA_HEADER_PATTERN.matcher(query).replaceAll("");
        return (MIN_LEN_PROTEIN_PATTERN.matcher(s).matches());
    }

    /**
     * Returns MD5 for sequence
     *
     * @param sequence Protein sequence
     * @return MD5
     */
    public static String calculateMd5(String sequence) {
        return Md5Helper.calculateMd5(normaliseSequence(sequence));
    }

    /**
     * Returns sequence without whitespace or FASTA header.
     *
     * @param sequence Protein sequence, optionally in FASTA format
     * @return Sequence without whitespace or FASTA header.
     */
    public static String normaliseSequence(String sequence) {
        // Remove header line
        sequence = FASTA_HEADER_PATTERN.matcher(sequence).replaceAll("");
        // Remove whitespace
        sequence = WHITESPACE_PATTERN.matcher(sequence).replaceAll("");
        return sequence;
    }

}
