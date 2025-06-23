package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PIRSFHmmer3ParserSupport extends AbstractHmmer3ParserSupport<PirsfHmmer3RawMatch> {
    private static final Pattern MODEL_ACCESSION_LINE_PATTERN
            = Pattern.compile("^Accession:\\s+(PIRSF\\d{6})\\s*$");

    @Override
    public Pattern getModelIdentLinePattern() {
        return MODEL_ACCESSION_LINE_PATTERN;
    }

    @Override
    public Integer getModelLength(Matcher modelIdentLinePatternMatcher) {
        return null;
    }

    @Override
    protected PirsfHmmer3RawMatch createMatch(final String signatureLibraryRelease,
                                              final HmmSearchRecord hmmSearchRecord,
                                              final SequenceMatch sequenceMatch,
                                              final DomainMatch domainMatch) {
        return new PirsfHmmer3RawMatch(
                sequenceMatch.getSequenceIdentifier(),
                hmmSearchRecord.getModelAccession(),
                SignatureLibrary.PIRSF,
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
                domainMatch.getBias(),
                domainMatch.isSignificant()
        );
    }

    @Override
    public boolean parseAlignments() {
        return false;
    }
}
