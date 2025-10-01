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
    public static final Pattern HMMSEARCH_DOMAIN_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(.*)$");
    public static final Pattern HMMSCAN_DOMAIN_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)$");

    private String queryName;
    private String queryAccession;
    private int queryLength;

    private String targetIdentifier;
    private String targetAccession;
    private int targetLength;

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

    public DomTblDomainMatch(Matcher domainLineMatcher, String mode) {
        if (mode.equals("hmmsearch")) {
            targetIdentifier = domainLineMatcher.group(1);
            targetAccession = domainLineMatcher.group(2);
            targetLength = Integer.parseInt(domainLineMatcher.group(3));
            queryName = domainLineMatcher.group(4);
            queryAccession = domainLineMatcher.group(5);
            queryLength = Integer.parseInt(domainLineMatcher.group(6));
        }else{
            targetIdentifier = domainLineMatcher.group(4);
            targetAccession = domainLineMatcher.group(5);
            targetLength = Integer.parseInt(domainLineMatcher.group(6));
            queryName = domainLineMatcher.group(1);
            queryAccession = domainLineMatcher.group(2);
            queryLength = Integer.parseInt(domainLineMatcher.group(3));
        }

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
        domainAccuracy = Double.parseDouble(domainLineMatcher.group(22));
    }

    public DomTblDomainMatch(String targetIdentifier, String queryName,
                             double sequenceEValue, double sequenceScore, double sequenceBias,
                             double domainCEvalue, double domainIEvalue, double domainScore, double domainBias,
                             int domainHmmfrom, int domainHmmto, int domainAliFrom, int domainAliTo,
                             int domainEnvFrom, int domainEnvTo, double domainAccuracy) {

        this.targetIdentifier = targetIdentifier;
        this.queryName = queryName;

        this.sequenceEValue = sequenceEValue;
        this.sequenceScore = sequenceScore;
        this.sequenceBias = sequenceBias;

        this.domainCEvalue = domainCEvalue;
        this.domainIEvalue = domainIEvalue;
        this.domainScore = domainScore;
        this.domainBias = domainBias;

        this.domainHmmfrom = domainHmmfrom;
        this.domainHmmto = domainHmmto;
        this.domainAliFrom = domainAliFrom;
        this.domainAliTo = domainAliTo;
        this.domainEnvFrom = domainEnvFrom;
        this.domainEnvTo = domainEnvTo;

        this.domainAccuracy = domainAccuracy;
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

    public int getQueryLength() {
        return queryLength;
    }

    public int getTargetLength() {
        return targetLength;
    }

    public String getDomTblDominLineKey() {
        return targetIdentifier
                +  queryName
                +  domainEnvFrom
                + "-"
                +  domainEnvTo;
    }

    public String getPirsfDomTblDomainLineKey() {
        return targetIdentifier
                +  queryAccession
                +  domainEnvFrom
                + "-"
                +  domainEnvTo;
    }

    public String getGene3DDomTblDominLineKey(String mode) {
        String gene3DQueryName = queryName.split("\\-")[0];

        return targetIdentifier
                +  gene3DQueryName
                +  domainEnvFrom
                + "-"
                +  domainEnvTo;
    }

    public static Matcher getDomainDataLineMatcher(String line, String mode){
        if (mode.equals("hmmsearch")) {
            return DomTblDomainMatch.HMMSEARCH_DOMAIN_LINE_PATTERN.matcher(line);
        }else{
            return DomTblDomainMatch.HMMSCAN_DOMAIN_LINE_PATTERN.matcher(line);
        }

    }

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
