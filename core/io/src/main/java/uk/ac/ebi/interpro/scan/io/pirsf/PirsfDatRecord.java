package uk.ac.ebi.interpro.scan.io.pirsf;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

public class PirsfDatRecord implements Serializable {

    private static final int INDEX_MEAN_SEQ_LEN = 1;
    private static final int INDEX_STD_DEV_SEQ_LEN = 2;
    private static final int INDEX_MIN_SCORE = 3;
    private static final int INDEX_MEAN_SCORE = 4;
    private static final int INDEX_STD_DEV_SCORE = 5;

    private String modelAccession;
    private String modelName;
    private double meanSeqLen;
    private double stdDevSeqLen;
    private double minScore;
    private double meanScore;
    private double stdDevScore;
    private final Set<String> subfamilies = new HashSet<String>();

    public PirsfDatRecord(String modelAccession) {
        this.modelAccession = modelAccession;
    }

    public void setValues(Matcher matcher) {
        this.meanSeqLen = Double.parseDouble(matcher.group(INDEX_MEAN_SEQ_LEN));
        this.stdDevSeqLen = Double.parseDouble(matcher.group(INDEX_STD_DEV_SEQ_LEN));
        this.minScore = Double.parseDouble(matcher.group(INDEX_MIN_SCORE));
        this.meanScore = Double.parseDouble(matcher.group(INDEX_MEAN_SCORE));
        this.stdDevScore = Double.parseDouble(matcher.group(INDEX_STD_DEV_SCORE));
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

    public Set<String> getSubfamilies() {
        return subfamilies;
    }

    public void addSubFamily(String subfamily) {
        this.subfamilies.add(subfamily);
    }

    public void setModelAccession(String modelAccession) {
        this.modelAccession = modelAccession;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof PirsfDatRecord) {
            final PirsfDatRecord castedObj = (PirsfDatRecord) o;
            return castedObj.getMeanScore() == getMeanScore() &&
                    castedObj.getMeanSeqLen() == getMeanSeqLen() &&
                    castedObj.getMinScore() == getMinScore() &&
                    castedObj.getModelAccession().equals(getModelAccession()) &&
                    castedObj.getModelName().equals(getModelName()) &&
                    castedObj.getStdDevScore() == getStdDevScore() &&
                    castedObj.getStdDevSeqLen() == getStdDevSeqLen() &&
                    castedObj.getSubfamilies().equals(getSubfamilies());
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
                .append(subfamilies)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
