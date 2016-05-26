package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * Models a match based upon the Coils algorithm against a
 * protein sequence.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
@Entity
@XmlType(name = "CoilsMatchType")
public class CoilsMatch extends Match<CoilsMatch.CoilsLocation> {

    protected CoilsMatch() {
    }

    public CoilsMatch(Signature signature, Set<CoilsLocation> locations) {
        super(signature, locations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CoilsMatch))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(27, 47)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<CoilsLocation> clonedLocations = new HashSet<CoilsLocation>(this.getLocations().size());
        for (CoilsLocation location : this.getLocations()) {
            clonedLocations.add((CoilsLocation) location.clone());
        }
        return new CoilsMatch(this.getSignature(), clonedLocations);
    }

    /**
     * Location of Coils match on a protein sequence
     *
     * @author Phil Jones
     */
    @Entity
    @Table(name = "coils_location")
    @XmlType(name = "CoilsLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class CoilsLocation extends Location {

        protected CoilsLocation() {
        }

        public CoilsLocation(int start, int end) {

            super(start, end);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CoilsLocation))
                return false;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(27, 47)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new CoilsLocation(this.getStart(), this.getEnd());
        }
    }

}
