package uk.ac.ebi.interpro.scan.io;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleSequenceMatch {

     Map<String, List<SimpleDomainMatch>> domainMatches = new HashMap<>();

    public SimpleSequenceMatch(Map<String, List<SimpleDomainMatch>> domainMatches) {
        this.domainMatches = domainMatches;
    }

    public SimpleSequenceMatch() {
    }

    public Map<String, List<SimpleDomainMatch>> getDomainMatches() {
        return domainMatches;
    }

    public void setDomainMatches(Map<String, List<SimpleDomainMatch>> domainMatches) {
        this.domainMatches = domainMatches;
    }

    @Override
    public String toString() {
        return "SimpleSequenceMatch{" +
                "domainMatches=" + domainMatches.toString() +
                '}';
    }
}
