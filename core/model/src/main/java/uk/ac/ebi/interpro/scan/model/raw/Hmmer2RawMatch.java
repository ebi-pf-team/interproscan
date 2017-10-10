package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.*;

/**
 * <a href="http://hmmer.janelia.org/">HMMER 2</a> raw match.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
public abstract class Hmmer2RawMatch extends HmmerRawMatch {

    //    @Column(name = "location_evalue")
    @Column
    private double locationEvalue;

    protected Hmmer2RawMatch() {
    }

    protected Hmmer2RawMatch(String sequenceIdentifier, String model,
                             SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                             int locationStart, int locationEnd,
                             double evalue, double score,
                             int hmmStart, int hmmEnd, String hmmBounds,
                             double locationEvalue, double locationScore) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore);
        setLocationEvalue(locationEvalue);
    }

    public double getLocationEvalue() {
        return locationEvalue;
    }

    private void setLocationEvalue(double locationEvalue) {
        this.locationEvalue = locationEvalue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Hmmer2RawMatch))
            return false;
        final Hmmer2RawMatch m = (Hmmer2RawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(locationEvalue, m.locationEvalue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 57)
                .appendSuper(super.hashCode())
                .append(locationEvalue)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    public static Collection<Hmmer2Match> getMatches(Collection<? extends Hmmer2RawMatch> rawMatches,
                                                     Listener rawMatchListener) {
        Collection<Hmmer2Match> matches = new HashSet<Hmmer2Match>();
        // Get a list of unique model IDs
        SignatureLibrary signatureLibrary = null;
        String signatureLibraryRelease = null;
        Map<String, Set<Hmmer2RawMatch>> matchesByModel = new HashMap<String, Set<Hmmer2RawMatch>>();
        for (Hmmer2RawMatch m : rawMatches) {
            // Get signature library name and release
            if (signatureLibrary == null) {
                signatureLibrary = m.getSignatureLibrary();
                signatureLibraryRelease = m.getSignatureLibraryRelease();
            } else if (!signatureLibrary.equals(m.getSignatureLibrary()) ||
                    !signatureLibraryRelease.equals(m.getSignatureLibraryRelease())) {
                throw new IllegalArgumentException("Filtered matches are from different signature library versions " +
                        "(more than one library version found)");
            }
            // Get unique list of model IDs
            String modelId = m.getModelId();
            if (matchesByModel.containsKey(modelId)) {
                matchesByModel.get(modelId).add(m);
            } else {
                Set<Hmmer2RawMatch> set = new HashSet<Hmmer2RawMatch>();
                set.add(m);
                matchesByModel.put(modelId, set);
            }
        }
        // Find the location(s) for each match and create a Match instance
        for (String key : matchesByModel.keySet()) {
            Signature signature = rawMatchListener.getSignature(key, signatureLibrary, signatureLibraryRelease);
            matches.add(getMatch(signature, key, matchesByModel));
        }
        // Next step would be to link this with protein...
        return matches;

    }

    private static Hmmer2Match getMatch(Signature signature, String modelId, Map<String, Set<Hmmer2RawMatch>> matchesByModel) {
        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<Hmmer2Match.Hmmer2Location>();
        double score = 0, evalue = 0;
        for (Hmmer2RawMatch m : matchesByModel.get(modelId)) {
            // Score and evalue should be the same (repeated for each location)
            score = m.getScore();
            evalue = m.getEvalue();
            locations.add(getLocation(m));
        }
        return new Hmmer2Match(signature, modelId, score, evalue, locations);
    }

    private static Hmmer2Match.Hmmer2Location getLocation(Hmmer2RawMatch m) {
        return new Hmmer2Match.Hmmer2Location(
                m.getLocationStart(),
                m.getLocationEnd(),
                m.getLocationScore(),
                m.getLocationEvalue(),
                m.getHmmStart(),
                m.getHmmEnd(),
                HmmBounds.parseSymbol(m.getHmmBounds())
        );
    }

}
