package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public class ProSitePatternRawMatch extends RawMatch {

    // TODO: Should we use enum for level?
    private String level;

    protected ProSitePatternRawMatch() { }

    public ProSitePatternRawMatch(String sequenceIdentifier, String model,
                                  String signatureLibraryName, String signatureLibraryRelease,
                                  long locationStart, long locationEnd,
                                  String level, String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, generator);
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    private void setLevel(String level) {
        this.level = level;
    }
    
}
