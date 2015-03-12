package uk.ac.ebi.interpro.scan.web.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Holds data about structural matches for a member database.
 * A collection of structural match locations with the necessary class ID and domain ID data for each.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SimpleStructuralMatchData implements Serializable {

    private Map<String, SortedSet<String>> locationDataMap = new HashMap<String, SortedSet<String>>();

    public SimpleStructuralMatchData(String classId, String domainId) {
        SortedSet<String> domainIds = new TreeSet<String>();
        domainIds.add(domainId);
        this.locationDataMap.put(classId, domainIds);
    }

    public void addStructuralMatchData(String classId, String domainId) {
        if (locationDataMap.containsKey(classId)) {
            SortedSet<String> domainIds = locationDataMap.get(classId);
            domainIds.add(domainId);
        } else {
            SortedSet<String> domainIds = new TreeSet<String>();
            domainIds.add(domainId);
            locationDataMap.put(classId, domainIds);
        }
    }

    public Map<String, SortedSet<String>> getLocationDataMap() {
        return locationDataMap;
    }
}
