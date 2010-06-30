package uk.ac.ebi.interpro.scan.model.raw;

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
public class PrintsRawMatch extends RawMatch implements Comparable<PrintsRawMatch> {

    @Column
    private String printsModelId;
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

    protected PrintsRawMatch() {
    }

    /* TODO - Use setters instead of assigning directly? */

    public PrintsRawMatch(String sequenceIdentifier, String modelAccession, String modelId,
                          String signatureLibraryRelease,
                          int locationStart, int locationEnd,
                          double evalue, String graphscan,
                          int motifCount, int motifNumber,
                          double pvalue, double score) {
        super(sequenceIdentifier, modelAccession, SignatureLibrary.PRINTS, signatureLibraryRelease, locationStart, locationEnd);
        this.printsModelId = modelId;
        this.evalue = evalue;
        this.graphscan = graphscan;
        this.motifCount = motifCount;
        this.pvalue = pvalue;
        this.score = score;
        this.motifNumber = motifNumber;
    }

    public double getEvalue() {
        return evalue;          /* TODO Use PersistenceConversion? */
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;   /* TODO Use PersistenceConversion? */
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

    public String getPrintsModelId() {
        return printsModelId;
    }

    public void setPrintsModelId(String printsModelId) {
        this.printsModelId = printsModelId;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p/>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p/>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p/>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p/>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p/>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param that the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(PrintsRawMatch that) {
        int comparison = this.getSequenceIdentifier().compareTo(that.getSequenceIdentifier());
        if (comparison == 0) {
            if (this.getEvalue() < that.getEvalue()) comparison = -1;
            else if (this.getEvalue() > that.getEvalue()) comparison = 1;
        }
        if (comparison == 0) {
            comparison = this.getModel().compareTo(that.getModel());
        }
        if (comparison == 0) {
            if (this.getLocationStart() < that.getLocationStart()) comparison = -1;
            else if (this.getLocationStart() > that.getLocationStart()) comparison = 1;
        }
        if (comparison == 0) {
            if (this.getLocationEnd() < that.getLocationEnd()) comparison = -1;
            else if (this.getLocationEnd() > that.getLocationEnd()) comparison = 1;
        }
        return comparison;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrintsRawMatch)) return false;
        if (!super.equals(o)) return false;

        PrintsRawMatch that = (PrintsRawMatch) o;

        if (Double.compare(that.evalue, evalue) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = evalue != +0.0d ? Double.doubleToLongBits(evalue) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
