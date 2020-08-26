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
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class PrositePfsearchMatchParser extends AbstractLineMatchParser<PfScanRawMatch> {

    private static final Logger LOGGER = LogManager.getLogger(PrositePfsearchMatchParser.class.getName());

    private CigarAlignmentEncoder cigarEncoder;

    private static final String START_OF_MATCH = ">";


    private Pattern PrositeMatchPattern = Pattern.compile("/^>(\\S+)\\/(\\d+)\\-(\\d+)\\s+\\S+=(\\S+)\\|\\S+\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s+\\S+=(\\S+)\\s(.*)/");

    protected PrositePfsearchMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
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
        //>testseq1/7-307 motif=MF_00001|Asp_carb_tr norm_score=38.022 raw_score=6449 level=1 seq_end=-5 motif_start=1 motif_end=-1
        LOGGER.debug("parsing line: " + line);
        Utilities.verboseLog(40, "parsing line: " + line);

        //MF_01458|FtsH	1	-1	UPI00043D6473	658	1179	13110	32.021942	+	Q

        if (line.strip().isEmpty()){
            return null;
        }
        PrositeSequenceMatch sequenceMatch = new PrositeSequenceMatch(line);
        Utilities.verboseLog(40, "alighment is ... -> " + sequenceMatch.getAlignment());
        if (sequenceMatch != null) {
            Utilities.verboseLog(40, "1. We found match ..." + sequenceMatch.toString());
            return buildMatchObject(
                    sequenceMatch.getSequenceIdentifier(),
                    sequenceMatch.getModel(),
                    this.getSignatureLibraryRelease(),
                    sequenceMatch.getSequenceStart(),
                    sequenceMatch.getSequenceEnd(),
                    cigarEncoder.encode(sequenceMatch.getAlignment()),
                    sequenceMatch.getScore(),
                    ProfileScanRawMatch.Level.byLevelString(Integer.toString(sequenceMatch.getLevel())),
                    null
            );
        } else if (line.startsWith(START_OF_MATCH)) {
            Matcher sequenceMatchLineMatcher = PrositeSequenceMatch.SEQUENCE_LINE_PATTERN.matcher(line);
            if (sequenceMatchLineMatcher.matches()) {
                LOGGER.debug("We found match ...");

                sequenceMatch = new PrositeSequenceMatch(sequenceMatchLineMatcher);
                Utilities.verboseLog(40, "2. We found match ..." + sequenceMatch.toString());
                return buildMatchObject(
                        sequenceMatch.getSequenceIdentifier(),
                        sequenceMatch.getModel(),
                        this.getSignatureLibraryRelease(),
                        sequenceMatch.getSequenceStart(),
                        sequenceMatch.getSequenceEnd(),
                        cigarEncoder.encode(sequenceMatch.getAlignment()),
                        sequenceMatch.getScore(),
                        ProfileScanRawMatch.Level.byLevelString(Integer.toString(sequenceMatch.getLevel())),
                        null
                );
            }
        }
        return null;
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

}
