package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel;

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
 * @version $Id: Hmmer2SearchRecord.java 558 2010-04-22 10:24:53Z aquinn.ebi $
 * @since 1.0-SNAPSHOT
 */
public final class Hmmer2SearchRecord implements Serializable {

    private final String modelAccession;
    private Integer modelLength;
    private final Map<String, Hmmer2SequenceMatch> sequenceMatches = new HashMap<String, Hmmer2SequenceMatch>();

    public Hmmer2SearchRecord(String modelAccession) {
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

    public Map<String, Hmmer2SequenceMatch> getSequenceMatches() {
        return sequenceMatches;
    }

    public void addSequenceMatch(Hmmer2SequenceMatch hmmer2SequenceMatch) {
        this.sequenceMatches.put(hmmer2SequenceMatch.getSequenceIdentifier(), hmmer2SequenceMatch);
    }

    public void addDomainMatch(String sequenceId, Hmmer2DomainMatch hmmer2DomainMatch) {
        Hmmer2SequenceMatch parentHmmer2SequenceMatch = this.sequenceMatches.get(sequenceId);
        if (parentHmmer2SequenceMatch == null) {
            throw new IllegalStateException("Cannot add a domain match without a corresponding sequence match.");
        }
        parentHmmer2SequenceMatch.addDomainMatch(hmmer2DomainMatch);
    }

    public void removeDomainMatch(String sequenceId, Hmmer2DomainMatch hmmer2DomainMatch) {
        Hmmer2SequenceMatch parentHmmer2SequenceMatch = this.sequenceMatches.get(sequenceId);
        if (parentHmmer2SequenceMatch == null) {
            throw new IllegalStateException("Cannot remove a domain match without a corresponding sequence match.");
        }
        parentHmmer2SequenceMatch.removeDomainMatch(hmmer2DomainMatch);
    }

}
