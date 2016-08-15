package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This model object accepts the data parsed from a sequence match line in the hmmsearch output format.
 *
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SequenceMatch implements Serializable {

    /**
     * Group 1: Sequence E-value
     * Group 2: Sequence Score
     * Group 3: Sequence Bias
     * Group 4: UPI
     */
    public static final Pattern SEQUENCE_LINE_PATTERN =
            Pattern.compile("^\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\d+\\s+(\\S+).*$");


    // The following four ints are to help with extracting data from the Pattern above - KEEP THEM IN SYNC!
    public static final int EVALUE_GROUP = 1;
    public static final int SCORE_GROUP = 2;
    public static final int BIAS_GROUP = 3;
    public static final int SEQUENCE_ID_GROUP = 4;

    private String sequenceIdentifier;

    private double eValue;

    private double score;

    private double bias;

    private List<DomainMatch> domainMatches = new ArrayList<DomainMatch>();

    public SequenceMatch(Matcher domainLineMatcher) {
        this.eValue = Double.parseDouble(domainLineMatcher.group(EVALUE_GROUP));
        this.score = Double.parseDouble(domainLineMatcher.group(SCORE_GROUP));
        this.bias = Double.parseDouble(domainLineMatcher.group(BIAS_GROUP));
        this.sequenceIdentifier = domainLineMatcher.group(SEQUENCE_ID_GROUP);

    }
    public SequenceMatch(String  sequenceIdentifier, double eValue, double score, double bias) {
        this.sequenceIdentifier = sequenceIdentifier;
        this.eValue = eValue;
        this.score = score;
        this.bias = bias;
    }

    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    public double getEValue() {
        return eValue;
    }

    public double getScore() {
        return score;
    }

    public double getBias() {
        return bias;
    }

    void addDomainMatch(DomainMatch domainMatch) {
        this.domainMatches.add(domainMatch);
    }

    void removeDomainMatch(DomainMatch domainMatch) {
        this.domainMatches.remove(domainMatch);
    }

    public List<DomainMatch> getDomainMatches() {
        return domainMatches;
    }

    @Override
    public String toString() {
        return "SequenceMatch{" +
                "sequenceIdentifier='" + sequenceIdentifier + '\'' +
                ", eValue=" + eValue +
                ", score=" + score +
                ", bias=" + bias +
                ", domainMatches=" + domainMatches +
                '}';
    }
}
