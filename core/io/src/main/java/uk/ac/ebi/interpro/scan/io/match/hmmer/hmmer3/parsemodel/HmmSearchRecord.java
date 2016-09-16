package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Represents a single record in the hmmsearch output file, for example:</p>
 * <pre>
 * Query:       DUF3497  [M=221]
 * Accession:   PF12003.1
 * Description: Domain of unknown function (DUF3497)
 * Scores for complete sequences (score includes all domains):
 *    --- full sequence ---   --- best 1 domain ---    -#dom-
 *     E-value  score  bias    E-value  score  bias    exp  N  Sequence      Description
 *     ------- ------ -----    ------- ------ -----   ---- --  --------      -----------
 *     1.3e-52  187.5   0.0      2e-52  186.9   0.0    1.2  1  UPI00000015B6
 * <p/>
 * <p/>
 * Domain and alignment annotation for each sequence:
 * >> UPI00000015B6
 *    #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
 * ---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
 * 1 !  186.9   0.0   2.1e-59     2e-52       4     220 ..    2047    2289 ..    2044    2290 .. 0.97
 * <p/>
 *   Alignments for each domain:
 *   == domain 1    score: 186.9 bits;  conditional E-value: 2.1e-59
 *         DUF3497    4 lqdgesaselareLaelt..krtlyggDvlttvklleqlldllsvqlrallpatkdsaarenlvktvsnLLdpeakeaWeqlqtteqlrgatkL 95
 *                      l++g s+ +la  L+++t  +  ++g+Dv+++++l ++ll+++s+q+++ l+at+d++++enl++++s+LLd ++k +We +q+te+  g+++L
 *   UPI00000015B6 2047 LDSGRSQ-QLALLLRNATqhTAGYFGSDVKVAYQLATRLLAHESTQRGFGLSATQDVHFTENLLRVGSALLDTANKRHWELIQQTEG--GTAWL 2137
 *                      6677766.8*********85559****************************************************************..***** PP
 * </pre>
 *
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class HmmSearchRecord implements Serializable {

    private final String modelAccession;
    private Integer modelLength;
    private final Map<String, SequenceMatch> sequenceMatches = new HashMap<String, SequenceMatch>();

    public HmmSearchRecord(String modelAccession) {
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
            throw new IllegalStateException("Cannot add a domain match without a corresponding sequence match." + sequenceId);
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
