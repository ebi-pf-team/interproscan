package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Represents a single record in the HMMER2 hmmpfam output file, for example:</p>
 * <pre>
 * Query sequence: tr|C6UBH1|C6UBH1_ECOBR
 * Accession:      [none]
 * Description:    4'-phosphopantetheinyl transferase OS=Escherichia coli (strain B / REL606) GN=acpS PE=3 SV=1
 * <p/>
 * Scores for sequence family classification (score includes all domains):
 * Model        Description                                Score    E-value  N
 * --------     -----------                                -----    ------- ---
 * TIGR00516    acpS: holo-[acyl-carrier-protein] syntha   237.9    9.5e-69   1
 * TIGR00556    pantethn_trn: phosphopantethiene--protei   204.9    7.8e-59   1
 * <p/>
 * Parsed for domains:
 * Model        Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 * --------     ------- ----- -----    ----- -----      -----  -------
 * TIGR00516      1/1       1   126 []     1   131 []   237.9  9.5e-69
 * TIGR00556      1/1       2   126 .]     1   139 []   204.9  7.8e-59
 * //
 * </pre>
 *
 * @author Phil Jones
 * @version $Id: Hmmer2HmmPfamSearchRecord.java 558 2010-04-22 10:24:53Z aquinn.ebi $
 * @since 1.0-SNAPSHOT
 */
public final class Hmmer2HmmPfamSearchRecord implements Serializable {

    private final String sequenceId;
    /**
     * Map of model accession as String, to Hmmer2HmmPfamSequenceMatch
     */
    private final Map<String, Hmmer2HmmPfamSequenceMatch> sequenceMatches = new HashMap<String, Hmmer2HmmPfamSequenceMatch>();

    public Hmmer2HmmPfamSearchRecord(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    /**
     * Returns Map of model accession as String, to Hmmer2HmmPfamSequenceMatch
     *
     * @return Map of model accession as String, to Hmmer2HmmPfamSequenceMatch
     */
    public Map<String, Hmmer2HmmPfamSequenceMatch> getSequenceMatches() {
        return sequenceMatches;
    }

    public void addSequenceMatch(Hmmer2HmmPfamSequenceMatch hmmer2SequenceMatch) {
        this.sequenceMatches.put(hmmer2SequenceMatch.getModelAccession(), hmmer2SequenceMatch);
    }

    public void addDomainMatch(Hmmer2HmmPfamDomainMatch hmmer2DomainMatch) {
        Hmmer2HmmPfamSequenceMatch parentHmmer2SequenceMatch = this.sequenceMatches.get(hmmer2DomainMatch.getModelAccession());
        if (parentHmmer2SequenceMatch == null) {
            throw new IllegalStateException("Cannot add a domain match without a corresponding sequence match.");
        }
        parentHmmer2SequenceMatch.addDomainMatch(hmmer2DomainMatch);
    }

    public void removeDomainMatch(Hmmer2HmmPfamDomainMatch hmmer2DomainMatch) {
        Hmmer2HmmPfamSequenceMatch parentHmmer2SequenceMatch = this.sequenceMatches.get(hmmer2DomainMatch.getModelAccession());
        if (parentHmmer2SequenceMatch == null) {
            throw new IllegalStateException("Cannot remove a domain match without a corresponding sequence match.");
        }
        parentHmmer2SequenceMatch.removeDomainMatch(hmmer2DomainMatch);
    }

}
