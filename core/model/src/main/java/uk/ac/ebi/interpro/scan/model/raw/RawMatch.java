package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.*;

/**
 * TODO: Add class description
 * TODO: Design for "abstract" (Bloch)
 * raw.* classes will be used in batch processing, so we may or may not use Hibernate
 * model.* will be mainly used in CRUD operations, so Hibernate is OK
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@IdClass(uk.ac.ebi.interpro.scan.model.raw.RawMatchKey.class)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@DiscriminatorColumn(name="METHOD_AC",discriminatorType=DiscriminatorType.STRING)
public abstract class RawMatch {

    //Primary key representation for RawMatch
    /*@ManyToOne
    @JoinColumns({
        @JoinColumn(name="sequenceIdentifier",
            referencedColumnName="UPI"),
        @JoinColumn(name="model",
            referencedColumnName="METHOD_AC"),
         @JoinColumn(name="dbversion",
            referencedColumnName="RELNO_MAJOR"),
            @JoinColumn(name="start",
            referencedColumnName="SEQ_START"),
            @JoinColumn(name="generator",
            referencedColumnName="ALGORITHM")
    }) */
    // TODO: Don't need any foreign keys -- just index fields we will search on
    //@Column(name="UPI",nullable = false)
    private String sequenceIdentifier;  // eg. MD5
    //@Id
    //@Column(name="METHOD_AC",nullable = false, unique = true, updatable = false, length = 50)
    private String model;   // eg. "PF00001"

    //@Column(name="MEMBER_DBNAME")
   // private String dbname; //for ex: PFAM, or GENE3D

    // TODO: Get dbversion from Spring Batch JobParameter?
    //@Column (name="DBVERSION",nullable = false, updatable = false, length = 10)
    private String dbversion;// eg. "23.0"

    //@Column (name="ALGORITHM")

    private String generator;  // eg. "HMMER 2.3.1"

    //@Column (name="SEQ_START")
    private long start;

   // @Column (name="SEQ_END")// location.start
    private long end;                   // location.end
    

    protected RawMatch() { }
    
    public RawMatch(String model) {
        setModel(model);
    }
    public RawMatch(String identifier, String model, String dbn, String dbver, String gen, long start, long end) {
        setSequenceIdentifier(identifier);
        setModel(model);
        //setDbname(dbn);
        setDbversion(dbver);
        setGenerator(gen);
        setStart(start);
        setEnd(end);
    }
    @Id
    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    public void setSequenceIdentifier(String sequenceIdentifier) {
        this.sequenceIdentifier = sequenceIdentifier;
    }
    @Id
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
     @Id
    public String getDbversion() {
        return dbversion;
    }

    public void setDbversion(String dbversion) {
        this.dbversion = dbversion;
    }
     @Id
    public String getGenerator() {
        return generator;
    }

    /* public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }  */
    
    public void setGenerator(String generator) {
        this.generator = generator;
    }
    @Id
    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
