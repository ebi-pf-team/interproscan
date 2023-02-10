package uk.ac.ebi.interpro.scan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.builder.EqualsBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlType(name = "FunFamHmmer3MatchType")
public class FunFamHmmer3Match extends Match<FunFamHmmer3Match.FunFamHmmer3Location> {
    @Column(nullable = false)
    private double evalue;

    @Column(nullable = false)
    private double score;

    protected FunFamHmmer3Match() {

    }

    public FunFamHmmer3Match(Signature signature, String signatureModels, double score, double evalue, Set<FunFamHmmer3Match.FunFamHmmer3Location> locations) {
        super(signature, signatureModels, locations);
        setEvalue(evalue);
        setScore(score);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final Set<FunFamHmmer3Location> clonedLocations = new HashSet<FunFamHmmer3Location>(this.getLocations().size());
        for (FunFamHmmer3Location location: this.getLocations()) {
            clonedLocations.add((FunFamHmmer3Location) location.clone());
        }

        return new FunFamHmmer3Match(this.getSignature(), this.getSignatureModels(), this.getScore(), this.getEvalue(), clonedLocations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunFamHmmer3Match)) return false;
        final FunFamHmmer3Match m = (FunFamHmmer3Match) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(this.getScore(), m.getScore())
                .isEquals() &&
                PersistenceConversion.equivalent(this.getEvalue(), m.getEvalue());
    }

    @XmlAttribute(required = true)
    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    @XmlAttribute(name = "score", required = true)
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public static class FunFamHmmer3Location extends Hmmer3Match.Hmmer3Location {
        @Column(nullable = false, name = "hmmer3_seq_start")
        @JsonIgnore
        private int hmmer3SeqStart;

        @Column(nullable = false, name = "hmmer3_seq_end")
        @JsonIgnore
        private int hmmer3SeqEnd;

        protected FunFamHmmer3Location() {
        }

        public FunFamHmmer3Location(int start, int end, double score, double evalue, int hmmStart, int hmmEnd,
                                    int hmmLength, HmmBounds hmmBounds, int envelopeStart, int envelopeEnd,
                                    boolean postProcessed, DCStatus dcStatus, String alignment,
                                    int resolvedStart, int resolvedEnd) {
            super(resolvedStart, resolvedEnd, score, evalue, hmmStart, hmmEnd, hmmLength, hmmBounds, envelopeStart, envelopeEnd, postProcessed, dcStatus, alignment);
            setHmmer3SeqStart(start);
            setHmmer3SeqEnd(end);
        }

        @XmlTransient
        public int getHmmer3SeqStart() {
            return hmmer3SeqStart;
        }

        public void setHmmer3SeqStart(int hmmer3SeqStart) {
            this.hmmer3SeqStart = hmmer3SeqStart;
        }

        @XmlTransient
        public int getHmmer3SeqEnd() {
            return hmmer3SeqEnd;
        }

        public void setHmmer3SeqEnd(int hmmer3SeqEnd) {
            this.hmmer3SeqEnd = hmmer3SeqEnd;
        }
    }
}
