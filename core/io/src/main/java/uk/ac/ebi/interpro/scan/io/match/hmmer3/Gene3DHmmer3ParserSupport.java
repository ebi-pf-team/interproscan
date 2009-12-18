package uk.ac.ebi.interpro.scan.io.match.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.HmmsearchOutputMethod;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.alignment.AlignmentEncoder;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support class to parse HMMER3 output into {@link Gene3dHmmer3RawMatch}es.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class Gene3DHmmer3ParserSupport implements Hmmer3ParserSupport<Gene3dHmmer3RawMatch> {

    @Override
    public HmmKey getHmmKey() {
        return HmmKey.NAME;  //Later on inject value for this through spring, for flexibility
    }


    private static final Pattern MODEL_ACCESSION_LINE_PATTERN = Pattern.compile ("^[^:]*:\\s+(\\w+)\\s+\\[M=(\\d+)\\].*$" );
    
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

    // TODO: Signature info is common to all RawMatch implementations so use composition or package-private abstract class to reduce code?
    private String signatureLibraryName;
    private String signatureLibraryRelease;

    private AlignmentEncoder alignmentEncoder;



    @Required
    public void setSignatureLibraryName(String signatureLibraryName) {
        this.signatureLibraryName = signatureLibraryName;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setAlignmentEncoder(AlignmentEncoder alignmentEncoder) {
        this.alignmentEncoder = alignmentEncoder;
    }

    /**
     * Based upon a match to the Pattern retrieved by the getModelIdentLinePattern method,
     * returns the ID / accession of the method.
     *
     * @param modelIdentLinePatternMatcher matcher to the Pattern retrieved by the getModelIdentLinePattern method
     * @return the ID or accession of the method.
     */
    @Override
    public String getMethodIdentification(Matcher modelIdentLinePatternMatcher) {
        return modelIdentLinePatternMatcher.group(1);
    }

    /**
     * Returns the model accession length, or null if this value is not available.
     *
     * @param modelIdentLinePatternMatcher matcher to the Pattern retrieved by the getModelIdentLinePattern method
     * @return the model accession length, or null if this value is not available.
     */
    @Override
    public Integer getMethodAccessionLength(Matcher modelIdentLinePatternMatcher) {
        return Integer.parseInt(modelIdentLinePatternMatcher.group(2));
    }

    /**
     * Adds {@link Gene3dHmmer3RawMatch}es to {@code methodMatches}.
     *
     * @param methodMatches Data model of hmmsearch output.
     * @param rawResults    Map of protein accessions to RawProteins.
     */
    public void addMatch(HmmsearchOutputMethod methodMatches, Map<String, RawProtein<Gene3dHmmer3RawMatch>> rawResults) {
        for (SequenceMatch sequenceMatch : methodMatches.getSequenceMatches().values()){
            for (DomainMatch domainMatch : sequenceMatch.getDomainMatches()){
                // Get existing protein or add new one
                String id = sequenceMatch.getSequenceIdentifier();
                RawProtein<Gene3dHmmer3RawMatch> protein = rawResults.get(id);
                if (protein == null){
                    protein = new RawProtein<Gene3dHmmer3RawMatch>(id);
                    rawResults.put(id, protein);
                }
                // Get encoded alignment
                String encodedAlignment = alignmentEncoder.encode(domainMatch.getAlignment());
                // Create raw match
                final Gene3dHmmer3RawMatch match = new Gene3dHmmer3RawMatch(
                        sequenceMatch.getSequenceIdentifier(),
                        methodMatches.getMethodAccession(),
                        signatureLibraryName,
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
                        encodedAlignment,
                        null
                );
                protein.addMatch(match);
            }
        }
    }

    /**
     * Returns true to indicate Gene3D requires alignments be parsed from the HMMER output.
     *
     * @return true to indicate Gene3D requires alignments be parsed from the HMMER output.
     */
    public boolean parseAlignments() {
        return true;
    }

}