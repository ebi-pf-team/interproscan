package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.HmmBounds;

import javax.persistence.Entity;
import java.util.*;

/**
 * <a href="http://hmmer.janelia.org/">HMMER 3</a> raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public abstract class Hmmer3RawMatch extends HmmerRawMatch {

    private int envelopeStart;
    private int envelopeEnd;
    
    private double expectedAccuracy;

    private double fullSequenceBias;

    private double domainCeValue;
    private double domainIeValue;
    private double domainBias;

    protected Hmmer3RawMatch() { }

    protected Hmmer3RawMatch(String sequenceIdentifier, String model,
                             String signatureLibraryName, String signatureLibraryRelease,
                             int locationStart, int locationEnd,
                             double evalue, double score,
                             int hmmStart, int hmmEnd, String hmmBounds,
                             double locationScore,
                             int envelopeStart, int envelopeEnd,
                             double expectedAccuracy, double fullSequenceBias,
                             double domainCeValue, double domainIeValue, double domainBias,
                             String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, generator);
        this.envelopeStart = envelopeStart;
        this.envelopeEnd = envelopeEnd;
        this.expectedAccuracy = expectedAccuracy;
        this.fullSequenceBias = fullSequenceBias;
        this.domainCeValue = Math.log10(domainCeValue);
        this.domainIeValue = Math.log10(domainIeValue);
        this.domainBias = domainBias;
    }

    public int getEnvelopeStart() {
        return envelopeStart;
    }

    private void setEnvelopeStart(int envelopeStart) {
        this.envelopeStart = envelopeStart;
    }

    public int getEnvelopeEnd() {
        return envelopeEnd;
    }

    private void setEnvelopeEnd(int envelopeEnd) {
        this.envelopeEnd = envelopeEnd;
    }

    public double getExpectedAccuracy() {
        return expectedAccuracy;
    }

    private void setExpectedAccuracy(double expectedAccuracy) {
        this.expectedAccuracy = expectedAccuracy;
    }

    public double getFullSequenceBias() {
        return fullSequenceBias;
    }

    private void setFullSequenceBias(double fullSequenceBias) {
        this.fullSequenceBias = fullSequenceBias;
    }

    public double getDomainCeValue() {
        return domainCeValue;
    }

    private void setDomainCeValue(double domainCeValue) {
        this.domainCeValue = domainCeValue;
    }

    public double getDomainIeValue() {
        return domainIeValue;
    }

    private void setDomainIeValue(double domainIeValue) {
        this.domainIeValue = domainIeValue;
    }

    public double getDomainBias() {
        return domainBias;
    }

    private void setDomainBias(double domainBias) {
        this.domainBias = domainBias;
    }

// TODO: Add hashCode(), equals() ...etc


    // TODO: Generalise this to RawMatch
    public static Collection<Hmmer2Match> getHmmerMatches(Collection<Hmmer3RawMatch> rawMatches)  {
        Collection<Hmmer2Match> matches = new HashSet<Hmmer2Match>();
        Map<String, Set<Hmmer3RawMatch>> matchesByModel = new HashMap<String, Set<Hmmer3RawMatch>>();
        // Assign raw matches to protein
        for (Hmmer3RawMatch m : rawMatches)   {
            String modelId = m.getModel();
            if (matchesByModel.containsKey(modelId))    {
                matchesByModel.get(modelId).add(m);
            }
            else    {
                Set<Hmmer3RawMatch> set = new HashSet<Hmmer3RawMatch>();
                set.add(m);
                matchesByModel.put(modelId, set);
            }
        }

        for (String key : matchesByModel.keySet())  {
            Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<Hmmer2Match.Hmmer2Location>();
            double score = 0, evalue = 0;
            for (Hmmer3RawMatch rm : matchesByModel.get(key))   {
                // Score and evalue should be the same (repeated for each location)
                score  = rm.getScore();
                evalue = rm.getEvalue();
                locations.add(getHmmer2Location(rm));
            }
            // TODO: Look up correct signature accession based on model ID, library release ...etc
            Signature signature = new Signature(key);
            matches.add(new Hmmer2Match(signature, score, evalue, locations));
        }
        // Next step would be to link this with protein...
        return matches;

    }

    // TODO: Add HMMER3 stuff to HmmerLocation
    public static Hmmer2Match.Hmmer2Location getHmmer2Location(Hmmer3RawMatch m){
        return new Hmmer2Match.Hmmer2Location(
                m.getLocationStart(),
                m.getLocationEnd(),
                m.getLocationScore(),
                m.getDomainIeValue(),
                m.getHmmStart(),
                m.getHmmEnd(),
                HmmBounds.parseSymbol(m.getHmmBounds())
        );
    }
    
}