package uk.ac.ebi.interpro.scan.web.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Location of a match.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class SimpleLocation implements Comparable<SimpleLocation>, Serializable {

    private final int start;
    private final int end;
    private String models = null; // Required for Gene3D and SUPERFAMILY
    private String feature = null; // For sequence features only (MobiDB)
    private Set<SimpleLocationFragment> fragments;

    public SimpleLocation(int start, int end, String models, String feature) {
        this(start, end, models);
        this.feature = feature;
    }

    public SimpleLocation(int start, int end, String models) {
        this(start, end);
        this.models = models;
    }

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

    public Set<SimpleLocationFragment> getFragments() {
        return fragments;
    }

    public void addFragment(SimpleLocationFragment fragment) {
        if (fragments == null) {
            fragments = new HashSet<>();
        }
        fragments.add(fragment);
    }

    public String getModels() {
        return models;
    }

    public String getFeature() {
        return feature;
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
        if (!(o instanceof SimpleLocation)) return false;

        SimpleLocation that = (SimpleLocation) o;

        if (start != that.start) return false;
        if (end != that.end) return false;
        if (models != null ? !models.equals(that.models) : that.models != null) return false;
        return feature != null ? feature.equals(that.feature) : that.feature == null;

    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + (models != null ? models.hashCode() : 0);
        result = 31 * result + (feature != null ? feature.hashCode() : 0);
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
        sb.append(", models=").append(models);
        sb.append(", feature=").append(feature);
        sb.append('}');
        return sb.toString();
    }
}
