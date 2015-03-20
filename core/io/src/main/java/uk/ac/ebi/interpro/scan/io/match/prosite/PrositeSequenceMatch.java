package uk.ac.ebi.interpro.scan.io.match.prosite;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This model object accepts the data parsed from a sequence match line in the pfsearch output format. *
 *
 * @author Gift Nuka
 *
 *
 */
public class PrositeSequenceMatch implements Serializable {

    /**
     * Group 1:
     * Group 2:
     * Group 3:
     * Group 4:
     */
    public static final Pattern SEQUENCE_LINE_PATTERN =
            Pattern.compile("^>(\\S+)\\/(\\d+)\\-(\\d+)\\s+\\S+=(\\S+)\\|\\S+\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+(.*)");
            //TODO  make this pattern simpler, sub groups?

//            Pattern.compile("^>(\\S+)\\/(\\d+)\\-(\\d+)\\s+\\S+=(\\S+)\\|\\S+\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+(.*)");

//            Pattern.compile("^\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\d+\\s+(\\S+).*$");



    // The following  ints are to help with extracting data from the Pattern above - KEEP THEM IN SYNC!
    public static final int SEQ_ID_GROUP = 1;
    public static final int SEQ_MATCH_START_GROUP = 2;
    public static final int SEQ_MATCH_END_GROUP = 3;
    public static final int MODEL_GROUP = 4;
    public static final int NORM_SCORE_GROUP = 5;
    public static final int RAW_SCORE_GROUP = 6;
    public static final int LEVEL_GROUP = 7;
    public static final int SEQ_END_GROUP = 8;
    public static final int MOTOF_START_GROUP = 9;
    public static final int MOTIF_END_GROUP = 10;
    public static final int ALIGNMENT_GROUP = 11;

    private String sequenceIdentifier;

    private int sequenceStart;

    private int sequenceEnd;

    private String model;

    private double score;

    private int level;

    private String alignment;

    private List<DomainMatch> domainMatches = new ArrayList<DomainMatch>();

    public PrositeSequenceMatch(Matcher domainLineMatcher) {
        this.sequenceIdentifier = domainLineMatcher.group(SEQ_ID_GROUP);
        this.sequenceStart = Integer.parseInt(domainLineMatcher.group(SEQ_MATCH_START_GROUP));
        this.sequenceEnd = Integer.parseInt(domainLineMatcher.group(SEQ_MATCH_END_GROUP));
        this.model = domainLineMatcher.group(MODEL_GROUP);
        this.score = Double.parseDouble(domainLineMatcher.group(NORM_SCORE_GROUP));
        this.level = Integer.parseInt(domainLineMatcher.group(LEVEL_GROUP));
        this.alignment = domainLineMatcher.group(ALIGNMENT_GROUP);
    }

    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    public void setSequenceIdentifier(String sequenceIdentifier) {
        this.sequenceIdentifier = sequenceIdentifier;
    }

    public int getSequenceStart() {
        return sequenceStart;
    }

    public void setSequenceStart(int sequenceStart) {
        this.sequenceStart = sequenceStart;
    }

    public int getSequenceEnd() {
        return sequenceEnd;
    }

    public void setSequenceEnd(int sequenceEnd) {
        this.sequenceEnd = sequenceEnd;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }
}
