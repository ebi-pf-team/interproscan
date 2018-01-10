package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * Models a match based upon the Phobius algorithm against a
 * protein sequence.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
@Entity
@XmlType(name = "PhobiusMatchType")
public class PhobiusMatch extends Match<PhobiusMatch.PhobiusLocation> {

    protected PhobiusMatch() {
    }

    public PhobiusMatch(Signature signature, String signatureModels, Set<PhobiusLocation> locations) {
        super(signature, signatureModels, locations);
        // TODO - Add runtime check that the Signature being matched
        // has been constructed from the Phobius enum.
        if (!PhobiusFeatureType.isValidSignature(signature)) {
            throw new IllegalArgumentException("The Signature object being used for this PhobiusMatch does not appear to be a valid Phobius signature.");
        }
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<PhobiusLocation> clonedLocations = new HashSet<PhobiusLocation>(this.getLocations().size());
        for (PhobiusLocation location : this.getLocations()) {
            clonedLocations.add((PhobiusLocation) location.clone());
        }
        return new PhobiusMatch(this.getSignature(), this.getSignatureModels(), clonedLocations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhobiusMatch))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 49)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    /**
     * Location of Phobius match on a protein sequence
     *
     * @author Phil Jones
     * @author Gift Nuka
     */
    @Entity
    @Table(name = "phobius_location")
    @XmlType(name = "PhobiusLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class PhobiusLocation extends Location {

        protected PhobiusLocation() {
        }

        public PhobiusLocation(int start, int end) {
            super(start, end);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PhobiusLocation))
                return false;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(29, 49)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new PhobiusLocation(this.getStart(), this.getEnd());
        }
    }

}
