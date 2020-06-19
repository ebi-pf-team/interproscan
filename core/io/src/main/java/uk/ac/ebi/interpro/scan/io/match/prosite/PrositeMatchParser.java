package uk.ac.ebi.interpro.scan.io.match.prosite;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.AbstractLineMatchParser;
import uk.ac.ebi.interpro.scan.model.PatternScanMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PfScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.alignment.CigarAlignmentEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses the GFF output format from ps_scan.pl
 * <p/>
 * the GFF format (version 2) is specified here: http://www.sanger.ac.uk/resources/software/gff/spec.html
 * <p/>
 * Column description (general GFF) summarised from the page above.:
 * <p/>
 * seqname: The id of the protein sequence.
 * source: The source of this feature: in this case the software / version number.
 * feature: The HAMAP or Prosite accession number
 * start: on the protein sequence
 * end: on the protein sequence
 * score: A floating point value. When there is no score
 * strand: NOT USED hence '.'
 * frame: NOT USED hence '.'
 * attributes: key value structure with semicolon separators. keys must be standard identifiers ([A-Za-z][A-Za-z0-9_]*).
 * Free text values must be quoted with double quotes.
 * Note: Non-printing chars ANSI C representation (eg newlines as '\n', tabs as '\t').
 * Multiple values can follow a specific key.
 * <p/>
 * For specific member databases, attributes are:
 * <p/>
 * HAMAP:             Name, Level, RawScore, FeatureFrom, FeatureTo, Sequence, SequenceDescription
 * Prosite Profiles:  Name, Level, RawScore, FeatureFrom, FeatureTo, Sequence, SequenceDescription, KnownFalsePos
 * Prosite Patterns:  Name, LevelTag, Sequence, SequenceDescription, KnownFalsePos
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class PrositeMatchParser extends AbstractLineMatchParser<PfScanRawMatch> {

    private static final Logger LOGGER = LogManager.getLogger(PrositeMatchParser.class.getName());

    private CigarAlignmentEncoder cigarEncoder;

    protected PrositeMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        super(signatureLibrary, signatureLibraryRelease);
    }


    @Required
    public void setCigarEncoder(CigarAlignmentEncoder cigarEncoder) {
        this.cigarEncoder = cigarEncoder;
    }

    /**
     * Returns {@link uk.ac.ebi.interpro.scan.model.raw.RawMatch} instance using values from parameters.
     *
     * @param line Line read from input file.
     * @return {@link uk.ac.ebi.interpro.scan.model.raw.RawMatch} instance using values from parameters
     */
    @Override
    protected PfScanRawMatch createMatch(String line) {
        final String[] splitLine = line.split("\\t");
        if (splitLine.length < 9) return null;

        Double score = null;
        final String scoreString = splitLine[5].trim();
        if (scoreString.length() > 0 && !".".equals(scoreString)) {
            score = Double.parseDouble(scoreString);
        }
        final Map<String, String> gffAttributes = extractAttributes(splitLine[8]);

        return buildMatchObject(
                splitLine[0].trim(),
                splitLine[2].trim(),
                this.getSignatureLibraryRelease(),
                Integer.parseInt(splitLine[3].trim()),
                Integer.parseInt(splitLine[4].trim()),
                cigarEncoder.encode(gffAttributes.get("Sequence")),
                score,
                ProfileScanRawMatch.Level.byLevelString(gffAttributes.get("Level")),
                PatternScanMatch.PatternScanLocation.Level.getLevelByTag(gffAttributes.get("LevelTag"))
        );
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
    protected abstract PfScanRawMatch buildMatchObject(String sequenceIdentifier,
                                                       String model,
                                                       String signatureLibraryRelease,
                                                       int seqStart,
                                                       int seqEnd,
                                                       String cigarAlign,
                                                       Double score,
                                                       ProfileScanRawMatch.Level profileLevel,
                                                       PatternScanMatch.PatternScanLocation.Level patternLevel);

    /**
     * Helper method to grab the attributes from the end of the gff line.
     * Pretty horrible format, hence pretty horrible code!
     *
     * @param attributes semi-colon separated attributes to be parsed into key-value pairs.
     * @return a Map of key-value pairs extracted from the attributes String.
     */
    private Map<String, String> extractAttributes(String attributes) {
        Map<String, String> attributeMap = new HashMap<String, String>();
        String[] parts = attributes.split(";");
        for (String part : parts) {
            if (part != null) {
                final String trimmedPart = part.trim();
                final int firstSpaceIndex = trimmedPart.indexOf(' ');
                String key = trimmedPart.substring(0, firstSpaceIndex);
                String value = trimmedPart.substring(firstSpaceIndex + 1).trim();
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                }
                if (value.endsWith("\"")) {
                    value = value.substring(0, value.length() - 1);
                }
                if (key != null && value != null) {
                    attributeMap.put(key, value);
                }
            }
        }

        return attributeMap;
    }
}
