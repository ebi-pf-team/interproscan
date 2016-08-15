package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Represents a single record in the hmmsearch output file, for example:</p>
 * <pre>
 * <p/>
 * Query:       A2YIW7  [L=122]
 Scores for complete sequence (score includes all domains):
 --- full sequence ---   --- best 1 domain ---    -#dom-
 E-value  score  bias    E-value  score  bias    exp  N  Model      Description
 ------- ------ -----    ------- ------ -----   ---- --  --------   -----------
 9.1e-34  109.9   0.4      1e-33  109.7   0.4    1.0  1  SFLDG00337  Thioredoxin-like

 Domain annotation for each model (and alignments):
 >> SFLDG00337  Thioredoxin-like
 #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
 ---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
 1 !  109.7   0.4   1.2e-35     1e-33       6     104 .]      12     112 ..       7     112 .. 0.93

 Alignments for each domain:
 == domain 1  score: 109.7 bits;  conditional E-value: 1.2e-35
 x.xxxxxxxx...xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx RF

 * </pre>
 *
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class HmmScanSearchRecord implements Serializable {

    private final String modelAccession;
    private Integer modelLength;
    private final Map<String, SequenceMatch> sequenceMatches = new HashMap<String, SequenceMatch>();

    public HmmScanSearchRecord(String modelAccession) {
        this.modelAccession = modelAccession;
    }

    public String getModelAccession() {
        return modelAccession;
    }

    public int getModelLength() {
        return modelLength;
    }

    public void setModelLength(Integer modelLength) {
        this.modelLength = modelLength;
    }

    public Map<String, SequenceMatch> getSequenceMatches() {
        return sequenceMatches;
    }

    public void addSequenceMatch(SequenceMatch sequenceMatch) {
        this.sequenceMatches.put(sequenceMatch.getSequenceIdentifier(), sequenceMatch);
    }

    public void addDomainMatch(String sequenceId, DomainMatch domainMatch) {
        SequenceMatch parentSequenceMatch = this.sequenceMatches.get(sequenceId);
        if (parentSequenceMatch == null) {
            throw new IllegalStateException("Cannot add a domain match without a corresponding sequence match.");
        }
        parentSequenceMatch.addDomainMatch(domainMatch);
    }

    public void removeDomainMatch(String sequenceId, DomainMatch domainMatch) {
        SequenceMatch parentSequenceMatch = this.sequenceMatches.get(sequenceId);
        if (parentSequenceMatch == null) {
            throw new IllegalStateException("Cannot remove a domain match without a corresponding sequence match.");
        }
        parentSequenceMatch.removeDomainMatch(domainMatch);
    }

}
