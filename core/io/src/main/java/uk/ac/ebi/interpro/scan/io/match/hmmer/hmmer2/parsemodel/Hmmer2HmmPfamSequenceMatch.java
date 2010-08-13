package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This model object accepts the data parsed from a sequence match line in the hmmpfam output format.
 *
 * @author Phil Jones
 * @version $Id: Hmmer2HmmPfamSequenceMatch.java 558 2010-04-22 10:24:53Z aquinn.ebi $
 * @since 1.0-SNAPSHOT
 */
public class Hmmer2HmmPfamSequenceMatch implements Serializable {

    /**
     * Scores for sequence family classification (score includes all domains):
     * Model           Description                             Score    E-value  N
     * --------        -----------                             -----    ------- ---
     * TIGR03593       yidC_nterm: membrane protein insertas   513.0   1.4e-151   1
     * TIGR03592       yidC_oxa1_cterm: membrane protein ins   398.7   3.5e-117   1
     * <p/>
     * Group 1: Model Accession
     * Group 2: Sequence Score
     * Group 3: Sequence E-value
     * Group 4: N
     */
    public static final Pattern SEQUENCE_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+.+\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)$"
    );


    // The following four ints are to help with extracting data from the Pattern above - KEEP THEM IN SYNC!
    public static final int MODEL_ACCESSION_GROUP = 1;
    public static final int SCORE_GROUP = 2;
    public static final int EVALUE_GROUP = 3;
    public static final int N_GROUP = 4;

    private String modelAccession;
    private double eValue;
    private long n;
    private double sequenceScore;


    private List<Hmmer2HmmPfamDomainMatch> hmmer2DomainMatches = new ArrayList<Hmmer2HmmPfamDomainMatch>();

    public Hmmer2HmmPfamSequenceMatch(Matcher domainLineMatcher) {
        this.eValue = Double.parseDouble(domainLineMatcher.group(EVALUE_GROUP));
        this.sequenceScore = Double.parseDouble(domainLineMatcher.group(SCORE_GROUP));
        this.modelAccession = domainLineMatcher.group(MODEL_ACCESSION_GROUP);
        this.n = Long.parseLong(domainLineMatcher.group(N_GROUP));
    }

    public String getModelAccession() {
        return modelAccession;
    }

    public double getEValue() {
        return eValue;
    }

    public double getSequenceScore() {
        return sequenceScore;
    }

    public long getN() {
        return n;
    }

    void addDomainMatch(Hmmer2HmmPfamDomainMatch hmmer2DomainMatch) {
        this.hmmer2DomainMatches.add(hmmer2DomainMatch);
    }

    void removeDomainMatch(Hmmer2HmmPfamDomainMatch hmmer2DomainMatch) {
        this.hmmer2DomainMatches.remove(hmmer2DomainMatch);
    }

    public List<Hmmer2HmmPfamDomainMatch> getDomainMatches() {
        return hmmer2DomainMatches;
    }


}
