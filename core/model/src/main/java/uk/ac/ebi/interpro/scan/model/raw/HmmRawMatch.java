package uk.ac.ebi.interpro.scan.model.raw;

//import javax.persistence.Column;
//import javax.persistence.InheritanceType;
//import javax.persistence.Inheritance;
import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class HmmRawMatch extends RawMatch {

   // TODO: evalue and score can be calculated -- do we need to store? Needs testing
    //@Column(name="EVALUE",nullable = false, updatable = false)
    protected double fullSequenceEvalue;

   //@Column(name="SCORE",nullable = false, updatable = false)
    protected double sequenceScore;

    private Double fullSequenceBias;


    /**
    * Populated from the 'this domain' c-Evalue column
    */
   private double domainCEValue;

   /**
    * Populated from the 'this domain' i-Evalue column
    */
   private double domainIEValue;

   /**
    * Populated from the 'this domain' score column
    * Called 'score' for consistency with the res t of Onion code.
    */
   private double score;

   /**
    * Populated from the 'this domain' bias column
    */
   private Double domainBias;


    //@Column(name="HMM_START")
    protected long hmmStart;
    //@Column(name="HMM_END")
    protected long hmmEnd;
    //@Column(name="HMM_BOUNDS")
    protected String hmmBounds;
    /**
        * Populated from the 'ali coord' from column
        */
       private int alignmentFrom;


    /**
        * Populated from the 'ali coord' to column
        */
       private int alignmentTo;

       /**
        * Populated from the 'env coord' from column
        */
       private int envelopeFrom;

       /**
        * Populated from the 'env coord' to column
        */
       private int envelopeTo;

       /**
        * Populated from the acc column
        */
       private double acc;

       /**
        * Populated from the description of target column
        */
       private String descriptionOfTarget;

       private Integer releaseMajor;

       private Integer releaseMinor;



    //@Column(name="LOCATION_EVALUE")
    protected double locationEvalue;
    //@Column(name="SEQ_SCORE")
   protected double locationScore;
    //@Column(name="ALIGNMENT")
   protected String cigarAlignment;   // CIGAR format

    public HmmRawMatch() { }
    public HmmRawMatch(String seqIdentifier, String model,String dbname,String dbversion, String generator, long start, long end) {
        super(seqIdentifier,model,dbname,dbversion,generator,start,end);
    }
    public HmmRawMatch(String model) {
        super(model);
    }

    public String getCigarAlignment() {
        return cigarAlignment;
    }

    public void setCigarAlignment(String cigarAlignment) {
        this.cigarAlignment = cigarAlignment;
    }

     public double getFullSequenceEvalue() {
        return fullSequenceEvalue;
    }

    public void setFullSequenceEvalue(double fullSequenceEvalue) {
        this.fullSequenceEvalue = fullSequenceEvalue;
    }

    public double getSequenceScore() {
        return sequenceScore;
    }

    public void setSequenceScore(double sequenceScore) {
        this.sequenceScore = sequenceScore;
    }
    public Double getFullSequenceBias() {
           return fullSequenceBias;
       }

       public void setFullSequenceBias(Double fullSequenceBias) {
           this.fullSequenceBias = fullSequenceBias;
       }

       public Double getDomainBias() {
           return domainBias;
       }

       public void setDomainBias(Double domainBias) {
           this.domainBias = domainBias;
       }

       public double getDomainCEValue() {
           return domainCEValue;
       }

       public void setDomainCEValue(double domainCEValue) {
           this.domainCEValue = domainCEValue;
       }

       public double getDomainIEValue() {
           return domainIEValue;
       }

       public void setDomainIEValue(double domainIEValue) {
           this.domainIEValue = domainIEValue;
       }

       public double getScore() {
           return score;
       }

       public void setScore(double score) {
           this.score = score;
       }
    

    public long getHmmStart() {
        return hmmStart;
    }

    public void setHmmStart(long hmmStart) {
        this.hmmStart = hmmStart;
    }

    public long getHmmEnd() {
        return hmmEnd;
    }

    public void setHmmEnd(long hmmEnd) {
        this.hmmEnd = hmmEnd;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    public void setHmmBounds(String hmmBounds) {
        this.hmmBounds = hmmBounds;
    }

    public double getLocationEvalue() {
        return locationEvalue;
    }

    public void setLocationEvalue(double locationEvalue) {
        this.locationEvalue = locationEvalue;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }
    public int getAlignmentFrom() {
           return alignmentFrom;
       }

       public void setAlignmentFrom(int alignmentFrom) {
           this.alignmentFrom = alignmentFrom;
       }

       public int getAlignmentTo() {
           return alignmentTo;
       }

       public void setAlignmentTo(int alignmentTo) {
           this.alignmentTo = alignmentTo;
       }

       public int getEnvelopeFrom() {
           return envelopeFrom;
       }

       public void setEnvelopeFrom(int envelopeFrom) {
           this.envelopeFrom = envelopeFrom;
       }

       public int getEnvelopeTo() {
           return envelopeTo;
       }

       public void setEnvelopeTo(int envelopeTo) {
           this.envelopeTo = envelopeTo;
       }

       public double getAcc() {
           return acc;
       }

       public void setAcc(double acc) {
           this.acc = acc;
       }

       public String getDescriptionOfTarget() {
           return descriptionOfTarget;
       }

       public void setDescriptionOfTarget(String descriptionOfTarget) {
           this.descriptionOfTarget = descriptionOfTarget;
       }

       public Integer getReleaseMajor() {
           return releaseMajor;
       }

       public void setReleaseMajor(Integer releaseMajor) {
           this.releaseMajor = releaseMajor;
       }

       public Integer getReleaseMinor() {
           return releaseMinor;
       }

       public void setReleaseMinor(Integer releaseMinor) {
           this.releaseMinor = releaseMinor;
       }
    

}
