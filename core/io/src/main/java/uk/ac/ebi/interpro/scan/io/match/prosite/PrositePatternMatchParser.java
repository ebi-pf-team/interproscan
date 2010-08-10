package uk.ac.ebi.interpro.scan.io.match.prosite;

import uk.ac.ebi.interpro.scan.model.PatternScanMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PfScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.ProSitePatternRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;

/**
 * Concrete parser for Prosite patterns.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PrositePatternMatchParser extends PrositeMatchParser {

    public PrositePatternMatchParser(String signatureLibraryRelease) {
        super(SignatureLibrary.PROSITE_PATTERNS, signatureLibraryRelease);
    }

    /**
     * Method to be implemented that builds the correct kind of PfScanRawMatch.
     *
     * @param sequenceIdentifier      protein sequence identifier
     * @param model                   the accession / ID of the model
     * @param signatureLibraryRelease the current release number
     * @param seqStart                sequence match start coordinate
     * @param seqEnd                  sequence match stop coordinate
     * @param cigarAlign              cigar alignment String
     * @param score                   the score for the match
     * @param profileLevel            optional level for a Profile match
     * @param patternLevel            optional level for a Pattern match
     * @return an implementation of a PfScanRawMatch object.
     */
    @Override
    protected PfScanRawMatch buildMatchObject(String sequenceIdentifier, String model, String signatureLibraryRelease, int seqStart, int seqEnd, String cigarAlign, Double score, ProfileScanRawMatch.Level profileLevel, PatternScanMatch.PatternScanLocation.Level patternLevel) {
        return new ProSitePatternRawMatch(
                sequenceIdentifier,
                model,
                signatureLibraryRelease,
                seqStart,
                seqEnd,
                cigarAlign,
                patternLevel
        );
    }
}
