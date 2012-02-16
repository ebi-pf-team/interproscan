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

    private final MatchDataSource dataSource; // Structural member database details (name, description etc)
    private Map<SimpleLocation, SimpleStructuralMatchData> structuralMatches = new HashMap<SimpleLocation, SimpleStructuralMatchData>();

    public SimpleStructuralDatabase(MatchDataSource dataSource) {
        this.dataSource = dataSource;
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


    public MatchDataSource getDataSource() {
        return dataSource;
    }

    public Map<SimpleLocation, SimpleStructuralMatchData> getStructuralMatches() {
        return structuralMatches;
    }

    /**
     * Order alphabetically by database name.
     */
    @Override
    public int compareTo(SimpleStructuralDatabase that) {
        if (this == that) {
            return 0;
        }
        return this.dataSource.name().compareTo(that.getDataSource().name());
    }
}
