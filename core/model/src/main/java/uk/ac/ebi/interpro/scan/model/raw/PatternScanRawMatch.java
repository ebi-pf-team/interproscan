package uk.ac.ebi.interpro.scan.model.raw;

/**
 * TODO: Add class description
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public final class PatternScanRawMatch extends RawMatch {

    // TODO: Should we use enum for level?  No - easier in Spring Batch not to
    private String level; // location.level

    public PatternScanRawMatch() { }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
}
