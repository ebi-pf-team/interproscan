package uk.ac.ebi.interpro.scan.web.model;

import uk.ac.ebi.interpro.scan.web.io.svg.MatchLocationSvgElementBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add description
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class SimpleSignature implements Comparable<SimpleSignature>, Serializable {

    private final String ac;
    private final String name;
    private final String models;
    private final MatchDataSource dataSource;
    private final List<SimpleLocation> locations;

    public SimpleSignature(String ac, String name, String models, String databaseName) {
        this.ac = ac;
        this.name = name;
        this.models = models;
        this.dataSource = MatchDataSource.parseName(databaseName);
        this.locations = new ArrayList<>();
    }


    public String getAc() {
        return ac;
    }

    public String getName() {
        return name;
    }

    public String getModels() {
        return models;
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
        int comparison = this.getAc().compareTo(that.getAc());
        if (comparison == 0) {
            comparison = Collections.min(this.locations).compareTo(Collections.min(that.locations));
        }
        return comparison;
    }

    public String getMatchLocationsViewSvg(final int proteinLength, final Map<String, Integer> entryColourMap,
                                           final String entryType, final String entryAccession, final String scale) {
        return new MatchLocationSvgElementBuilder(this).build(proteinLength, entryColourMap, entryType, entryAccession, scale).toString();
    }
}