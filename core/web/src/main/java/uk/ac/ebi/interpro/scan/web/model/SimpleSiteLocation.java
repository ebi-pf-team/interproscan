package uk.ac.ebi.interpro.scan.web.model;

import java.io.Serializable;

public class SimpleSiteLocation implements Comparable<SimpleSiteLocation>, Serializable {

    private final String residue;
    private final SimpleLocation location;

    public SimpleSiteLocation(String residue, SimpleLocation location) {
        this.residue = residue;
        this.location = location;
    }

    public String getResidue() {
        return residue;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    @Override
    public int compareTo(SimpleSiteLocation that) {
        // Equal
        if (this == that || this.equals(that)) {
            return 0;
        }
        int c = this.getLocation().compareTo(that.getLocation());
        if (c == 0) {
            return this.getResidue().compareTo(that.getResidue());
        }
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleSiteLocation that = (SimpleSiteLocation) o;

        if (!residue.equals(that.residue)) return false;
        if (!this.location.equals(that.location)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.location.getStart();
        result = 71 * result + this.location.getEnd();
        return result;
    }

    public int getLength() {
        return Math.abs(this.location.getEnd() - this.location.getStart()) + 1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SimpleSiteLocation");
        sb.append("{residue=").append(residue);
        sb.append(", start=").append(this.location.getStart());
        sb.append(", end=").append(this.location.getEnd());
        sb.append('}');
        return sb.toString();
    }
}
