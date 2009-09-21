package uk.ac.ebi.interpro.scan.model.raw;

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
public abstract class RawMatch {

    // TODO: Don't need any foreign keys -- just index fields we will search on

    private String sequenceIdentifier;  // eg. MD5
    private String model;               // eg. "PF00001"
    // TODO: Get dbversion from Spring Batch JobParameter?
    private String dbversion;           // eg. "23.0"
    private String generator;           // eg. "HMMER 2.3.1"
    private long start;                 // location.start
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
