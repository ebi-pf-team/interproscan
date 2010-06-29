package uk.ac.ebi.interpro.scan.io.match.coils;

/**
 * Model class representing a single coils match.
 *
 * @author Phil Jones
 * @version $Id: ParseCoilsMatch.java,v 1.1 2009/11/25 14:01:17 pjones Exp $
 * @since 1.0-SNAPSHOT
 */
public class ParseCoilsMatch {

    private String proteinDatabaseIdentifier;

    private int startCoordinate;

    private int stopCoordinate;

    public ParseCoilsMatch(String proteinDatabaseIdentifier, int startCoordinate, int stopCoordinate) {
        this.proteinDatabaseIdentifier = proteinDatabaseIdentifier;
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = stopCoordinate;
    }

    public String getProteinDatabaseIdentifier() {
        return proteinDatabaseIdentifier;
    }

    public int getStartCoordinate() {
        return startCoordinate;
    }

    public int getEndCoordinate() {
        return stopCoordinate;
    }
}
