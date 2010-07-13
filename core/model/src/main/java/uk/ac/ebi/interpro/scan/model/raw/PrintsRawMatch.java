package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://www.bioinf.manchester.ac.uk/dbbrowser/PRINTS/">PRINTS</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@Table(name = "prints_raw_match")
public class PrintsRawMatch extends RawMatch {

    @Column
    private double evalue;
    @Column
    private String graphscan;
    @Column
    private int motifCount;
    @Column
    private double pvalue;
    @Column
    private double score;
    @Column
    private int motifNumber;

    protected PrintsRawMatch() {
    }

    public PrintsRawMatch(String sequenceIdentifier, String model,
                          String signatureLibraryRelease,
                          int locationStart, int locationEnd,
                          double evalue, String graphscan,
                          int motifCount, int motifNumber,
                          double pvalue, double score) {
        super(sequenceIdentifier, model, SignatureLibrary.PRINTS, signatureLibraryRelease, locationStart, locationEnd);
        this.setEvalue(evalue);
        this.setGraphscan(graphscan);
        this.setMotifCount(motifCount);
        this.setPvalue(pvalue);
        this.setScore(score);
        this.setMotifNumber(motifNumber);
    }

    public double getEvalue() {
        return PersistenceConversion.get(evalue);
    }

    private void setEvalue(double evalue) {
        this.evalue = PersistenceConversion.set(evalue);
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
        return PersistenceConversion.get(pvalue);
    }

    private void setPvalue(double pvalue) {
        this.pvalue = PersistenceConversion.set(pvalue);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PrintsRawMatch that = (PrintsRawMatch) o;

        if (!PersistenceConversion.equivalent(this.getEvalue(), that.getEvalue())) return false;
        if (motifCount != that.motifCount) return false;
        if (motifNumber != that.motifNumber) return false;
        if (!PersistenceConversion.equivalent(this.getPvalue(), that.getPvalue())) return false;
        if (Double.compare(that.score, score) != 0) return false;
        if (graphscan != null ? !graphscan.equals(that.graphscan) : that.graphscan != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = evalue != +0.0d ? Double.doubleToLongBits(evalue) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (graphscan != null ? graphscan.hashCode() : 0);
        result = 31 * result + motifCount;
        temp = pvalue != +0.0d ? Double.doubleToLongBits(pvalue) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = score != +0.0d ? Double.doubleToLongBits(score) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + motifNumber;
        return result;
    }
}
