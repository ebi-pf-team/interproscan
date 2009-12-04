package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Column;

/**
 * TODO: Add class description
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public class ProSitePatternRawMatch extends RawMatch {

    // TODO: Should we use enum for level?
    @Column (name="pro_site_level")
    private String level;

    protected ProSitePatternRawMatch() { }

    public ProSitePatternRawMatch(String sequenceIdentifier, String model,
                                  String signatureLibraryName, String signatureLibraryRelease,
                                  int locationStart, int locationEnd,
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
