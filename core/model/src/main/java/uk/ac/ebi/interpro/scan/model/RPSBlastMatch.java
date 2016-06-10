package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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

//    @Transient
//    @XmlJavaTypeAdapter(Site.SiteAdapter.class)
//    public Set<RPSBlastLocation.RPSBlastSite> getSites() {
//        return sites;
//    }
//
//    // Private so can only be set by JAXB, Hibernate ...etc via reflection


//    protected void setSites(Set<RPSBlastLocation.RPSBlastSite> sites) {
//        this.sites = sites;
//    }
//
//    protected void setLocations(final Set<T> locations) {
//        if (locations != null) {
//            for (T location : locations) {
//                location.setMatch(this);
//                this.locations.add(location);
//            }
//        }
//    }
//
//    @Transient
//    public void addSite(RPSBlastLocation.RPSBlastSite site) {
//        site...setMatch(this);
//        this.locations.add(location);
//    }


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
    public static class RPSBlastLocation extends Location {

        @Column(nullable = false, name = "evalue")
        private double evalue;

        @Column(nullable = false, name = "score")
        private double score;

//        Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> sites;

        protected RPSBlastLocation() {
        }

        public RPSBlastLocation(int start, int end, double score, double evalue) {
            super(start, end);
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

//        @Override
//        public int hashCode() {
//            int result = super.hashCode();
//            long temp;
//            temp = Double.doubleToLongBits(evalue);
//            result = 31 * result + (int) (temp ^ (temp >>> 32));
//            temp = Double.doubleToLongBits(score);
//            result = 31 * result + (int) (temp ^ (temp >>> 32));
//            return result;
//        }
        @Override
        public int hashCode() {
            return new HashCodeBuilder(41, 59)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new RPSBlastLocation(this.getStart(), this.getEnd(), this.getScore(), this.getEvalue());
        }

        @Entity
        @Table(name = "rpsblast_site")
        @XmlType(name = "RPSBlastSiteType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
        public static class RPSBlastSite extends Site {


            protected RPSBlastSite() {
            }

            public RPSBlastSite(String residue, int start, int end) {
                super(residue, start, end);
            }
        }
    }

}
