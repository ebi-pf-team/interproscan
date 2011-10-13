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
public final class SimpleSignature {

    private final String ac;
    private final String name;
    private final String database;
    private final List<SimpleLocation> locations;

    public SimpleSignature(String ac, String name, String database) {
        this.ac = ac;
        this.name = name;
        this.database = database;
        this.locations  = new ArrayList<SimpleLocation>();
    }

    public String getAc() {
        return ac;
    }

    public String getName() {
        return name;
    }

    public String getDatabase() {
        return database;
    }

    public List<SimpleLocation> getLocations() {
        return locations;
    }

    public void addLocation(SimpleLocation location) {
        this.locations.add(location);
    }

    public void sort() {
        Collections.sort(locations);
    }

}
