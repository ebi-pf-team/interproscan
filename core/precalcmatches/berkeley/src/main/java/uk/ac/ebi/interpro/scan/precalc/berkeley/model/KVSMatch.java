package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

import java.io.Serializable;


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
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

@Entity
public class KVSMatch implements Serializable{

    @PrimaryKey(sequence = "match_unique_index_sequence")
    private Long matchId;

    @SecondaryKey(relate = MANY_TO_ONE)
    private String proteinMD5;

    Set<String> kvsMatchSet = new HashSet<>();

    /**
     * Required by BerkeleyDB
     */
    public KVSMatch() {

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

    @XmlElement(name = "hit")
    public Set<String> getKvsMatchSet() {
        return kvsMatchSet;
    }

    public void setKvsMatchSet(Set<String> kvsMatchSet) {
        this.kvsMatchSet = kvsMatchSet;
    }

    public void addMatch(String kvsMatch) {
        this.kvsMatchSet.add(kvsMatch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KVSMatch otherMatch = (KVSMatch) o;

        if (this.proteinMD5 != null ? !this.proteinMD5.equals(otherMatch.proteinMD5) : otherMatch.proteinMD5 != null)
            return false;
        if (this.kvsMatchSet != null ? !this.kvsMatchSet.equals(otherMatch.kvsMatchSet) : otherMatch.kvsMatchSet != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.proteinMD5 != null ? this.proteinMD5.hashCode() : 0;
        result = 31 * result + (this.kvsMatchSet != null ? this.kvsMatchSet.hashCode() : 0);
        return result;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("KVSMatch");
        sb.append(" MD5 ").append(this.proteinMD5);
        for (String hit: this.kvsMatchSet) {
            sb.append(" \n ");
            sb.append("  hit: ").append(hit);
        }
        sb.append(" \n ");
        sb.append('}');
        return sb.toString();
    }
}
