package uk.ac.ebi.interpro.scan.web.model;

import java.util.*;

/**
 * A wrapper object that associates a member database with it's structural matches.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class SimpleStructuralDatabase implements Comparable<SimpleStructuralDatabase> {

    private final String databaseName; // Unique structural member database name
    private Map<SimpleLocation, SimpleStructuralMatchData> structuralMatches = new HashMap<SimpleLocation, SimpleStructuralMatchData>();

    public SimpleStructuralDatabase(String databaseName) {
        this.databaseName = databaseName;
    }

    public void addStructuralMatch(String classId, String domainId, SimpleLocation location) {
        if (structuralMatches.containsKey(location)) {
            // There is already a structural match for this database and location, therefore add to the existing data
            SimpleStructuralMatchData structuralMatchData = structuralMatches.get(location);
            structuralMatchData.addStructuralMatchData(classId, domainId);
        }
        else {
            SimpleStructuralMatchData structuralMatchData = new SimpleStructuralMatchData(classId, domainId);
            structuralMatches.put(location, structuralMatchData);
        }

    }


    public String getDatabaseName() {
        return databaseName;
    }

    public Map<SimpleLocation, SimpleStructuralMatchData> getStructuralMatches() {
        return structuralMatches;
    }

    @Override
    public int compareTo(SimpleStructuralDatabase that) {
        if (this == that) {
            return 0;
        }
        return this.databaseName.compareTo(that.getDatabaseName());
    }
}
