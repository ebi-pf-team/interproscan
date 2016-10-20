package uk.ac.ebi.interpro.scan.web.model;

import java.io.Serializable;
import java.util.*;

public class SimpleSite implements Comparable<SimpleSite>, Serializable {

    private final long id;
    private final String description;
    private final int numLocations;
    private final SimpleSignature signature;
    private final SimpleEntry entry;
    private final SortedSet<SimpleSiteLocation> siteLocations = new TreeSet<>();

    public SimpleSite(long id, String description, int numLocations, SimpleSignature signature, SimpleEntry entry) {
        this.id = id;
        this.description = description;
        this.numLocations = numLocations;
        this.signature = signature;
        this.entry = entry;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getNumLocations() {
        return numLocations;
    }

    public SimpleSignature getSignature() {
        return signature;
    }

    public SimpleEntry getEntry() {
        return entry;
    }

    public SortedSet<SimpleSiteLocation> getSiteLocations() {
        return siteLocations;
    }

    public void addSiteLocation(SimpleSiteLocation siteLocation) {
        this.siteLocations.add(siteLocation);
    }

    public int getFirstStart() {
        return siteLocations.first().getLocation().getStart();
    }

    public int getLastEnd() {
        return siteLocations.last().getLocation().getEnd();
    }

    @Override
    public int compareTo(SimpleSite that) {
        // Equal
        if (this == that || this.equals(that)) {
            return 0;
        }
        // Before
        if (this.getFirstStart() < that.getFirstStart()) {
            return -1;
        }
        // After
        if (this.getFirstStart() > that.getFirstStart()) {
            return 1;
        }
        // If same start, show lowest first
        if (this.getFirstStart() == that.getFirstStart()) {
            return this.getLastEnd() - that.getLastEnd();
        }
        // Should never get here, but...
        return this.getFirstStart() - that.getLastEnd();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleSite that = (SimpleSite) o;

        if (id != that.id) return false;
        if (!description.equals(that.description)) return false;
        if (numLocations != that.numLocations) return false;
        if (!signature.getAc().equals(that.signature.getAc())) return false;
        if (!entry.equals(that.entry)) return false;
        if (!siteLocations.equals(siteLocations)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.getFirstStart();
        result = 73 * result + this.getLastEnd();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SimpleSite");
        sb.append("{id=").append(id);
        sb.append(", description=").append(description);
        sb.append(", numLocations=").append(numLocations);
        sb.append(", signature=").append(signature.getAc());
        sb.append(", entry=").append(entry);
        sb.append('}');
        return sb.toString();
    }

}
