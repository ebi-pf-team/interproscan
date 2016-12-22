package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a match for a Domain line in hmmsearch domtbloutput format.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class DomTblDomainMatch implements Serializable {

    /**
     * Group[1] Score (float)
     * Group[2] Bias (float)
     * Group[3] c-Evalue (float)
     * Group[4] i-Evalue (float)
     * Group[5] hmm from (int)
     * Group[6] hmm to (int)
     * Group[7] hmmbounds, e.g. "[]"
     * Group[8] aliFrom  (int)
     * Group[9] aliTo  (int)
     * Group[10] envFrom  (int)
     * Group[11] envTo   (int)
     * Group[12] acc (float)
     */
    //public static final Pattern DOMAIN_LINE_PATTERN = Pattern.compile("^\\s+(\\d+)\\s+[!?]\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+\\S+\\s+(\\d+)\\s+(\\d+)\\s+\\S+\\s+(\\S+).*$");

    public static final Pattern DOMAIN_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+) \\s+(\\S+)\\s+(\\S+)\\s+(\\S+) \\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+\\S+\\s+(\\S+).*$");

    public static final Pattern DOMAIN_ALIGNMENT_LINE_PATTERN = Pattern.compile("^\\s+==\\s+domain\\s+(\\d+)\\s+.*$");

    // TODO: This pattern won't work for UniProt FASTA files because assumes sequence ID contains numbers 
    // TODO: and letters only, but UniProt FASTA ID lines contain "|", for example "tr|Q9U4N3|Q9U4N3_TOXGO"
    public static final Pattern ALIGNMENT_SEQUENCE_PATTERN = Pattern.compile("^\\s+(\\w+)\\s+(\\S+)\\s+([-a-zA-Z]+)\\s+(\\S+)\\s*$");

    //entered by Manjula for Gene3D parser
    //private final int domainNumber;

    String targetIdentifier;
    private String queryName;

    private double sequenceEValue;
    private double sequenceScore;
    private double sequenceBias;

    private double domainCEvalue;
    private double domainIEvalue;
    private double domainScore;
    private double domainBias;

    private int domainHmmfrom;
    private int domainHmmto;
    private int domainAliFrom;
    private int domainAliTo;
    private int domainEnvFrom;
    private int domainEnvTo;
    private double domainAccuracy;

    //private final double score;

    //private final double bias;

    //private final double cEvalue;

    //private final double iEvalue;

    //private final int hmmfrom;

    //private final int hmmto;

    //private final String hmmBounds;

    //private final int aliFrom;

    //private final int aliTo;

    //private final int envFrom;

    //private final int envTo;

    //private final double acc;

    //private String alignment;

    public DomTblDomainMatch(Matcher domainLineMatcher) {
        targetIdentifier = domainLineMatcher.group(1);
        queryName = domainLineMatcher.group(4);
        //signatureLibraryRelease,
        sequenceEValue = Double.parseDouble(domainLineMatcher.group(7));
        sequenceScore = Double.parseDouble(domainLineMatcher.group(8));
        sequenceBias = Double.parseDouble(domainLineMatcher.group(9));

        domainCEvalue = Double.parseDouble(domainLineMatcher.group(12));
        domainIEvalue = Double.parseDouble(domainLineMatcher.group(13));
        domainScore = Double.parseDouble(domainLineMatcher.group(14));
        domainBias = Double.parseDouble(domainLineMatcher.group(15));

        domainHmmfrom = Integer.parseInt(domainLineMatcher.group(16));
        domainHmmto = Integer.parseInt(domainLineMatcher.group(17));
        domainAliFrom = Integer.parseInt(domainLineMatcher.group(18));
        domainAliTo = Integer.parseInt(domainLineMatcher.group(19));
        domainEnvFrom = Integer.parseInt(domainLineMatcher.group(20));
        domainEnvTo = Integer.parseInt(domainLineMatcher.group(21));
        String acc = domainLineMatcher.group(22);
        domainAccuracy = 0.0;
//        domainAccuracy = Double.parseDouble(domainLineMatcher.group(22));

        // this.score = Double.parseDouble(domainLineMatcher.group(2));
        // this.bias = Double.parseDouble(domainLineMatcher.group(3));
        // this.cEvalue = Double.parseDouble(domainLineMatcher.group(4));
        // this.iEvalue = Double.parseDouble(domainLineMatcher.group(5));
        // this.hmmfrom = Integer.parseInt(domainLineMatcher.group(6));
        // this.hmmto = Integer.parseInt(domainLineMatcher.group(7));
        // this.hmmBounds = domainLineMatcher.group(8);
        // this.aliFrom = Integer.parseInt(domainLineMatcher.group(9));
        // this.aliTo = Integer.parseInt(domainLineMatcher.group(10));
        // this.envFrom = Integer.parseInt(domainLineMatcher.group(11));
        // this.envTo = Integer.parseInt(domainLineMatcher.group(12));
        // this.acc = Double.parseDouble(domainLineMatcher.group(13));

    }

    public DomTblDomainMatch(SequenceDomainMatch sequenceDomainMatch) {
//        this.score = sequenceDomainMatch.getScore();
//        this.bias = sequenceDomainMatch.getBias();
//        this.cEvalue = sequenceDomainMatch.getCEvalue();
//        this.iEvalue = sequenceDomainMatch.getIEvalue();
//        this.hmmfrom = sequenceDomainMatch.getHmmfrom();
//        this.hmmto = sequenceDomainMatch.getHmmto();
//        this.hmmBounds = sequenceDomainMatch.getHmmBounds();
//        this.aliFrom = sequenceDomainMatch.getAliFrom();
//        this.aliTo = sequenceDomainMatch.getAliTo();
//        this.envFrom = sequenceDomainMatch.getEnvFrom();
//        this.envTo = sequenceDomainMatch.getEnvTo();
//        this.acc = sequenceDomainMatch.getAcc();
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public String getQueryName() {
        return queryName;
    }

    public double getSequenceEValue() {
        return sequenceEValue;
    }

    public double getSequenceScore() {
        return sequenceScore;
    }

    public double getSequenceBias() {
        return sequenceBias;
    }

    public double getDomainCEvalue() {
        return domainCEvalue;
    }

    public double getDomainIEvalue() {
        return domainIEvalue;
    }

    public double getDomainScore() {
        return domainScore;
    }

    public double getDomainBias() {
        return domainBias;
    }

    public int getDomainHmmfrom() {
        return domainHmmfrom;
    }

    public int getDomainHmmto() {
        return domainHmmto;
    }

    public int getDomainAliFrom() {
        return domainAliFrom;
    }

    public int getDomainAliTo() {
        return domainAliTo;
    }

    public int getDomainEnvFrom() {
        return domainEnvFrom;
    }

    public int getDomainEnvTo() {
        return domainEnvTo;
    }

    public double getDomainAccuracy() {
        return domainAccuracy;
    }

    public String getDomTblDominLineKey() {
        return targetIdentifier
                +  queryName
                +  domainEnvFrom
                + "-"
                +  domainEnvTo;
    }


    /*
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
    
    */

    @Override
    public String toString() {
        return "DomTblDomainMatch{" +
                "targetIdentifier='" + targetIdentifier + '\'' +
                ", queryName='" + queryName + '\'' +
                ", sequenceEValue=" + sequenceEValue +
                ", sequenceScore=" + sequenceScore +
                ", sequenceBias=" + sequenceBias +
                ", domainCEvalue=" + domainCEvalue +
                ", domainIEvalue=" + domainIEvalue +
                ", domainScore=" + domainScore +
                ", domainBias=" + domainBias +
                ", domainHmmfrom=" + domainHmmfrom +
                ", domainHmmto=" + domainHmmto +
                ", domainAliFrom=" + domainAliFrom +
                ", domainAliTo=" + domainAliTo +
                ", domainEnvFrom=" + domainEnvFrom +
                ", domainEnvTo=" + domainEnvTo +
                ", domainAccuracy='" + domainAccuracy + '\'' +
                '}';
    }

}
