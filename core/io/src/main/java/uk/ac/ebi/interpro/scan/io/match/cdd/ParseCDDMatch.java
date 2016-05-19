package uk.ac.ebi.interpro.scan.io.match.cdd;

import java.io.Serializable;

/**
 * Model class representing a single cdd match.
 *
 * @author Gift Nuka
 * @version $Id: ParseCDDMatch.java,v 1.1 2015/12/16 14:01:17 nuka Exp $
 * @since 1.0-SNAPSHOT
 */
public class ParseCDDMatch implements Serializable {

    private String proteinDatabaseIdentifier;

    private int startCoordinate;

    private int stopCoordinate;

    public ParseCDDMatch(String proteinDatabaseIdentifier, int startCoordinate, int stopCoordinate) {
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
