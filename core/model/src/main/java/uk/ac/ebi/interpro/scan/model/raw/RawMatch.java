package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;

/**
 * TODO: Add class description
 * TODO: Design for "abstract" (Bloch)
 * raw.* classes will be used in batch processing, so we may or may not use Hibernate
 * model.* will be mainly used in CRUD operations, so Hibernate is OK
 *
 * @author  Antony Quinn
 * @version $Id$
 */
//@IdClass(uk.ac.ebi.interpro.scan.model.raw.RawMatchKey.class)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@DiscriminatorColumn(name="METHOD_AC",discriminatorType=DiscriminatorType.STRING)
public abstract class RawMatch implements Serializable {

    //Primary key representation for RawMatch
    /*@ManyToOne
    @JoinColumns({
        @JoinColumn(name="sequenceIdentifier",
            referencedColumnName="UPI"),
        @JoinColumn(name="model",
            referencedColumnName="METHOD_AC"),
         @JoinColumn(name="signatureLibraryRelease",
            referencedColumnName="RELNO_MAJOR"),
            @JoinColumn(name="locationStart",
            referencedColumnName="SEQ_START"),
            @JoinColumn(name="generator",
            referencedColumnName="ALGORITHM")
    }) */
    
    // TODO: Don't need any foreign keys -- just index fields we will search on
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)    //TODO - Removed as causing problem with 'one table per class'.
    private Long id;
    private String sequenceIdentifier;  // eg. MD5
    
    //@Column(name="METHOD_AC",nullable = false, unique = true, updatable = false, length = 50)
    private String model;   // eg. "PF00001"

   //@Column(name="MEMBER_DBNAME")
   private String signatureLibraryName; //for ex: PFAM, or GENE3D

    // TODO: Get signatureLibraryRelease from Spring Batch JobParameter?
    //@Column (name="DBVERSION",nullable = false, updatable = false, length = 10)
    private String signatureLibraryRelease;// eg. "23.0"

    //@Column (name="ALGORITHM")
    private String generator;  // eg. "HMMER 2.3.1"

    //@Column (name="SEQ_START")
    private long locationStart;

   // @Column (name="SEQ_END")
    private long locationEnd;

    protected RawMatch() { }

    protected RawMatch(String sequenceIdentifier, String model,
                       String signatureLibraryName, String signatureLibraryRelease,
                       long locationStart, long locationEnd, String generator) {
        this.sequenceIdentifier     = sequenceIdentifier;
        this.model                  = model;
        this.signatureLibraryName   = signatureLibraryName;
        this.signatureLibraryRelease = signatureLibraryRelease;
        this.locationStart          = locationStart;
        this.locationEnd            = locationEnd;
        this.generator              = generator;
    }

    public Long getId() {
        return id;
    }

    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    private void setSequenceIdentifier(String sequenceIdentifier) {
        this.sequenceIdentifier = sequenceIdentifier;
    }

    public String getModel() {
        return model;
    }

    private void setModel(String model) {
        this.model = model;
    }
     public String getSignatureLibraryName() {
        return signatureLibraryName;
    }

    private void setSignatureLibraryName(String signatureLibraryName) {
        this.signatureLibraryName = signatureLibraryName;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    private void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public String getGenerator() {
        return generator;
    }

    private void setGenerator(String generator) {
        this.generator = generator;
    }

    public long getLocationStart() {
        return locationStart;
    }

    private void setLocationStart(long locationStart) {
        this.locationStart = locationStart;
    }

    public long getLocationEnd() {
        return locationEnd;
    }

    private void setLocationEnd(long locationEnd) {
        this.locationEnd = locationEnd;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawMatch))
            return false;
        final RawMatch m = (RawMatch) o;
        return new EqualsBuilder()
                .append(sequenceIdentifier, m.sequenceIdentifier)
                .append(signatureLibraryName, m.signatureLibraryName)
                .append(signatureLibraryRelease, m.signatureLibraryRelease)
                .append(generator, m.generator)
                .append(model, m.model)
                .append(locationStart, m.locationStart)
                .append(locationEnd, m.locationEnd)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(21, 51)
                .append(sequenceIdentifier)
                .append(signatureLibraryName)
                .append(signatureLibraryRelease)
                .append(generator)
                .append(model)
                .append(locationStart)
                .append(locationEnd)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
