package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.alignment.AlignmentEncoder;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.regex.Pattern;

/**
 * Support class to parse HMMER3 output into {@link Gene3dHmmer3RawMatch}es.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public class Gene3DHmmer3ParserSupport extends AbstractHmmer3ParserSupport<Gene3dHmmer3RawMatch> {

//    default reg pattern
//    private static final Pattern MODEL_ACCESSION_LINE_PATTERN_DEFAULT
//            = Pattern.compile("^[^:]*:\\s+(\\w+)\\s+\\[M=(\\d+)\\].*$");

    //Uses Query
    private static final Pattern MODEL_ACCESSION_LINE_PATTERN
            = Pattern.compile("^Query:\\s+(\\S+)+\\s+\\[M=(\\d+)\\]|^Query:\\s+cath\\|\\w+\\|([^\\/]+)\\S+\\s+\\[M=(\\d+)\\].*$");

    private AlignmentEncoder alignmentEncoder;

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
    public void setAlignmentEncoder(AlignmentEncoder alignmentEncoder) {
        this.alignmentEncoder = alignmentEncoder;
    }

    @Override
    protected Gene3dHmmer3RawMatch createMatch(final String signatureLibraryRelease,
                                               final HmmSearchRecord hmmSearchRecord,
                                               final SequenceMatch sequenceMatch,
                                               final DomainMatch domainMatch) {
        // TODO: Store model length? (required by DF3) -- see HmmSearchRecord.getModelLength()
        if (domainMatch.getAlignment() == null || domainMatch.getAlignment().trim().length() == 0) {
            throw new IllegalStateException("Attempting to create a Gene3D match that has no alignment data.");
        }
        Utilities.verboseLog("match: " +
                sequenceMatch.getSequenceIdentifier() + " " +
                hmmSearchRecord.getModelAccession()+ " " +
                signatureLibraryRelease + " " +
                sequenceMatch.getEValue() + " " +
                sequenceMatch.getScore() + " " +
                sequenceMatch.getBias() + " " +
                domainMatch.getHmmfrom() + " " +
                domainMatch.getHmmto() + " " +
                domainMatch.getHmmBounds() + " " +
                domainMatch.getScore() + " " +
                domainMatch.getEnvFrom() + " " +
                domainMatch.getEnvTo() + " " +
                domainMatch.getAliFrom() + " " +
                domainMatch.getAliTo() + " " +
                domainMatch.getAcc() + " " +
                domainMatch.getCEvalue() + " " +
                domainMatch.getIEvalue() + " " +
                domainMatch.getBias()
        );
        return new Gene3dHmmer3RawMatch(
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
                alignmentEncoder.encode(domainMatch.getAlignment())
        );
    }

    /**
     * Returns true to indicate Gene3D requires alignments be parsed from the HMMER output.
     *
     * @return true to indicate Gene3D requires alignments be parsed from the HMMER output.
     */
    @Override
    public boolean parseAlignments() {
        return true;
    }


}
