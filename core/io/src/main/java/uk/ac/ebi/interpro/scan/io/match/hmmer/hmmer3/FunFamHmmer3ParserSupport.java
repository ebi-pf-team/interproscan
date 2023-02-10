package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.raw.FunFamHmmer3RawMatch;


import java.util.regex.Pattern;

/**
 * Support class to parse HMMER3 output into {@link FunFamHmmer3RawMatch}es.
 *
 * @author Matthias Blum
 * @version $Id$
 */
public class FunFamHmmer3ParserSupport extends AbstractHmmer3ParserSupport<FunFamHmmer3RawMatch> {

    private static final Pattern MODEL_ACCESSION_LINE_PATTERN = Pattern.compile("^Query:\\s+(\\S+)+\\s+\\[M=(\\d+)\\]");

    /**
     * Returns Pattern object to parse the accession line.
     * As the regular expressions required to parse the 'ID' or 'Accession' lines appear
     * to differ from one member database to another, factored out here.
     *
     * @return Pattern object to parse the accession line.
     */
    @Override
    public Pattern getModelIdentLinePattern() {
        return MODEL_ACCESSION_LINE_PATTERN;
    }

    @Override
    protected FunFamHmmer3RawMatch createMatch(final String signatureLibraryRelease,
                                               final HmmSearchRecord hmmSearchRecord,
                                               final SequenceMatch sequenceMatch,
                                               final DomainMatch domainMatch) {
        return new FunFamHmmer3RawMatch(
                sequenceMatch.getSequenceIdentifier(),
                hmmSearchRecord.getModelAccession(),
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
                domainMatch.getAlignment(),
                0,
                0
        );
    }

    @Override
    public boolean parseAlignments() {
        return true;
    }
}
