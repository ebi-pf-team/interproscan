package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * Models a match based upon the RPSBlast algorithm against a
 * protein sequence.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 5.16
 */
@Entity
@Table(name = "rpsblast_match")
@XmlType(name = "RPSBlastMatchType")
public class RPSBlastMatch extends Match<RPSBlastMatch.RPSBlastLocation> {

    protected RPSBlastMatch() {
    }

    public RPSBlastMatch(Signature signature, Set<RPSBlastLocation> locations) {
        super(signature, locations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RPSBlastMatch))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 59)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<RPSBlastLocation> clonedLocations = new HashSet<RPSBlastLocation>(this.getLocations().size());
        for (RPSBlastLocation location : this.getLocations()) {
            clonedLocations.add((RPSBlastLocation) location.clone());
        }
        return new RPSBlastMatch(this.getSignature(), clonedLocations);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Location of CDD match on a protein sequence
     *
     * @author Gift Nuka
     */
    @Entity
    @Table(name = "rpsblast_location")
    @XmlType(name = "RPSBlastLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    //@XmlType(name = "RPSBlastLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5", propOrder = { "start", "end", "score", "evalue"})
    public static class RPSBlastLocation extends Location<RPSBlastLocation.RPSBlastSite> {

        @Column(nullable = false, name = "evalue")
        private double evalue;

        @Column(nullable = false, name = "score")
        private double score;

        protected RPSBlastLocation() {
        }

        public RPSBlastLocation(int start, int end, double score, double evalue, Set<RPSBlastSite> sites) {
            super(start, end, sites);
            setScore(score);
            setEvalue(evalue);
        }

        @XmlAttribute(required = true)
        public double getEvalue() {
            return evalue;
        }

        private void setEvalue(double evalue) {
            this.evalue = evalue;
        }

        @XmlAttribute(required = true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof RPSBlastLocation))
                return false;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(41, 59)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            final Set<RPSBlastSite> clonedLocations = new HashSet<>(this.getSites().size());
            for (RPSBlastSite site : this.getSites()) {
                clonedLocations.add((RPSBlastSite) site.clone());
            }
            return new RPSBlastLocation(this.getStart(), this.getEnd(), this.getScore(), this.getEvalue(), clonedLocations);
        }

        @Entity
        @Table(name = "rpsblast_site")
        @XmlType(name = "RPSBlastSiteType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
        public static class RPSBlastSite extends Site {

            @Column(name = "description", nullable = false)
            private String description;


            protected RPSBlastSite() {
            }

            public RPSBlastSite(String description, Set<ResidueLocation> residueLocations) {
                super(residueLocations);
                setDescription(description);
            }

            @XmlAttribute(required = true)
            public String getDescription() {
                return description;
            }

            private void setDescription(String description) {
                this.description = description;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof RPSBlastSite))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(41, 59)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                final Set<ResidueLocation> clonedResidueLocations = new HashSet<>(this.getResidueLocations().size());
                for (ResidueLocation rl : this.getResidueLocations()) {
                    clonedResidueLocations.add((ResidueLocation) rl.clone());
                }
                return new RPSBlastSite(this.getDescription(), clonedResidueLocations);
            }

        }
    }

}
