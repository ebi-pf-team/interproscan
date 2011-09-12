package uk.ac.ebi.interpro.scan.io.gene3d;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.alignment.CigarAlignmentEncoder;

/**
 * Represents a record in DomainFinder3's SSF format:
 * <pre>
 * FILE FORMAT: Sam Summary File (SSF) Format 2.1
 * Column 1: Sequence accession code e.g. gi|159385
 * Column 2: HMM model name (not taken from stat or align file; set externally by user)
 * Column 3: Total sequence length
 * Column 4: Total HMM length
 * Column 5: Length of alignment (upper case letters AND gaps that appear within segments)
 * Column 6: Number of matched residues (upper case letters)
 * Column 7: First residue in sequence to match model (first upper case letter)
 * Column 8: Last residue in sequence to match model (last upper case letter)
 * Column 9: First matched position in model
 * Column 10: Last matched position in model
 * Column 11: Domain i-evalue
 * Column 12: Simple score
 * Column 13: Reverse score
 * Column 14: Number of matched sequence segments (2+: discontinuous match)
 * Column 15: Colon-separated segment boundaries e.g. 1:51:99:163
 * </pre>
 * For example:
 * <pre>
 * pdb|101mA 3_2_0:2z8aA00 154 0 146 136 1 143 1 146 2.9e-28 97.00 0.00 1 1:143
 * </pre>
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class DomainFinderRecord {

    public static final String SEGMENT_BOUNDARY_SEPARATOR = ":";

    private static final String COLUMN_SEP = "\t";

    private static final int SEQUENCE_ID_POS = 0;
    private static final int MODEL_ID_POS = 1;
    private static final int SEQUENCE_LENGTH_POS = 2;
    private static final int MODEL_LENGTH_POS = 3;
    private static final int ALIGNMENT_LENGTH_POS = 4;
    private static final int MATCHED_RESIDUE_COUNT_POS = 5;
    private static final int SEQUENCE_START_POS = 6;
    private static final int SEQUENCE_END_POS = 7;
    private static final int MODEL_START_POS = 8;
    private static final int MODEL_END_POS = 9;
    private static final int DOMAIN_I_EVALUE_POS = 10;
    private static final int SCORE_POS = 11;
    private static final int REVERSE_SCORE_POS = 12;
    private static final int MATCHED_SEQUENCE_COUNT_POS = 13;
    private static final int SEGMENT_BOUNDARIES_POS = 14;
    private static final int LAST_POS = SEGMENT_BOUNDARIES_POS;

    // TODO: Find out if we really need these or not
    private static final Integer DEFAULT_SEQUENCE_LENGTH = 0;
    private static final Integer DEFAULT_MATCHED_RESIDUE_COUNT = 0;
    private static final Integer DEFAULT_MODEL_LENGTH = 0;
    private static final Integer DEFAULT_ALIGNMENT_LENGTH = 0;
    private static final Double DEFAULT_REVERSE_SCORE = 0.0;

    private final String sequenceId;
    private final String modelId;
    private final Integer sequenceLength;
    private final Integer modelLength;
    private final Integer alignmentLength;
    private final Integer matchedResidueCount;
    private final Integer sequenceStart;
    private final Integer sequenceEnd;
    private final Integer modelStart;
    private final Integer modelEnd;
    private final Double domainIeValue;
    private final Double score;
    private final Double reverseScore;
    private final Integer matchedSequenceCount;
    private final String segmentBoundaries;

    // NOTE: Sequence e-value is not stored in SSF file, but is required for ordering of SSF file
    private final Double sequenceEvalue;

    private DomainFinderRecord() {
        this.sequenceId = null;
        this.modelId = null;
        this.sequenceLength = null;
        this.modelLength = null;
        this.alignmentLength = null;
        this.matchedResidueCount = null;
        this.sequenceStart = null;
        this.sequenceEnd = null;
        this.modelStart = null;
        this.modelEnd = null;
        this.domainIeValue = null;
        this.score = null;
        this.reverseScore = null;
        this.matchedSequenceCount = null;
        this.segmentBoundaries = null;
        this.sequenceEvalue = null;
    }

    public DomainFinderRecord(String sequenceId, String modelId,
                              Integer sequenceStart, Integer sequenceEnd,
                              Integer modelStart, Integer modelEnd,
                              Double domainIeValue, Double score, String cigarAlignment,
                              Double sequenceEvalue) {
        SegmentRecord segmentRecord = getSegmentAndBoundaries(cigarAlignment, sequenceStart);
        this.sequenceId = sequenceId;
        this.modelId = modelId;
        this.sequenceLength = DEFAULT_SEQUENCE_LENGTH;
        this.modelLength = DEFAULT_MODEL_LENGTH;
        this.alignmentLength = DEFAULT_ALIGNMENT_LENGTH;
        this.matchedResidueCount = DEFAULT_MATCHED_RESIDUE_COUNT;
        this.sequenceStart = sequenceStart;
        this.sequenceEnd = sequenceEnd;
        this.modelStart = modelStart;
        this.modelEnd = modelEnd;
        this.domainIeValue = domainIeValue;
        this.score = score;
        this.reverseScore = DEFAULT_REVERSE_SCORE;
        this.matchedSequenceCount = segmentRecord.getMatchedSequenceCount();
        this.segmentBoundaries = segmentRecord.getSegmentBoundaries();
        this.sequenceEvalue = sequenceEvalue;
    }

    DomainFinderRecord(String sequenceId, String modelId,
                       Integer sequenceLength, Integer modelLength,
                       Integer alignmentLength, Integer matchedResidueCount,
                       Integer sequenceStart, Integer sequenceEnd,
                       Integer modelStart, Integer modelEnd,
                       Double domainIeValue, Double score, Double reverseScore,
                       Integer matchedSequenceCount, String segmentBoundaries, Double sequenceEvalue) {
        this.sequenceId = sequenceId;
        this.modelId = modelId;
        this.sequenceLength = sequenceLength;
        this.modelLength = modelLength;
        this.alignmentLength = alignmentLength;
        this.matchedResidueCount = matchedResidueCount;
        this.sequenceStart = sequenceStart;
        this.sequenceEnd = sequenceEnd;
        this.modelStart = modelStart;
        this.modelEnd = modelEnd;
        this.domainIeValue = domainIeValue;
        this.score = score;
        this.reverseScore = reverseScore;
        this.matchedSequenceCount = matchedSequenceCount;
        this.segmentBoundaries = segmentBoundaries;
        this.sequenceEvalue = sequenceEvalue;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public String getModelId() {
        return modelId;
    }

    public Integer getSequenceLength() {
        return sequenceLength;
    }

    public Integer getModelLength() {
        return modelLength;
    }

    public Integer getAlignmentLength() {
        return alignmentLength;
    }

    public Integer getMatchedResidueCount() {
        return matchedResidueCount;
    }

    public Integer getSequenceStart() {
        return sequenceStart;
    }

    public Integer getSequenceEnd() {
        return sequenceEnd;
    }

    public Integer getModelStart() {
        return modelStart;
    }

    public Integer getModelEnd() {
        return modelEnd;
    }

    public Double getDomainIeValue() {
        return domainIeValue;
    }

    public Double getScore() {
        return score;
    }

    public Double getReverseScore() {
        return reverseScore;
    }

    public Integer getMatchedSequenceCount() {
        return matchedSequenceCount;
    }

    public String getSegmentBoundaries() {
        return segmentBoundaries;
    }

    public Double getSequenceEvalue() {
        return sequenceEvalue;
    }

    public static DomainFinderRecord valueOf(Gene3dHmmer3RawMatch rawMatch) {
        if (rawMatch == null) {
            throw new NullPointerException("RawMatch object is null");
        }
        return new DomainFinderRecord(rawMatch.getSequenceIdentifier(), rawMatch.getModelId(),
                rawMatch.getLocationStart(), rawMatch.getLocationEnd(),
                rawMatch.getHmmStart(), rawMatch.getHmmEnd(),
                rawMatch.getDomainIeValue(), rawMatch.getLocationScore(),
                rawMatch.getCigarAlignment(), rawMatch.getEvalue());

    }

    public static DomainFinderRecord valueOf(String line) {
        String[] columns = line.split(COLUMN_SEP);
        String sequenceId = columns[SEQUENCE_ID_POS];
        String modelId = columns[MODEL_ID_POS];
        Integer sequenceLength = Integer.parseInt(columns[SEQUENCE_LENGTH_POS]);
        Integer modelLength = Integer.parseInt(columns[MODEL_LENGTH_POS]);
        Integer alignmentLength = Integer.parseInt(columns[ALIGNMENT_LENGTH_POS]);
        Integer matchedResidueCount = Integer.parseInt(columns[MATCHED_RESIDUE_COUNT_POS]);
        Integer sequenceStart = Integer.parseInt(columns[SEQUENCE_START_POS]);
        Integer sequenceEnd = Integer.parseInt(columns[SEQUENCE_END_POS]);
        Integer modelStart = Integer.parseInt(columns[MODEL_START_POS]);
        Integer modelEnd = Integer.parseInt(columns[MODEL_END_POS]);
        Double domainIeValue = Double.parseDouble(columns[DOMAIN_I_EVALUE_POS]);
        Double score = Double.parseDouble(columns[SCORE_POS]);
        Double reverseScore = Double.parseDouble(columns[REVERSE_SCORE_POS]);
        Integer matchedSequenceCount = Integer.parseInt(columns[MATCHED_SEQUENCE_COUNT_POS]);
        String segmentBoundaries = columns[SEGMENT_BOUNDARIES_POS];
        return new DomainFinderRecord(sequenceId, modelId,
                sequenceLength, modelLength,
                alignmentLength, matchedResidueCount,
                sequenceStart, sequenceEnd,
                modelStart, modelEnd,
                domainIeValue, score, reverseScore,
                matchedSequenceCount, segmentBoundaries, null);
    }

    public static String toLine(DomainFinderRecord record) {
        String[] columns = new String[LAST_POS + 1];
        columns[SEQUENCE_ID_POS] = record.sequenceId;
        columns[MODEL_ID_POS] = record.modelId;
        columns[SEQUENCE_LENGTH_POS] = String.valueOf(record.sequenceLength);
        columns[MODEL_LENGTH_POS] = String.valueOf(record.modelLength);
        columns[ALIGNMENT_LENGTH_POS] = String.valueOf(record.alignmentLength);
        columns[MATCHED_RESIDUE_COUNT_POS] = String.valueOf(record.matchedResidueCount);
        columns[SEQUENCE_START_POS] = String.valueOf(record.sequenceStart);
        columns[SEQUENCE_END_POS] = String.valueOf(record.sequenceEnd);
        columns[MODEL_START_POS] = String.valueOf(record.modelStart);
        columns[MODEL_END_POS] = String.valueOf(record.modelEnd);
        String eValString = String.valueOf(record.domainIeValue);
        // Because evalues are stored as log10 values, 0 ends up as -Infinity...
        if ("-Infinity".equalsIgnoreCase(eValString)) {
            eValString = "0.0";
        }
        columns[DOMAIN_I_EVALUE_POS] = eValString;
        columns[SCORE_POS] = String.valueOf(record.score);
        columns[REVERSE_SCORE_POS] = String.valueOf(record.reverseScore);
        columns[MATCHED_SEQUENCE_COUNT_POS] = String.valueOf(record.matchedSequenceCount);
        columns[SEGMENT_BOUNDARIES_POS] = String.valueOf(record.segmentBoundaries);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            builder.append(columns[i]);
            if (i < LAST_POS) {
                builder.append(COLUMN_SEP);
            }
        }
        return builder.toString();
    }


    // TODO: Sort out Manjula code below -- IntelliJ complains that it's "too complex to analyze"
    /*
    * The algorithm for splitting a domain into segments is:
    *(1) Get the string of the sequence as aligned to the model, for example 'AIHNMPGMAFRTGAamasvalqtivareYPVLV'
    *(2) Step through the sequence residue-by-residue
    *(3) If a lower case letter is reached open a gap;
    *    if an upper case letter is reached close a gap.
    *    '.'s and '-'s are ignored, i.e. they don't open or close gaps and are ignored when calculating the gap length
    *(4) At the point of closing a gap, if the gap was > 30 residues then the sequence is split into
    *    two segments around it. So the end of the first segment is the last residue before the gap opens,
    *    while the beginning of the second is the first after the gap closes.
    *(5) Continue on down the sequence ...
     */

    public static SegmentRecord getSegmentAndBoundaries(String cigarAlignment, int sequenceStart) {

        int residueLength = sequenceStart;
        int startOfMatch = 0, endOfMatch = 0;
        int segmentCounter = 1;
        int insertCounter = 0;
        int residueCounter;
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();

        for (char c : cigarAlignment.toCharArray()) {

            if (Character.isUpperCase(c)) {
                residueCounter = Integer.parseInt(sb1.toString());
                sb1.append(c);
                sb1.setLength(0);
                residueLength += residueCounter;

                switch (c) {

                    case CigarAlignmentEncoder.MATCH_CHAR:
                        endOfMatch = residueLength;
                        insertCounter = 0;
                        if (startOfMatch == 0)
                            startOfMatch = residueLength - residueCounter;  //to have a good start of match always
                        break;

                    case CigarAlignmentEncoder.INSERT_CHAR:
                        insertCounter += residueCounter;  //this is to handle two insert segment followed by each other
                        if (insertCounter > 30 && endOfMatch > startOfMatch) {
                            sb.append(startOfMatch).append(SEGMENT_BOUNDARY_SEPARATOR).append(endOfMatch - 1).append(SEGMENT_BOUNDARY_SEPARATOR);
                            startOfMatch = residueLength;
                            segmentCounter++;
                        }
                        break;

                    case CigarAlignmentEncoder.DELETE_CHAR:
                        residueLength -= residueCounter;
                        break;
                }
            } else {
                sb1.append(c);
            }

        } //end of for
        if (endOfMatch > startOfMatch) {
            sb.append(startOfMatch).append(SEGMENT_BOUNDARY_SEPARATOR).append(endOfMatch - 1).append(SEGMENT_BOUNDARY_SEPARATOR);  //not to missout any trailing segment
        }
        String segmentBoundaries = sb.toString().substring(0, sb.toString().length() - 1);
        return new SegmentRecord(segmentCounter, segmentBoundaries);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DomainFinderRecord))
            return false;
        final DomainFinderRecord r = (DomainFinderRecord) o;
        return new EqualsBuilder()
                .append(sequenceId, r.sequenceId)
                .append(modelId, r.modelId)
                .append(sequenceLength, r.sequenceLength)
                .append(modelLength, r.modelLength)
                .append(alignmentLength, r.alignmentLength)
                .append(matchedResidueCount, r.matchedResidueCount)
                .append(sequenceStart, r.sequenceStart)
                .append(sequenceEnd, r.sequenceEnd)
                .append(modelStart, r.modelStart)
                .append(modelEnd, r.modelEnd)
                .append(score, r.score)
                .append(reverseScore, r.reverseScore)
                .append(matchedSequenceCount, r.matchedSequenceCount)
                .append(segmentBoundaries, r.segmentBoundaries)
                .isEquals()
                &&
                // Allow for some imprecision in evalues
                PersistenceConversion.equivalent(domainIeValue, r.domainIeValue);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 97)
                .append(sequenceId)
                .append(modelId)
                .append(sequenceLength)
                .append(modelLength)
                .append(alignmentLength)
                .append(matchedResidueCount)
                .append(sequenceStart)
                .append(sequenceEnd)
                .append(modelStart)
                .append(modelEnd)
                .append(domainIeValue)
                .append(score)
                .append(reverseScore)
                .append(matchedSequenceCount)
                .append(segmentBoundaries)
                .toHashCode();
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static final class SegmentRecord {

        private final Integer matchedSequenceCount;
        private final String segmentBoundaries;

        public SegmentRecord(Integer matchedSequenceCount, String segmentBoundaries) {
            this.matchedSequenceCount = matchedSequenceCount;
            this.segmentBoundaries = segmentBoundaries;
        }

        public Integer getMatchedSequenceCount() {
            return matchedSequenceCount;
        }

        public String getSegmentBoundaries() {
            return segmentBoundaries;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof SegmentRecord))
                return false;
            final SegmentRecord r = (SegmentRecord) o;
            return new EqualsBuilder()
                    .append(matchedSequenceCount, r.matchedSequenceCount)
                    .append(segmentBoundaries, r.segmentBoundaries)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 99)
                    .append(matchedSequenceCount)
                    .append(segmentBoundaries)
                    .toHashCode();
        }


        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }


    }

}
