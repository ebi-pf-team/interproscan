package uk.ac.ebi.interpro.scan.web.model;

import uk.ac.ebi.interpro.scan.web.io.svg.MatchLocationSvgElementBuilder;

import java.io.Serializable;
import java.util.*;

/**
 * TODO: Add description
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class SimpleSignature implements Comparable<SimpleSignature>, Serializable {

    private final String ac;
    private final String name;
    private final MatchDataSource dataSource;
    private final List<SimpleLocation> locations;
    private Map<String, List<SimpleLocation>> featureLocationsMap;

    public SimpleSignature(String ac, String name, String databaseName) {
        this.ac = ac;
        this.name = name;
        this.dataSource = MatchDataSource.parseName(databaseName);
        this.locations = new ArrayList<>();
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

    // For use in Freemarker
    public Map<String, List<SimpleLocation>> getFeatureLocationsMap() {
        if (dataSource.equals(MatchDataSource.MOBIDB_LITE) || dataSource.equals(MatchDataSource.MOBIDB)) {
            featureLocationsMap = new HashMap<>();
            for (SimpleLocation location : locations) {
                String feature = location.getFeature();
                if (feature == null) {
                    feature = "";
                }
                if (featureLocationsMap.containsKey(feature)) {
                    List<SimpleLocation> l = featureLocationsMap.get(feature);
                    l.add(location);
                } else {
                    List<SimpleLocation> l = new ArrayList<>();
                    l.add(location);
                    featureLocationsMap.put(feature, l);
                }
            }
            return featureLocationsMap;
        }
        return null;
    }
}