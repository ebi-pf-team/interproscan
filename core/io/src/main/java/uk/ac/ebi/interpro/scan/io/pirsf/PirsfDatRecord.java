package uk.ac.ebi.interpro.scan.io.pirsf;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

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
    private boolean blastReqd = false; // Default to Blast not required for this model

    public PirsfDatRecord(String modelAccession, String modelName, String[] values, String blastReqd) {
        this.modelAccession = modelAccession;
        this.modelName = modelName;
        this.meanSeqLen = Double.parseDouble(values[INDEX_MEAN_SEQ_LEN].trim());
        this.stdDevSeqLen = Double.parseDouble(values[INDEX_STD_DEV_SEQ_LEN].trim());
        this.minScore = Double.parseDouble(values[INDEX_MIN_SCORE].trim());
        this.meanScore = Double.parseDouble(values[INDEX_MEAN_SCORE].trim());
        this.stdDevScore = Double.parseDouble(values[INDEX_STD_DEV_SCORE].trim());
        if (blastReqd != null) {
            blastReqd = blastReqd.trim();
            if (blastReqd.equalsIgnoreCase("YES")) {
                this.blastReqd = true;
            }
        }
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

    public boolean isBlastReqd() {
        return blastReqd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof PirsfDatRecord)
        {
            final PirsfDatRecord castedObj = (PirsfDatRecord) o;
            if(castedObj.getMeanScore()==getMeanScore() &&
                    castedObj.getMeanSeqLen()==getMeanSeqLen() &&
                    castedObj.getMinScore()==getMinScore() &&
                    castedObj.getModelAccession().equals(getModelAccession()) &&
                    castedObj.getModelName().equals(getModelName()) &&
                    castedObj.getStdDevScore()==getStdDevScore() &&
                    castedObj.getStdDevSeqLen()==getStdDevSeqLen()) {
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
                .append(blastReqd)
                .toHashCode();
    }


    @Override
    public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
