package uk.ac.ebi.interpro.scan.io.match.coils;

/**
 * Model class representing a single coils match.
 *
 * @author Phil Jones
 * @version $Id: CoilMatch.java,v 1.1 2009/11/25 14:01:17 pjones Exp $
 * @since 1.0-SNAPSHOT
 */
public class CoilMatch {

    private String proteinAccession;

    private int startCoordinate;

    private int stopCoordinate;

    public CoilMatch(String proteinAccession, int startCoordinate, int stopCoordinate) {
        this.proteinAccession = proteinAccession;
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = stopCoordinate;
    }

    public String getProteinAccession() {
        return proteinAccession;
    }

    public int getStartCoordinate() {
        return startCoordinate;
    }

    public int getEndCoordinate() {
        return stopCoordinate;
    }
}
