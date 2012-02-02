package uk.ac.ebi.interpro.scan.web.model;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds data about structural matches for a member database.
 * A collection of structural match locations with the necessary class ID and domain ID data for each.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SimpleStructuralMatchData {

    private Map<String, List<String>> locationDataMap = new HashMap<String, List<String>>();

    public SimpleStructuralMatchData(String classId, String domainId) {
        List<String> domainIds = new ArrayList<String>();
        domainIds.add(domainId);
        this.locationDataMap.put(classId, domainIds);
    }

    public void addStructuralMatchData(String classId, String domainId) {
        if (locationDataMap.containsKey(classId)) {
            List<String> domainIds = locationDataMap.get(classId);
            domainIds.add(domainId);
        }
        else {
            List<String> domainIds = new ArrayList<String>();
            domainIds.add(domainId);
            locationDataMap.put(classId, domainIds);
        }
    }

    public Map<String, List<String>> getLocationDataMap() {
        return locationDataMap;
    }
}
