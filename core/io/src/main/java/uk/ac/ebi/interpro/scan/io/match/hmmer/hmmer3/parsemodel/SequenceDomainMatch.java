package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a match for a Domain line in the processed hmmsearch output (used in SFLD post processing).
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SequenceDomainMatch implements Serializable {

    /**
     * model_accession  hmmstart hmmend  [hhmbounds] domain_score envelope_start envelope_end ali_start ali_end domain_ce_value domain_ie_value  expected_accurracy domain_bias
     * Group[1] Model (string)
     * Group[2] hmm start (int)
     * Group[3] hmm end (int)
     * Group[4] hmmbounds, e.g. "[]"
     * Group[5] Score (float)
     * Group[6] Bias (float)
     * Group[7] env start  (int)
     * Group[8] env end  (int)
     * Group[9] ali start  (int)
     * Group[10] ali end  (int)
     * Group[11] c-Evalue (float)
     * Group[12] i-Evalue (float)
     * Group[13] acc (float)
     */
    public static final Pattern DOMAIN_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+).*$");

    public static final Pattern DOMAIN_ALIGNMENT_LINE_PATTERN = Pattern.compile("^\\s+==\\s+domain\\s+(\\d+)\\s+.*$");

    // TODO: This pattern won't work for UniProt FASTA files because assumes sequence ID contains numbers
    // TODO: and letters only, but UniProt FASTA ID lines contain "|", for example "tr|Q9U4N3|Q9U4N3_TOXGO"
    public static final Pattern ALIGNMENT_SEQUENCE_PATTERN = Pattern.compile("^\\s+(\\w+)\\s+(\\S+)\\s+([-a-zA-Z]+)\\s+(\\S+)\\s*$");



    private final String modelAccession;

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

    public SequenceDomainMatch(Matcher domainLineMatcher) {
        this.modelAccession = domainLineMatcher.group(1);
        this.hmmto = Integer.parseInt(domainLineMatcher.group(2));
        this.hmmfrom = Integer.parseInt(domainLineMatcher.group(3));
        this.hmmBounds = domainLineMatcher.group(4);
        this.score = Double.parseDouble(domainLineMatcher.group(5));
        this.bias = Double.parseDouble(domainLineMatcher.group(6));
        this.aliFrom = Integer.parseInt(domainLineMatcher.group(7));
        this.aliTo = Integer.parseInt(domainLineMatcher.group(8));
        this.envFrom = Integer.parseInt(domainLineMatcher.group(9));
        this.envTo = Integer.parseInt(domainLineMatcher.group(10));
        this.cEvalue = Double.parseDouble(domainLineMatcher.group(11));
        this.iEvalue = Double.parseDouble(domainLineMatcher.group(12));
        this.acc = Double.parseDouble(domainLineMatcher.group(13));

    }

    public String getModelAccession() {
        return modelAccession;
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
        return "SequenceDomainMatch{" +
                "accession=" + modelAccession +
                ", score=" + score +
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
