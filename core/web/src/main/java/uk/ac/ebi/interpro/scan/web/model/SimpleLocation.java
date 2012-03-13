package uk.ac.ebi.interpro.scan.web.model;

import java.io.Serializable;

/**
 * Location of a match.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class SimpleLocation implements Comparable<SimpleLocation>, Serializable {

    private final int start;
    private final int end;

    public SimpleLocation(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public int compareTo(SimpleLocation that) {
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
        if (o == null || getClass() != o.getClass()) return false;

        SimpleLocation that = (SimpleLocation) o;

        if (end != that.end) return false;
        if (start != that.start) return false;

        return true;
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
        sb.append("SimpleLocation");
        sb.append("{start=").append(start);
        sb.append(", end=").append(end);
        sb.append('}');
        return sb.toString();
    }
}
