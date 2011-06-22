package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;
import java.util.Set;

/**
 * Models a match based upon the ProDom algorithm against a
 * protein sequence.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0
 */
@Entity
@XmlType(name = "ProDomMatchType")
public class ProDomMatch extends Match<ProDomMatch.ProDomLocation> {

    protected ProDomMatch() {
    }

    public ProDomMatch(Signature signature, Set<ProDomLocation> locations) {
        super(signature, locations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProDomMatch))
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
     * Location of ProDom match on a protein sequence
     *
     * @author Matthew Fraser
     */
    @Entity
    @Table(name = "prodom_location")
    @XmlType(name = "ProDomLocationType")
    public static class ProDomLocation extends Location {

        protected ProDomLocation() {
        }

        public ProDomLocation(int start, int end) {

            super(start, end);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ProDomLocation))
                return false;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(29, 53)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }
    }

}
