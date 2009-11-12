package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public class PrintsRawMatch extends RawMatch {

    private double evalue;
    private String graphscan;
    private int motifCount;
    private double pvalue;
    private double score;
    private int motifNumber;

    protected PrintsRawMatch() { }    

    public PrintsRawMatch(String sequenceIdentifier, String model,
                          String signatureLibraryName, String signatureLibraryRelease,
                          long locationStart, long locationEnd,
                          double evalue, String graphscan,
                          int motifCount, int motifNumber, 
                          double pvalue, double score,
                          String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, generator);
        this.evalue = evalue;
        this.graphscan = graphscan;
        this.motifCount = motifCount;
        this.pvalue = pvalue;
        this.score = score;
        this.motifNumber = motifNumber;
    }

    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public String getGraphscan() {
        return graphscan;
    }

    private void setGraphscan(String graphscan) {
        this.graphscan = graphscan;
    }

    public int getMotifCount() {
        return motifCount;
    }

    private void setMotifCount(int motifCount) {
        this.motifCount = motifCount;
    }

    public double getPvalue() {
        return pvalue;
    }

    private void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getScore() {
        return score;
    }

    private void setScore(double score) {
        this.score = score;
    }

    public int getMotifNumber() {
        return motifNumber;
    }

    private void setMotifNumber(int motifNumber) {
        this.motifNumber = motifNumber;
    }
}
