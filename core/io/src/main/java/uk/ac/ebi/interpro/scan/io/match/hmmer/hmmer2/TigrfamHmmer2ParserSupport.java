package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel.Hmmer2HmmPfamDomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel.Hmmer2HmmPfamSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel.Hmmer2HmmPfamSequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TigrFamHmmer2RawMatch;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser support class for TIGRFam.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TigrfamHmmer2ParserSupport implements Hmmer2ParserSupport<TigrFamHmmer2RawMatch> {

    private String signatureLibraryRelease;

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    /**
     * Implemented for specific member databases.  Different databases use different specific model classes
     * and may need to filter the matches at this point, based upon different criteria.
     * <p/>
     * This responsibility is delegated to the sub-class.
     *
     * @param methodMatches which contains a localised (parsing-only) data model of the information in
     *                      HMMER 3 hmmsearch output.
     * @param rawResults    being the Map of protein accessions to RawProteins
     *                      that the raw results should be added to.
     * @throws java.io.IOException in the event of an IO problem.
     */
    @Override
    public void addMatch(Hmmer2HmmPfamSearchRecord methodMatches, Map<String, RawProtein<TigrFamHmmer2RawMatch>> rawResults) throws IOException {
        for (String sequenceId : methodMatches.getSequenceMatches().keySet()) {
            Hmmer2HmmPfamSequenceMatch sequenceMatch = methodMatches.getSequenceMatches().get(sequenceId);
            for (Hmmer2HmmPfamDomainMatch domainMatch : sequenceMatch.getDomainMatches()) {
                final RawProtein<TigrFamHmmer2RawMatch> rawProtein;
                if (rawResults.keySet().contains(methodMatches.getSequenceId())) {
                    rawProtein = rawResults.get(methodMatches.getSequenceId());
                } else {
                    rawProtein = new RawProtein<TigrFamHmmer2RawMatch>(methodMatches.getSequenceId());
                    rawResults.put(methodMatches.getSequenceId(), rawProtein);
                }
                rawProtein.addMatch(
                        new TigrFamHmmer2RawMatch(
                                methodMatches.getSequenceId(),
                                sequenceMatch.getModelAccession(),
                                SignatureLibrary.TIGRFAM,
                                signatureLibraryRelease,
                                domainMatch.getSeqFrom(),
                                domainMatch.getSeqTo(),
                                sequenceMatch.getEValue(),
                                sequenceMatch.getSequenceScore(),
                                domainMatch.getHmmFrom(),
                                domainMatch.getHmmTo(),
                                domainMatch.getHmmBounds(),
                                domainMatch.getEValue(),
                                domainMatch.getScore()
                        )
                );
            }
        }
    }

    /**
     * For maximum efficiency, specific implementations can choose to ignore the
     * alignment section, in which case this method should return false.
     * <p/>
     * Implementations for member databases such as Gene3D should return true from this method.
     *
     * @return boolean indicating if the alignment section should be parsed.
     */
    @Override
    public boolean parseAlignments() {
        return false;
    }

    /**
     * Returns the model ID or model accession.
     *
     * @param modelIdentLinePatternMatcher Matcher to the Pattern retrieved by the getSequenceIdentLinePattern method
     * @return the ID or accession of the method.
     */
    @Override
    public String getSequenceId(Matcher modelIdentLinePatternMatcher) {
        return modelIdentLinePatternMatcher.group(1);
    }

    /**
     * Returns the model length, or null if this value is not available.
     *
     * @param modelIdentLinePatternMatcher matcher to the Pattern retrieved by the getSequenceIdentLinePattern method
     * @return the model accession length, or null if this value is not available.
     */
    @Override
    public Integer getModelLength(Matcher modelIdentLinePatternMatcher) {
        return null;
    }

    @Override
    public HmmKey getHmmKey() {
        return HmmKey.QUERY;
    }

    private static final Pattern MODEL_IDENT_LINE_PATTERN = Pattern.compile("^Query\\ssequence:\\s+(.+)\\s*$");

    /**
     * As the regular expressions required to parse the 'ID' or 'Accession' lines appear
     * to differ from one member database to another, factored out here.
     *
     * @return a Pattern object to parse the ID / accession line.
     */
    @Override
    public Pattern getSequenceIdentLinePattern() {
        return MODEL_IDENT_LINE_PATTERN;
    }
}
