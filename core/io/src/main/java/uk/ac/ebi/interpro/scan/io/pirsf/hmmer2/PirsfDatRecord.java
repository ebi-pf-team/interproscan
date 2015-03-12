package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PirsfDatRecord implements Serializable {

    private static final int INDEX_MEAN_SEQ_LEN = 0;
    private static final int INDEX_STD_DEV_SEQ_LEN = 1;
    private static final int INDEX_MIN_SCORE = 2;
    private static final int INDEX_MEAN_SCORE = 3;
    private static final int INDEX_STD_DEV_SCORE = 4;

    private String modelAccession;
    private String modelName;
    private double meanSeqLen;
    private double stdDevSeqLen;
    private double minScore;
    private double meanScore;
    private double stdDevScore;
    private boolean blastRequired = false; // Default to Blast not required for this model
    private Set<String> subFamilies = new HashSet<String>();

    public PirsfDatRecord(String modelAccession) {
        this.modelAccession = modelAccession;
    }

    public PirsfDatRecord(String modelAccession, String modelName, String[] values, boolean blastRequired) {
        this(modelAccession, modelName, values, blastRequired, new HashSet<String>());
    }

    public PirsfDatRecord(String modelAccession, String modelName, String[] values, boolean blastRequired, Set<String> subFamilies) {
        this.modelAccession = modelAccession;
        this.modelName = modelName;
        setValues(values);
        this.blastRequired = blastRequired;
        addSubFamilies(subFamilies);
    }

    public void setValues(String[] values) {
        this.meanSeqLen = Double.parseDouble(values[INDEX_MEAN_SEQ_LEN].trim());
        this.stdDevSeqLen = Double.parseDouble(values[INDEX_STD_DEV_SEQ_LEN].trim());
        this.minScore = Double.parseDouble(values[INDEX_MIN_SCORE].trim());
        this.meanScore = Double.parseDouble(values[INDEX_MEAN_SCORE].trim());
        this.stdDevScore = Double.parseDouble(values[INDEX_STD_DEV_SCORE].trim());
    }

    public String getModelAccession() {
        return modelAccession;
    }

    public String getModelName() {
        return modelName;
    }

    public double getMeanSeqLen() {
        return meanSeqLen;
    }

    public double getStdDevSeqLen() {
        return stdDevSeqLen;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMeanScore() {
        return meanScore;
    }

    public double getStdDevScore() {
        return stdDevScore;
    }

    public boolean isBlastRequired() {
        return blastRequired;
    }

    public Set<String> getSubFamilies() {
        return subFamilies;
    }

    private void addSubFamilies(Set<String> subFamilies) {
        for (String subfamily : subFamilies) {
            addSubFamily(subfamily);
        }
    }

    public void addSubFamily(String subfamily) {
        this.subFamilies.add(subfamily);
    }

    public void setModelAccession(String modelAccession) {
        this.modelAccession = modelAccession;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setMeanSeqLen(double meanSeqLen) {
        this.meanSeqLen = meanSeqLen;
    }

    public void setStdDevSeqLen(double stdDevSeqLen) {
        this.stdDevSeqLen = stdDevSeqLen;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public void setMeanScore(double meanScore) {
        this.meanScore = meanScore;
    }

    public void setStdDevScore(double stdDevScore) {
        this.stdDevScore = stdDevScore;
    }

    public void setBlastRequired(boolean blastRequired) {
        this.blastRequired = blastRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof PirsfDatRecord) {
            final PirsfDatRecord castedObj = (PirsfDatRecord) o;
            if (castedObj.getMeanScore() == getMeanScore() &&
                    castedObj.getMeanSeqLen() == getMeanSeqLen() &&
                    castedObj.getMinScore() == getMinScore() &&
                    castedObj.getModelAccession().equals(getModelAccession()) &&
                    castedObj.getModelName().equals(getModelName()) &&
                    castedObj.getStdDevScore() == getStdDevScore() &&
                    castedObj.getStdDevSeqLen() == getStdDevSeqLen() &&
                    castedObj.getSubFamilies().equals(getSubFamilies()) &&
                    castedObj.isBlastRequired() == isBlastRequired()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(25, 11)
                .append(modelAccession)
                .append(modelName)
                .append(meanSeqLen)
                .append(stdDevSeqLen)
                .append(minScore)
                .append(meanScore)
                .append(stdDevScore)
                .append(blastRequired)
                .append(subFamilies)
                .toHashCode();
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
