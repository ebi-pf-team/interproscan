package uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3;

import uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3.parsemodel.HmmsearchOutputMethod;
import uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.parser.modelFileParser.GaValuesRetriever;
import uk.ac.ebi.interpro.scan.parser.ParseException;
import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.util.Map;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Required;

/**
 * For:  HMMER 3
 *       Pfam-A
 *
 * This implementation of MatchConverter is responsible for taking the raw results parsed out
 * from the hmmsearch format and creating the correct type of RawSequenceIdentifier,
 * at the same time as correctly filtering the raw results using a
 * HMMER3GaValues object.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Pfam_A_Hmmer3Hmmer3ParserSupport implements Hmmer3ParserSupport {

    private String signatureLibraryName;

    private String signatureLibraryRelease;

    private GaValuesRetriever gaValuesRetriever;

    @Required
    public void setSignatureLibraryName(String signatureLibraryName) {
        this.signatureLibraryName = signatureLibraryName;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setGaValuesRetriever(GaValuesRetriever gaValuesRetriever) {
        this.gaValuesRetriever = gaValuesRetriever;
    }

    public void addMatch(HmmsearchOutputMethod methodMatches, Map<String, RawSequenceIdentifier> rawResults) throws IOException {
        try{
            for (SequenceMatch sequenceMatch : methodMatches.getSequenceMatches().values()){
                for (DomainMatch domainMatch : sequenceMatch.getDomainMatches()){

                    // Find out if the sequence match / domain match pass the GA cutoff.
                    if ((sequenceMatch.getScore() >=  gaValuesRetriever.getSequenceGAForAccession(methodMatches.getMethodAccession()))
                            &&
                            (domainMatch.getScore() >= gaValuesRetriever.getDomainGAForAccession(methodMatches.getMethodAccession()))){

                        // Good sequence / domain match, so add to the rawResults.

                        // Either retrieve the correct RawSequenceIdentifer, or create a new one
                        // and add it to the Map.
                        RawSequenceIdentifier sequenceIdentifier = rawResults.get(sequenceMatch.getUpi());
                        if (sequenceIdentifier == null){
                            sequenceIdentifier = new RawSequenceIdentifier(sequenceMatch.getUpi());
                            rawResults.put(sequenceMatch.getUpi(), sequenceIdentifier);
                        }

                        final PfamHmmer3RawMatch match = new PfamHmmer3RawMatch(
                                sequenceMatch.getUpi(),
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
                                null
                        );
                        sequenceIdentifier.addMatch(match);
                    } // End of testing if pass GA cutoff.
                } // End of looping over domain matches
            } // End of looping over sequence matches
        } catch (ParseException e) {
            throw new IllegalStateException ("Unable to parse the GA values from the HMM library.", e);
        }
    }

    /**
     * Pfam-A parsing does not require the alignments.
     * @return false to indicate that Pfam-A parsing does not require the alignments.
     */
    public boolean parseAlignments() {
        return false;
    }
}
