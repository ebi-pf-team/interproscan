package uk.ac.ebi.interpro.scan.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* TODO: Add description
*
* @author Antony Quinn
* @version $Id$
*/
public final class SimpleSignature implements Comparable<SimpleSignature>  {

    private final String ac;
    private final String name;
    private final MatchDataSource dataSource;
    private final List<SimpleLocation> locations;

    public SimpleSignature(String ac, String name, String databaseName) {
        this.ac = ac;
        this.name = name;
        this.dataSource = MatchDataSource.parseName(databaseName);
        this.locations  = new ArrayList<SimpleLocation>();
    }

    public String getAc() {
        return ac;
    }

    public String getName() {
        return name;
    }

    public MatchDataSource getDataSource() {
        return dataSource;
    }

    public List<SimpleLocation> getLocations() {
        return locations;
    }

    public void addLocation(SimpleLocation location) {
        this.locations.add(location);
    }

    @Override
    public int compareTo(SimpleSignature that) {
        if (this == that || this.equals(that)) {
            return 0;
        }
        return Collections.min(this.locations).compareTo(Collections.min(that.locations));
    }

}
