package uk.ac.ebi.interpro.scan.model.raw;

/**
 * TODO: Add class description
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public final class FingerPrintsRawMatch extends RawMatch {

    private double evalue;
    private String graphscan;
    private int motifCount;
    private double pvalue;
    private double score;
    private int motifNumber;

    public FingerPrintsRawMatch() { }

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public String getGraphscan() {
        return graphscan;
    }

    public void setGraphscan(String graphscan) {
        this.graphscan = graphscan;
    }

    public int getMotifCount() {
        return motifCount;
    }

    public void setMotifCount(int motifCount) {
        this.motifCount = motifCount;
    }

    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getMotifNumber() {
        return motifNumber;
    }

    public void setMotifNumber(int motifNumber) {
        this.motifNumber = motifNumber;
    }
}
