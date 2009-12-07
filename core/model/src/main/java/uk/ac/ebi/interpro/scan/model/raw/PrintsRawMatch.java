package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name="prints_raw_match")
public class PrintsRawMatch extends RawMatch {

    @Column
    private double evalue;
    @Column(length = 15)
    private String graphscan;
    @Column
    private int motifCount;
    @Column
    private double pvalue;
    @Column
    private double score;
    @Column
    private int motifNumber;

    protected PrintsRawMatch() { }    

    public PrintsRawMatch(String sequenceIdentifier, String model,
                          String signatureLibraryName, String signatureLibraryRelease,
                          int locationStart, int locationEnd,
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
