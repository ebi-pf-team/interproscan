package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Set;
import java.util.TreeSet;

import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;

/**
 * Very simple Match implementation for data transfer &
 * storage in BerkeleyDB.
 * <p/>
 * Holds all of the fields that may appear in any Location
 * implementation (from the main InterProScan 5 data model).
 * <p/>
 * Note that the MD5 of the protein sequence is the key used to
 * access this data from BerkeleyDB,so does not appear in the class.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

@Entity
public class BerkeleyMatch {

    @PrimaryKey(sequence = "match_unique_index_sequence")
    private Long matchId;

    @SecondaryKey(relate = MANY_TO_ONE)
    private String proteinMD5;

    private String signatureLibraryName;

    private String signatureLibraryRelease;

    private String signatureAccession;

    private String signatureModels;

    private Double sequenceScore;

    private Double sequenceEValue;

    private String graphScan;


    private Set<BerkeleyLocation> locations;

    /**
     * Required by BerkeleyDB
     */
    public BerkeleyMatch() {

    }

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public String getProteinMD5() {
        return proteinMD5;
    }

    public void setProteinMD5(String proteinMD5) {
        this.proteinMD5 = proteinMD5;
    }

    public String getSignatureLibraryName() {
        return signatureLibraryName;
    }

    public void setSignatureLibraryName(String signatureLibraryName) {
        this.signatureLibraryName = signatureLibraryName;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public String getSignatureAccession() {
        return signatureAccession;
    }

    public void setSignatureAccession(String signatureAccession) {
        this.signatureAccession = signatureAccession;
    }

    public String getSignatureModels() {
        return signatureModels;
    }

    public void setSignatureModels(String signatureModels) {
        this.signatureModels = signatureModels;
    }

    public Double getSequenceScore() {
        return sequenceScore;
    }

    public void setSequenceScore(Double sequenceScore) {
        this.sequenceScore = sequenceScore;
    }

    public Double getSequenceEValue() {
        return sequenceEValue;
    }

    public void setSequenceEValue(Double sequenceEValue) {
        this.sequenceEValue = sequenceEValue;
    }

    public String getGraphScan() {
        return graphScan;
    }

    public void setGraphScan(String graphScan) {
        this.graphScan = graphScan;
    }

    // XmLElementWrapper generates a wrapper element around XML representation
    @XmlElementWrapper(name = "locations")
    // XmlElement sets the name of the entities
    @XmlElement(name = "location")
    public Set<BerkeleyLocation> getLocations() {
        return locations;
    }

    public void setLocations(Set<BerkeleyLocation> locations) {
        this.locations = locations;
    }

    public void addLocation(BerkeleyLocation location) {
        if (this.locations == null) {
            this.locations = new TreeSet<BerkeleyLocation>();
        }
        locations.add(location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BerkeleyMatch berkeley_match = (BerkeleyMatch) o;

        if (graphScan != null ? !graphScan.equals(berkeley_match.graphScan) : berkeley_match.graphScan != null)
            return false;
        if (locations != null ? !locations.equals(berkeley_match.locations) : berkeley_match.locations != null)
            return false;
        if (sequenceEValue != null ? !sequenceEValue.equals(berkeley_match.sequenceEValue) : berkeley_match.sequenceEValue != null)
            return false;
        if (sequenceScore != null ? !sequenceScore.equals(berkeley_match.sequenceScore) : berkeley_match.sequenceScore != null)
            return false;
        if (signatureAccession != null ? !signatureAccession.equals(berkeley_match.signatureAccession) : berkeley_match.signatureAccession != null)
            return false;
        if (signatureLibraryName != null ? !signatureLibraryName.equals(berkeley_match.signatureLibraryName) : berkeley_match.signatureLibraryName != null)
            return false;
        if (signatureLibraryRelease != null ? !signatureLibraryRelease.equals(berkeley_match.signatureLibraryRelease) : berkeley_match.signatureLibraryRelease != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = signatureLibraryName != null ? signatureLibraryName.hashCode() : 0;
        result = 31 * result + (signatureLibraryRelease != null ? signatureLibraryRelease.hashCode() : 0);
        result = 31 * result + (signatureAccession != null ? signatureAccession.hashCode() : 0);
        result = 31 * result + (sequenceScore != null ? sequenceScore.hashCode() : 0);
        result = 31 * result + (sequenceEValue != null ? sequenceEValue.hashCode() : 0);
        result = 31 * result + (graphScan != null ? graphScan.hashCode() : 0);
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        return result;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BerkeleyMatch");
        sb.append("{SignatureLibrary ").append(signatureLibraryName);
        sb.append(" Release ").append(signatureLibraryRelease);
        sb.append(" MD5 ").append(proteinMD5);
        sb.append(" signatureAc ").append(signatureAccession);
        sb.append('}');
        return sb.toString();
    }
}
