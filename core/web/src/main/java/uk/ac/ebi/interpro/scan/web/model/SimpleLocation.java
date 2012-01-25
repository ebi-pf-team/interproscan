package uk.ac.ebi.interpro.scan.web.model;

/**
 * TODO: Add description
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class SimpleLocation implements Comparable<SimpleLocation> {

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

}
