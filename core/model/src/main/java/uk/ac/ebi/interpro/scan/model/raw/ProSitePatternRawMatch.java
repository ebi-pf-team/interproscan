package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.PatternScanMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * <a href="http://www.expasy.ch/prosite/">PROSITE</a> Pattern raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@Table(name = "prosite_pattern_raw_match")
public class ProSitePatternRawMatch extends PfScanRawMatch {

    protected ProSitePatternRawMatch() {
    }

    @Enumerated(javax.persistence.EnumType.STRING)
    @Column(nullable = false)
    private PatternScanMatch.PatternScanLocation.Level patternLevel;

    public ProSitePatternRawMatch(String sequenceIdentifier, String model,
                                  String signatureLibraryRelease,
                                  int locationStart, int locationEnd, String cigarAlignment, PatternScanMatch.PatternScanLocation.Level patternLevel) {
        super(sequenceIdentifier, model, SignatureLibrary.PROSITE_PATTERNS, signatureLibraryRelease, locationStart, locationEnd, cigarAlignment);
        setPatternLevel(patternLevel);
    }

    public void setPatternLevel(PatternScanMatch.PatternScanLocation.Level patternLevel) {
        this.patternLevel = patternLevel;
    }

    public PatternScanMatch.PatternScanLocation.Level getPatternLevel() {
        return patternLevel;
    }
}
