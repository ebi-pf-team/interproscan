package uk.ac.ebi.interpro.scan.model.raw.alignment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@inheritDoc}
 * <p/>
 * <p>This implementation encodes alignments using the
 * <a href="http://www.ensembl.org/Help/Results?_referer=;result=glossary_13">CIGAR format</a>, a simple
 * type of data compression based on
 * <a href="http://en.wikipedia.org/wiki/Run-length_encoding">run-length encoding</a>.
 * <p/>
 * <p>CIGAR alignments can be found at SIB's ProSite DAS server, for example:
 * <a href="http://proserver.vital-it.ch/das/prositealign/alignment?query=PS50808">PS50808</a>.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class CigarAlignmentEncoder implements AlignmentEncoder {

    public static final char MATCH_CHAR = 'M';
    public static final char INSERT_CHAR = 'I';
    public static final char DELETE_CHAR = 'D';

    private static final String MATCH_STR = Character.toString(MATCH_CHAR);
    private static final String INSERT_STR = Character.toString(INSERT_CHAR);
    private static final String DELETE_STR = Character.toString(DELETE_CHAR);

    private static final char DELETE_SYMBOL = '-';

    public CigarAlignmentEncoder() {
    }

    /**
     * Returns alignment in CIGAR format. For example, "QEFHRK-----KDgnfGAD" is encoded as "6M5D2M3I3M".
     * <p/>
     * From <a href="http://www.cs.bris.ac.uk/~gough/book/">Hidden Markov models</a>:
     * <ul>
     * <li>Upper case letters are aligned to a segment of the model</li>
     * <li>Lower case letters are not aligned</li>
     * <li>Deletions with respect to the model are marked by the '-' character</li>
     * <ul>
     *
     * @param alignment Sequence alignment
     * @return Alignment in CIGAR format
     */
    @Override
    public String encode(String alignment) {
        if (alignment == null) {
            throw new NullPointerException("Alignment must not be null");
        }
        if (alignment.length() == 0) {
            throw new IllegalArgumentException("Alignment must not be empty");
        }
        StringBuilder sb = new StringBuilder();
        for (char c : alignment.toCharArray()) {
            String s;
            if (Character.isUpperCase(c)) {
                s = MATCH_STR;
            } else if (Character.isLowerCase(c)) {
                s = INSERT_STR;
            } else if (c == DELETE_SYMBOL) {
                s = DELETE_STR;
            } else if (c == '.') {
                s = null;
            } else {
                throw new IllegalArgumentException("Alignment contains unrecognised characters " +
                        "(must contain letters or " + String.valueOf(DELETE_SYMBOL) + "): " + alignment);
            }
            if (s != null) {
                sb.append(s);
            }
        }
        // Encode
        return RunLengthEncoding.encode(sb.toString());
    }

    /**
     * Returns decoded alignment.
     * <p/>
     * For example, "6M5D2M3I3M" is decoded as "QEFHRK-----KDgnfGAD", given the
     * sequence "QEFHRKPQPPPKDGNFGAD".
     * <p/>
     * From <a href="http://www.cs.bris.ac.uk/~gough/book/">Hidden Markov models</a>:
     * <ul>
     * <li>Upper case letters are aligned to a segment of the model</li>
     * <li>Lower case letters are not aligned</li>
     * <li>Deletions with respect to the model are marked by the '-' character</li>
     * <ul>
     *
     * @param sequence         Protein sequence
     * @param encodedAlignment CIGAR-encoded alignment
     * @param start            Start position of alignment on sequence
     * @param end              End position of alignment on sequence
     * @return Decoded alignment
     */
    @Override
    public String decode(String sequence, String encodedAlignment, int start, int end) {
        if (sequence == null) {
            throw new NullPointerException("Sequence must not be null");
        }
        if (encodedAlignment == null) {
            throw new NullPointerException("Alignment must not be null");
        }
        if (start < 1) {
            throw new IllegalArgumentException("Start position must be 1 or greater");
        }
        if (end < 1) {
            throw new IllegalArgumentException("Stop position must be 1 or greater");
        }
        if (start > end) {
            throw new IllegalArgumentException("Start position must be equal to or less than stop position");
        }
        // Extract the sequence region to which the alignment applies (use -1 because indexes in Java start from 0)
        String sequenceRegion = sequence.substring(start - 1, end);
        return decode(sequenceRegion, encodedAlignment);
    }

    private String decode(String sequence, String encodedAlignment) {
        // Handle "Not available" for alignments returned from Onion pre-calculated matches.
        if ("Not available".equals(encodedAlignment)) {
            return encodedAlignment;
        }
        // Expand, for example convert "4M2D" to "MMMMDD"
        String expandedAlignment = RunLengthEncoding.decode(encodedAlignment);
        final boolean deletionMarkersIncludedInSequence = expandedAlignment.length() == sequence.length();
        StringBuffer alignment = new StringBuffer();
        char[] alignmentArray = expandedAlignment.toCharArray();
        char[] sequenceArray = sequence.toCharArray();
        int deletionCount = 0;
        for (int i = 0; i < alignmentArray.length; i++) {
            switch (alignmentArray[i]) {
                case MATCH_CHAR: {
                    alignment.append(Character.toUpperCase(sequenceArray[i - deletionCount]));
                    break;
                }
                case INSERT_CHAR: {
                    alignment.append(Character.toLowerCase(sequenceArray[i - deletionCount]));
                    break;
                }
                case DELETE_CHAR: {
                    alignment.append(DELETE_SYMBOL);
                    if (!deletionMarkersIncludedInSequence) {
                        deletionCount++;
                    }
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Alignment contains unrecognised characters " +
                            "(can only contain " + MATCH_STR + ", " + INSERT_STR + " and " + DELETE_STR + "): " + alignment);
                }
            }
        }
        return alignment.toString();
    }

    /**
     * Provides information about the CIGAR-formatted alignment
     *
     * @author Antony Quinn
     */
    public final static class Parser implements AlignmentEncoder.Parser {

        private final int matchCount;
        private final int insertCount;
        private final int deleteCount;

        private Parser() {
            matchCount = 0;
            insertCount = 0;
            deleteCount = 0;
        }

        /**
         * Calculates number of matches, inserts and deletes from CIGAR-encoded alignment.
         *
         * @param encoding CIGAR-encoded alignment
         */
        public Parser(String encoding) {

            int mc = 0, ic = 0, dc = 0;
            // Decode, for example convert "4M2D" to "MMMMDD"
            char[] decoded = RunLengthEncoding.decode(encoding).toCharArray();
            // Count number of match, insert and delete characters
            for (char c : decoded) {
                switch (c) {
                    case MATCH_CHAR: {
                        mc++;
                        break;
                    }
                    case INSERT_CHAR: {
                        ic++;
                        break;
                    }
                    case DELETE_CHAR: {
                        dc++;
                        break;
                    }
                }
            }
            matchCount = mc;
            insertCount = ic;
            deleteCount = dc;
        }

        /**
         * Returns number of matches, for example "6M5D2M3I3M" returns 11 (6M + 2M + 3M)
         *
         * @return Number of matches
         */
        public int getMatchCount() {
            return matchCount;
        }

        /**
         * Returns number of inserts, for example "6M5D2M3I3M" returns 3 (3I)
         *
         * @return Number of inserts
         */
        public int getInsertCount() {
            return insertCount;
        }

        /**
         * Returns number of deletes, for example "6M5D2M3I3M" returns 5 (5D)
         *
         * @return Number of deletes
         */
        public int getDeleteCount() {
            return deleteCount;
        }

    }


    /**
     * Run-length encoding (RLE) is a very simple form of data compression in
     * which runs of data (that is, sequences in which the same data value occurs in many consecutive data elements)
     * are stored as a single data value and count, rather than as the original run.
     * For further information see <a href="http://en.wikipedia.org/wiki/Run-length_encoding">Run-length_encoding</a>.
     */
    private final static class RunLengthEncoding {

        private static final Pattern ENCODED_PATTERN = Pattern.compile("[0-9]+|[a-zA-Z]");

        /**
         * Returns encoded string. For example, converts "WWWWWWWWWWWWBWWWWWWWWWWWWBBB" to "12W1B12W3B"
         *
         * @param source String to encode.
         * @return Encoded string
         */
        public static String encode(String source) {
            StringBuffer dest = new StringBuffer();
            int sourceLen = source.length();
            for (int i = 0; i < sourceLen; i++) {
                int runLength = 1;
                final char currentChar = source.charAt(i);
                while (i + 1 < sourceLen && currentChar == source.charAt(i + 1)) {
                    runLength++;
                    i++;
                }
                dest.append(runLength);
                dest.append(currentChar);
            }
            return dest.toString();
        }

        /**
         * Returns decoded string, for example converts "12W1B12W3B" to "WWWWWWWWWWWWBWWWWWWWWWWWWBBB"
         *
         * @param source String to decode
         * @return Decoded string
         */
        public static String decode(String source) {
            StringBuffer dest = new StringBuffer();
            Matcher matcher = ENCODED_PATTERN.matcher(source);
            while (matcher.find()) {
                int number;
                try {
                    number = Integer.parseInt(matcher.group());
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Unable to decode the cigar string '" + source + "'.  Attempting to convert the substring '" + matcher.group() + "' to a number.  Thrown a NumberFormatException (appended).", nfe);
                }
                matcher.find();
                while (number-- != 0) {
                    dest.append(matcher.group());
                }
            }
            return dest.toString();
        }

    }


}
