package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.alignment.AlignmentEncoder;

/**
 * Support class to parse HMMER3 output into {@link Gene3dHmmer3RawMatch}es.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public class Gene3DHmmer3ParserSupport extends AbstractHmmer3ParserSupport<Gene3dHmmer3RawMatch> {

    private AlignmentEncoder alignmentEncoder;

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
