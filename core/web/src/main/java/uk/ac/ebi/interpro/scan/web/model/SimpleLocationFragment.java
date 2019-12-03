package uk.ac.ebi.interpro.scan.web.model;

import uk.ac.ebi.interpro.scan.model.DCStatus;

import java.io.Serializable;

/**
 * Location of a match.
 *
 * @version $Id$
 */
public final class SimpleLocationFragment implements Comparable<SimpleLocationFragment>, Serializable {

    private final int start;
    private final int end;
    private final DCStatus dcStatus;

    public SimpleLocationFragment(int start, int end, DCStatus dcStatus) {
        this.start = start;
        this.end = end;
        this.dcStatus = dcStatus;
    }

    public SimpleLocationFragment(int start, int end) {
        this.start = start;
        this.end = end;
        this.dcStatus = DCStatus.CONTINUOUS;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public DCStatus getDcStatus() {
        return dcStatus;
    }

    @Override
    public int compareTo(SimpleLocationFragment that) {
        // Equal
        if (this == that || this.equals(that)) {
            return 0;
        }
        // Before
        if (this.getStart() < that.getStart()) {
            return -1;
        }
        // After
        if (this.getStart() > that.getStart()) {
            return 1;
        }
        // If same start, show lowest first
        if (this.getStart() == that.getStart()) {
            return this.getEnd() - that.getEnd();
        }
        // Should never get here, but...
        return this.getStart() - that.getEnd();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleLocationFragment)) return false;

        SimpleLocationFragment that = (SimpleLocationFragment) o;

        if (start != that.start) return false;
        return (end == that.end);

    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    public int getLength() {
        return Math.abs(end - start) + 1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SimpleLocationFragment");
        sb.append("{start=").append(start);
        sb.append(", end=").append(end);
        sb.append('}');
        return sb.toString();
    }
}
