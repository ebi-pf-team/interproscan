package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Provides a match for a Domain line in hmmsearch output format. e.g.
 * <pre>
 * Parsed for domains:
 * Model        Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 * --------     ------- ----- -----    ----- -----      -----  -------
 * TIGR00516      1/1       1   126 []     1   131 []   237.9  9.5e-69
 * TIGR00556      1/1       2   126 .]     1   139 []   204.9  7.8e-59
 * //
 * </pre>
 *
 * @author Phil Jones
 * @version $Id: Hmmer2HmmPfamDomainMatch.java 503 2010-03-15 16:56:14Z dwbinns $
 * @since 1.0-SNAPSHOT
 */
public class Hmmer2HmmPfamDomainMatch implements Serializable {

    private final String modelAccession;

    private final double score;

    private final double eValue;

    private final int hmmFrom;

    private final int hmmTo;

    private final String hmmBounds;

    private final int seqFrom;

    private final int seqTo;

    private final String seqBounds;

    private String alignment;

    public Hmmer2HmmPfamDomainMatch(String domainLine) {
        String[] lineParts = domainLine.trim().split("\\s+");
        if (lineParts.length != 10) {
            throw new IllegalStateException("The hmmpfam domain line parser is not splitting up the line correctly. Here's the bits: " + Arrays.toString(lineParts));
        }
        this.modelAccession = lineParts[0];
        this.seqFrom = Integer.parseInt(lineParts[2]);
        this.seqTo = Integer.parseInt(lineParts[3]);
        this.seqBounds = lineParts[4];
        this.hmmFrom = Integer.parseInt(lineParts[5]);
        this.hmmTo = Integer.parseInt(lineParts[6]);
        this.hmmBounds = lineParts[7];
        this.score = Double.parseDouble(lineParts[8]);
        this.eValue = Double.parseDouble(lineParts[9]);
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

    public int getHmmFrom() {
        return hmmFrom;
    }

    public int getHmmTo() {
        return hmmTo;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    public int getSeqFrom() {
        return seqFrom;
    }

    public int getSeqTo() {
        return seqTo;
    }

    public double getEValue() {
        return eValue;
    }

    public String getSeqBounds() {
        return seqBounds;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }
}
