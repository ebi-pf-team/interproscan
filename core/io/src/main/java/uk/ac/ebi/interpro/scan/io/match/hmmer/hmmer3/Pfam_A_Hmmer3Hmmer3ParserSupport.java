package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.io.model.GaValuesRetriever;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For:  HMMER 3
 * Pfam-A
 * <p/>
 * This implementation of MatchConverter is responsible for taking the raw results parsed out
 * from the hmmsearch format and creating the correct type of RawProtein,
 * at the same time as correctly filtering the raw results using a
 * HMMER3GaValues object.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Pfam_A_Hmmer3Hmmer3ParserSupport implements Hmmer3ParserSupport<PfamHmmer3RawMatch> {

    private SignatureLibrary signatureLibrary;

    private String signatureLibraryRelease;

    private GaValuesRetriever gaValuesRetriever;

    @Required
    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setGaValuesRetriever(GaValuesRetriever gaValuesRetriever) {
        this.gaValuesRetriever = gaValuesRetriever;
    }

    /**
     * Based upon a match to the Pattern retrieved by the getSequenceIdentLinePattern method,
     * returns the ID / accession of the method.
     *
     * @param modelIdentLinePatternMatcher matcher to the Pattern retrieved by the getSequenceIdentLinePattern method
     * @return the ID or accession of the method.
     */
    @Override
    public String getModelId(Matcher modelIdentLinePatternMatcher) {
        return modelIdentLinePatternMatcher.group(1);
    }

    /**
     * Returns the model accession length, or null if this value is not available.
     *
     * @param modelIdentLinePatternMatcher matcher to the Pattern retrieved by the getSequenceIdentLinePattern method
     * @return the model accession length, or null if this value is not available.
     */
    @Override
    public Integer getModelLength(Matcher modelIdentLinePatternMatcher) {
        return null;
    }

    private static final Pattern MODEL_ACCESSION_LINE_PATTERN = Pattern.compile("^[^:]*:\\s+(\\w+).*$");

    /**
     * As the regular expressions required to parse the 'ID' or 'Accession' lines appear
     * to differ from one member database to another, factored out here.
     *
     * @return a Pattern object to parse the ID / accession line.
     */
    @Override
    public Pattern getModelIdentLinePattern() {
        return MODEL_ACCESSION_LINE_PATTERN;
    }

    public void addMatch(HmmSearchRecord methodMatches, Map<String, RawProtein<PfamHmmer3RawMatch>> rawResults) throws IOException {
        try {
            for (SequenceMatch sequenceMatch : methodMatches.getSequenceMatches().values()) {
                for (DomainMatch domainMatch : sequenceMatch.getDomainMatches()) {

                    // Find out if the sequence match / domain match pass the GA cutoff.
                    // hmmer3 handles cutoff correctly, so we will disable this check
//                    if ((sequenceMatch.getScore() >= gaValuesRetriever.getSequenceGAForAccession(methodMatches.getModelAccession()))
//                            &&
//                            (domainMatch.getScore() >= gaValuesRetriever.getDomainGAForAccession(methodMatches.getModelAccession()))) {

                        // Good sequence / domain match, so add to the rawResults.

                        // Either retrieve the correct RawSequenceIdentifer, or create a new one
                        // and add it to the Map.
                        RawProtein<PfamHmmer3RawMatch> rawProtein = rawResults.get(sequenceMatch.getSequenceIdentifier());
                        if (rawProtein == null) {
                            rawProtein = new RawProtein<PfamHmmer3RawMatch>(sequenceMatch.getSequenceIdentifier());
                            rawResults.put(sequenceMatch.getSequenceIdentifier(), rawProtein);
                        }

                        final PfamHmmer3RawMatch match = new PfamHmmer3RawMatch(
                                sequenceMatch.getSequenceIdentifier(),
                                methodMatches.getModelAccession(),
                                signatureLibrary,
                                signatureLibraryRelease,
                                domainMatch.getAliFrom(),
                                domainMatch.getAliTo(),
                                sequenceMatch.getEValue(),
                                sequenceMatch.getScore(),
                                domainMatch.getHmmfrom(),
                                domainMatch.getHmmto(),
                                domainMatch.getHmmBounds(),
                                domainMatch.getScore(),
                                domainMatch.getEnvFrom(),
                                domainMatch.getEnvTo(),
                                domainMatch.getAcc(),
                                sequenceMatch.getBias(),
                                domainMatch.getCEvalue(),
                                domainMatch.getIEvalue(),
                                domainMatch.getBias()
                        );
                        rawProtein.addMatch(match);
//                    } // End of testing if pass GA cutoff.
                } // End of looping over domain matches
            } // End of looping over sequence matches
        } catch (ParseException e) {
            throw new IllegalStateException("Unable to parse the GA values from the HMM library.", e);
        }
    }

    /**
     * Pfam-A parsing does not require the alignments.
     *
     * @return false to indicate that Pfam-A parsing does not require the alignments.
     */
    public boolean parseAlignments() {
        return false;
    }

    @Override
    public HmmKey getHmmKey() {
        return HmmKey.ACCESSION;

    }
}
