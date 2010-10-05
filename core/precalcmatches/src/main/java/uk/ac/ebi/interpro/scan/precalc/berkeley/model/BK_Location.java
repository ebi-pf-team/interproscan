package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Persistent;


/**
 * Very simple Location implementation for data transfer &
 * storage in BerkeleyDB.
 * <p/>
 * Holds all of the fields that may appear in any Location
 * implementation (from the main InterProScan 5 data model).
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Persistent
public class BK_Location {

    private Integer start;

    private Integer end;

    private Double score;

    private Double eValue;

    private Double pValue;

    private Integer motifNumber;

    private Integer hmmStart;

    private Integer hmmEnd;

    private String hmmBounds;

    private Integer envelopeStart;

    private Integer envelopeEnd;

    private String level;

    private String cigarAlignment;

    public BK_Location() {
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double geteValue() {
        return eValue;
    }

    public void seteValue(Double eValue) {
        this.eValue = eValue;
    }

    public Double getpValue() {
        return pValue;
    }

    public void setpValue(Double pValue) {
        this.pValue = pValue;
    }

    public Integer getMotifNumber() {
        return motifNumber;
    }

    public void setMotifNumber(Integer motifNumber) {
        this.motifNumber = motifNumber;
    }

    public Integer getHmmStart() {
        return hmmStart;
    }

    public void setHmmStart(Integer hmmStart) {
        this.hmmStart = hmmStart;
    }

    public Integer getHmmEnd() {
        return hmmEnd;
    }

    public void setHmmEnd(Integer hmmEnd) {
        this.hmmEnd = hmmEnd;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    public void setHmmBounds(String hmmBounds) {
        this.hmmBounds = hmmBounds;
    }

    public Integer getEnvelopeStart() {
        return envelopeStart;
    }

    public void setEnvelopeStart(Integer envelopeStart) {
        this.envelopeStart = envelopeStart;
    }

    public Integer getEnvelopeEnd() {
        return envelopeEnd;
    }

    public void setEnvelopeEnd(Integer envelopeEnd) {
        this.envelopeEnd = envelopeEnd;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCigarAlignment() {
        return cigarAlignment;
    }

    public void setCigarAlignment(String cigarAlignment) {
        this.cigarAlignment = cigarAlignment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BK_Location that = (BK_Location) o;

        if (cigarAlignment != null ? !cigarAlignment.equals(that.cigarAlignment) : that.cigarAlignment != null)
            return false;
        if (eValue != null ? !eValue.equals(that.eValue) : that.eValue != null) return false;
        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (envelopeEnd != null ? !envelopeEnd.equals(that.envelopeEnd) : that.envelopeEnd != null) return false;
        if (envelopeStart != null ? !envelopeStart.equals(that.envelopeStart) : that.envelopeStart != null)
            return false;
        if (hmmBounds != null ? !hmmBounds.equals(that.hmmBounds) : that.hmmBounds != null) return false;
        if (hmmEnd != null ? !hmmEnd.equals(that.hmmEnd) : that.hmmEnd != null) return false;
        if (hmmStart != null ? !hmmStart.equals(that.hmmStart) : that.hmmStart != null) return false;
        if (level != null ? !level.equals(that.level) : that.level != null) return false;
        if (motifNumber != null ? !motifNumber.equals(that.motifNumber) : that.motifNumber != null) return false;
        if (pValue != null ? !pValue.equals(that.pValue) : that.pValue != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;
        if (start != null ? !start.equals(that.start) : that.start != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (eValue != null ? eValue.hashCode() : 0);
        result = 31 * result + (pValue != null ? pValue.hashCode() : 0);
        result = 31 * result + (motifNumber != null ? motifNumber.hashCode() : 0);
        result = 31 * result + (hmmStart != null ? hmmStart.hashCode() : 0);
        result = 31 * result + (hmmEnd != null ? hmmEnd.hashCode() : 0);
        result = 31 * result + (hmmBounds != null ? hmmBounds.hashCode() : 0);
        result = 31 * result + (envelopeStart != null ? envelopeStart.hashCode() : 0);
        result = 31 * result + (envelopeEnd != null ? envelopeEnd.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (cigarAlignment != null ? cigarAlignment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BK_Location{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
