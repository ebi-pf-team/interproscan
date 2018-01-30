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
public class BerkeleyLocation implements Comparable<BerkeleyLocation> {

    private Integer start;

    private Integer end;

    private Double score;

    private Double eValue;

    private Double pValue;

    private Integer motifNumber;

    private Integer hmmStart;

    private Integer hmmEnd;

    private Integer hmmLength;

    private String hmmBounds;

    private Integer envelopeStart;

    private Integer envelopeEnd;

    private String level;

    private String cigarAlignment;

    public BerkeleyLocation() {
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

    public Integer getHmmLength() {
        return hmmLength;
    }

    public void setHmmLength(Integer hmmLength) {
        this.hmmLength = hmmLength;
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

        BerkeleyLocation that = (BerkeleyLocation) o;

        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (cigarAlignment != null ? !cigarAlignment.equals(that.cigarAlignment) : that.cigarAlignment != null) {
            return false;
        }
        if (eValue != null ? !eValue.equals(that.eValue) : that.eValue != null) return false;
        if (envelopeEnd != null ? !envelopeEnd.equals(that.envelopeEnd) : that.envelopeEnd != null) return false;
        if (envelopeStart != null ? !envelopeStart.equals(that.envelopeStart) : that.envelopeStart != null)
            return false;
        if (hmmBounds != null ? !hmmBounds.equals(that.hmmBounds) : that.hmmBounds != null) return false;
        if (hmmEnd != null ? !hmmEnd.equals(that.hmmEnd) : that.hmmEnd != null) return false;
        if (hmmStart != null ? !hmmStart.equals(that.hmmStart) : that.hmmStart != null) return false;
        if (hmmLength != null ? !hmmLength.equals(that.hmmLength) : that.hmmLength != null) return false;
        if (level != null ? !level.equals(that.level) : that.level != null) return false;
        if (motifNumber != null ? !motifNumber.equals(that.motifNumber) : that.motifNumber != null) return false;
        if (pValue != null ? !pValue.equals(that.pValue) : that.pValue != null) return false;
        if (score != null ? !score.equals(that.score) : that.score != null) return false;


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
        result = 31 * result + (hmmLength != null ? hmmLength.hashCode() : 0);
        result = 31 * result + (hmmBounds != null ? hmmBounds.hashCode() : 0);
        result = 31 * result + (envelopeStart != null ? envelopeStart.hashCode() : 0);
        result = 31 * result + (envelopeEnd != null ? envelopeEnd.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (cigarAlignment != null ? cigarAlignment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BerkeleyLocation{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    /**
     * Attempts to sort as follows:
     * <p/>
     * If equal (== or .equals) return 0.
     * Sort on start position
     * Sort on end position
     * Sort on envelope start
     * Sort on envelope end
     * Sort on HmmStart
     * Sort on HmmEnd
     * Sort on Score
     * Sort on Evalue
     *
     * @param that the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(BerkeleyLocation that) {
        if (this == that || this.equals(that)) return 0;
        if (this.getStart() != null && that.getStart() != null) {
            if (this.getStart() < that.getStart()) return -1;
            if (this.getStart() > that.getStart()) return 1;
        }
        if (this.getEnd() != null && that.getEnd() != null) {
            if (this.getEnd() < that.getEnd()) return -1;
            if (this.getEnd() > that.getEnd()) return 1;
        }
        if (this.getEnvelopeStart() != null && that.getEnvelopeStart() != null) {
            if (this.getEnvelopeStart() < that.getEnvelopeStart()) return -1;
            if (this.getEnvelopeStart() > that.getEnvelopeStart()) return 1;
        }
        if (this.getEnvelopeEnd() != null && that.getEnvelopeEnd() != null) {
            if (this.getEnvelopeEnd() < that.getEnvelopeEnd()) return -1;
            if (this.getEnvelopeEnd() > that.getEnvelopeEnd()) return 1;
        }
        if (this.getHmmStart() != null && that.getHmmStart() != null) {
            if (this.getHmmStart() < that.getHmmStart()) return -1;
            if (this.getHmmStart() > that.getHmmStart()) return 1;
        }
        if (this.getHmmEnd() != null && that.getHmmEnd() != null) {
            if (this.getHmmEnd() < that.getHmmEnd()) return -1;
            if (this.getHmmEnd() > that.getHmmEnd()) return 1;
        }
        if (this.getScore() != null && that.getScore() != null) {
            if (this.getScore() < that.getScore()) return -1;
            if (this.getScore() > that.getScore()) return 1;
        }
        if (this.geteValue() != null && that.geteValue() != null) {
            if (this.geteValue() < that.geteValue()) return -1;
            if (this.geteValue() > that.geteValue()) return 1;
        }
        throw new IllegalStateException("Trying to compare a BerkeleyLocation that has no state.  This: " + this + "\n\nThat: " + that);
    }
}
