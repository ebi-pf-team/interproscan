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
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="MODEL")
public abstract class RawMatch {

    // TODO: Don't need any foreign keys -- just index fields we will search on
    @Column(name="UPI")
    private String sequenceIdentifier;  // eg. MD5

    @Column(name="MODEL")
    private String model;   // eg. "PF00001"



    @Column(name="MEMBER_DBNAME")
    private String dbname; //for ex: PFAM, or GENE3D

    // TODO: Get dbversion from Spring Batch JobParameter?
    @Column (name="DBVERSION")
    private String dbversion;// eg. "23.0"

    @Column (name="ALGORITHM")
    private String generator;  // eg. "HMMER 2.3.1"

    @Column (name="SEQ_START")
    private long start;

    @Column (name="SEQ_END")// location.start
    private long end;                   // location.end
    

    protected RawMatch() { }

    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    public void setSequenceIdentifier(String sequenceIdentifier) {
        this.sequenceIdentifier = sequenceIdentifier;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDbversion() {
        return dbversion;
    }

    public void setDbversion(String dbversion) {
        this.dbversion = dbversion;
    }

    public String getGenerator() {
        return generator;
    }

     public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }
    
    public void setGenerator(String generator) {
        this.generator = generator;
    }

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
