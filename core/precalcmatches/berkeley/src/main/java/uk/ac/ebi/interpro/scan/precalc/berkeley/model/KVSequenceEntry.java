package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
public class KVSequenceEntry implements Serializable{
    @PrimaryKey
    private String proteinMD5;

    Set<String> sequenceHits = new HashSet<>();

    /**
     * Required by BerkeleyDB
     */
    public KVSequenceEntry() {

    }

    public String getProteinMD5() {
        return proteinMD5;
    }

    public void setProteinMD5(String proteinMD5) {
        this.proteinMD5 = proteinMD5;
    }

    @XmlElement(name = "hit")
    public Set<String> getSequenceHits() {
        return sequenceHits;
    }

    public void setSequenceHits(Set<String> sequenceHits) {
        this.sequenceHits = sequenceHits;
    }

    public void addMatch(String sequenceHit) {
        this.sequenceHits.add(sequenceHit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KVSequenceEntry otherMatch = (KVSequenceEntry) o;

        if (this.proteinMD5 != null ? !this.proteinMD5.equals(otherMatch.proteinMD5) : otherMatch.proteinMD5 != null)
            return false;
        if (this.sequenceHits != null ? !this.sequenceHits.equals(otherMatch.sequenceHits) : otherMatch.sequenceHits != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.proteinMD5 != null ? this.proteinMD5.hashCode() : 0;
        result = 31 * result + (this.sequenceHits != null ? this.sequenceHits.hashCode() : 0);
        return result;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{KVSequenceEntry");
        sb.append(" MD5 ").append(this.proteinMD5);
        for (String hit: this.sequenceHits) {
            sb.append(" \n ");
            sb.append("  hit: ").append(hit);
        }
        sb.append(" \n ");
        sb.append('}');
        return sb.toString();
    }
}
