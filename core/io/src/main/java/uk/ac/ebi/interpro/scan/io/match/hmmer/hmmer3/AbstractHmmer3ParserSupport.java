package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support class to parse HMMER3 output into {@link uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch}es.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
abstract class AbstractHmmer3ParserSupport<T extends Hmmer3RawMatch> implements Hmmer3ParserSupport<T> {

    private static final Pattern MODEL_ACCESSION_LINE_PATTERN
            = Pattern.compile("^[^:]*:\\s+(\\w+)\\s+\\[M=(\\d+)\\].*$");

    private String signatureLibraryRelease;

    private HmmKey hmmKey;

    @Required
    public void setHmmKey(HmmKey hmmKey) {
        this.hmmKey = hmmKey;
    }

    @Override
    public HmmKey getHmmKey() {
        return hmmKey;
    }

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

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Override
    public String getModelId(Matcher modelIdentLinePatternMatcher) {
        return modelIdentLinePatternMatcher.group(1);
    }

    @Override
    public Integer getModelLength(Matcher modelIdentLinePatternMatcher) {
        return Integer.parseInt(modelIdentLinePatternMatcher.group(2));
    }

    /**
     * Adds {@link uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch}es to {@code methodMatches}.
     *
     * @param hmmSearchRecord Data model of hmmsearch output.
     * @param rawResults      Map of protein accessions to {@link uk.ac.ebi.interpro.scan.model.raw.RawProtein}s.
     */
    public void addMatch(final HmmSearchRecord hmmSearchRecord,
                         final Map<String, RawProtein<T>> rawResults) {
        for (SequenceMatch sequenceMatch : hmmSearchRecord.getSequenceMatches().values()) {
            for (DomainMatch domainMatch : sequenceMatch.getDomainMatches()) {
                // Get existing protein or add new one
                String id = sequenceMatch.getSequenceIdentifier();
                RawProtein<T> protein = rawResults.get(id);
                if (protein == null) {
                    protein = new RawProtein<T>(id);
                    rawResults.put(id, protein);
                }
                // Add match
                final T match = createMatch(signatureLibraryRelease, hmmSearchRecord, sequenceMatch, domainMatch);
                protein.addMatch(match);
            }
        }
    }

    /**
     * Returns {@link uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch} instance using values from parameters.
     *
     * @param signatureLibraryRelease Corresponds to {@link uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease#getVersion()}
     * @param hmmSearchRecord         Single record in the hmmsearch output
     * @param sequenceMatch           Sequence match
     * @param domainMatch             Domain match
     * @return Hmmer3RawMatch instance using values from parameters.
     */
    protected abstract T createMatch(final String signatureLibraryRelease,
                                     final HmmSearchRecord hmmSearchRecord,
                                     final SequenceMatch sequenceMatch,
                                     final DomainMatch domainMatch);

}
