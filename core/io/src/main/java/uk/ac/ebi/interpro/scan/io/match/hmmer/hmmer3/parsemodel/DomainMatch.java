package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a match for a Domain line in hmmsearch output format.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class DomainMatch implements Serializable {

    /**
     * Group[1] domain #
     * Group[2] weak (?) or strong (!) match (string)
     * Group[3] Score (float)
     * Group[4] Bias (float)
     * Group[5] c-Evalue (float)
     * Group[6] i-Evalue (float)
     * Group[7] hmm from (int)
     * Group[8] hmm to (int)
     * Group[9] hmmbounds, e.g. "[]"
     * Group[10] aliFrom  (int)
     * Group[11] aliTo  (int)
     * Group[12] envFrom  (int)
     * Group[13] envTo   (int)
     * Group[14] acc (float)
     */
    public static final Pattern DOMAIN_LINE_PATTERN = Pattern.compile("^\\s+(\\d+)\\s+([!?])\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+\\S+\\s+(\\d+)\\s+(\\d+)\\s+\\S+\\s+(\\S+).*$");
    //entered by Manjula
    public static final Pattern DOMAIN_ALIGNMENT_LINE_PATTERN = Pattern.compile("^\\s+==\\s+domain\\s+(\\d+)\\s+.*$");

    // TODO: This pattern won't work for UniProt FASTA files because assumes sequence ID contains numbers
    // TODO: and letters only, but UniProt FASTA ID lines contain "|", for example "tr|Q9U4N3|Q9U4N3_TOXGO"
    public static final Pattern ALIGNMENT_SEQUENCE_PATTERN = Pattern.compile("^\\s+(\\w+)\\s+(\\S+)\\s+([-a-zA-Z]+)\\s+(\\S+)\\s*$");

    private final boolean significant;

    private final double score;

    private final double bias;

    private final double cEvalue;

    private final double iEvalue;

    private final int hmmfrom;

    private final int hmmto;

    private final String hmmBounds;

    private final int aliFrom;

    private final int aliTo;

    private final int envFrom;

    private final int envTo;

    private final double acc;

    private String alignment;

    public DomainMatch(Matcher domainLineMatcher) {
        this.significant = domainLineMatcher.group(2).equals("!");
        this.score = Double.parseDouble(domainLineMatcher.group(3));
        this.bias = Double.parseDouble(domainLineMatcher.group(4));
        this.cEvalue = Double.parseDouble(domainLineMatcher.group(5));
        this.iEvalue = Double.parseDouble(domainLineMatcher.group(6));
        this.hmmfrom = Integer.parseInt(domainLineMatcher.group(7));
        this.hmmto = Integer.parseInt(domainLineMatcher.group(8));
        this.hmmBounds = domainLineMatcher.group(9);
        this.aliFrom = Integer.parseInt(domainLineMatcher.group(10));
        this.aliTo = Integer.parseInt(domainLineMatcher.group(11));
        this.envFrom = Integer.parseInt(domainLineMatcher.group(12));
        this.envTo = Integer.parseInt(domainLineMatcher.group(13));
        this.acc = Double.parseDouble(domainLineMatcher.group(14));

    }

    public DomainMatch(SequenceDomainMatch sequenceDomainMatch) {
        this.significant = true;
        this.score = sequenceDomainMatch.getScore();
        this.bias = sequenceDomainMatch.getBias();
        this.cEvalue = sequenceDomainMatch.getCEvalue();
        this.iEvalue = sequenceDomainMatch.getIEvalue();
        this.hmmfrom = sequenceDomainMatch.getHmmfrom();
        this.hmmto = sequenceDomainMatch.getHmmto();
        this.hmmBounds = sequenceDomainMatch.getHmmBounds();
        this.aliFrom = sequenceDomainMatch.getAliFrom();
        this.aliTo = sequenceDomainMatch.getAliTo();
        this.envFrom = sequenceDomainMatch.getEnvFrom();
        this.envTo = sequenceDomainMatch.getEnvTo();
        this.acc = sequenceDomainMatch.getAcc();
    }

    public boolean isSignificant() {
        return significant;
    }

    public String getAlignment() {
        return alignment;
    }

    public double getScore() {
        return score;
    }

    public double getBias() {
        return bias;
    }

    public double getCEvalue() {
        return cEvalue;
    }

    public double getIEvalue() {
        return iEvalue;
    }

    public int getHmmfrom() {
        return hmmfrom;
    }

    public int getHmmto() {
        return hmmto;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    public int getAliFrom() {
        return aliFrom;
    }

    public int getAliTo() {
        return aliTo;
    }

    public int getEnvFrom() {
        return envFrom;
    }

    public int getEnvTo() {
        return envTo;
    }

    public double getAcc() {
        return acc;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    @Override
    public String toString() {
        return "DomainMatch{" +
                (significant ? "!" : "?") +
                "score=" + score +
                ", bias=" + bias +
                ", cEvalue=" + cEvalue +
                ", iEvalue=" + iEvalue +
                ", hmmfrom=" + hmmfrom +
                ", hmmto=" + hmmto +
                ", hmmBounds='" + hmmBounds + '\'' +
                ", aliFrom=" + aliFrom +
                ", aliTo=" + aliTo +
                ", envFrom=" + envFrom +
                ", envTo=" + envTo +
                ", acc=" + acc +
                ", alignment='" + alignment + '\'' +
                '}';
    }
}
