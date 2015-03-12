package uk.ac.ebi.interpro.scan.io.sequence;

/**
 * Represents a sequence record.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class SequenceRecord {

    private final String id;
    private final String description;
    private final String sequence;

    private SequenceRecord() {
        this.id          = null;
        this.description = null;
        this.sequence    = null;
    }

    public SequenceRecord(String id, String sequence) {
        this.id          = id;
        this.sequence    = sequence;
        this.description = null;
    }

    public SequenceRecord(String id, String description, String sequence) {
        this.id           = id;
        this.description  = description;
        this.sequence     = sequence;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSequence() {
        return sequence;
    }

}
