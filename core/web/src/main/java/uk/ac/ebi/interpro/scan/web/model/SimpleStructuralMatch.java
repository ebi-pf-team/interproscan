package uk.ac.ebi.interpro.scan.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* Holds data relating to a structural match.
*
* @author Antony Quinn
* @version $Id$
*/
public final class SimpleStructuralMatch implements Comparable<SimpleStructuralMatch> {

    private final String databaseName;
    private final String domainId;
    private final String classId;
    private List<SimpleLocation> locations = new ArrayList<SimpleLocation>();

    public SimpleStructuralMatch(String databaseName, String domainId, String classId) {
        this.databaseName = databaseName;
        this.domainId = domainId;
        this.classId = classId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDomainId() {
        return domainId;
    }

    public String getClassId() {
        return classId;
    }

    public List<SimpleLocation> getLocations() {
        return locations;
    }

    public void addLocation(SimpleLocation location) {
        this.locations.add(location);
    }

    @Override public int compareTo(SimpleStructuralMatch that) {
        if (this == that) {
            return 0;
        }
        return Collections.min(this.locations).compareTo(Collections.min(that.locations));
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SimpleStructuralMatch))
            return false;
        if (!this.databaseName.equals(((SimpleStructuralMatch)o).databaseName)) {
            return false;
        }
        if (!this.domainId.equals(((SimpleStructuralMatch)o).domainId)) {
            return false;
        }
        if (!this.classId.equals(((SimpleStructuralMatch)o).classId)) {
            return false;
        }
        return true;
    }


}
