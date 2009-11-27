package uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

/**
 * This model object represents a single record in the hmmsearch output format,
 * i.e. it represents a single HMM model entry.  Sequence and Domain matches
 * are then added to this object.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class HmmsearchOutputMethod implements Serializable {

    private final String methodAccession;

    private final Map<String, SequenceMatch> sequenceMatches = new HashMap<String, SequenceMatch>();

    public HmmsearchOutputMethod(String methodAccession){
        this.methodAccession = methodAccession;
    }

    public void addSequenceMatch (SequenceMatch sequenceMatch){
        this.sequenceMatches.put (sequenceMatch.getUpi(), sequenceMatch);
    }

    public void addDomainMatch (String modelId, DomainMatch domainMatch){
        SequenceMatch parentSequenceMatch = this.sequenceMatches.get(modelId);
        if (parentSequenceMatch == null){
            throw new IllegalStateException ("Check for a logic error in the HMMER3hmmscanFullFormatParser - trying to add a domain match, but there is no corresponding sequence match.");
        }
        parentSequenceMatch.addDomainMatch(domainMatch);
    }

    public String getMethodAccession() {
        return methodAccession;
    }

    public Map<String, SequenceMatch> getSequenceMatches() {
        return sequenceMatches;
    }
}
