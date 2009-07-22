package uk.ac.ebi.interpro.scan.batch.item;

import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Location;

/**
 * Removes overlapping {@link Location)s from {@link Match}es.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class OverlapItemProcessor implements ItemProcessor<Protein, Protein> {

    public Protein process(Protein item) throws Exception {

        // TODO: Translate Onion post-processing code into i5
        // TODO: Good explanation in http://www.ebi.ac.uk/seqdb/confluence/display/InterPro/Gene3D+overlap+removal

//        boolean overlaps = false;
//        Iterator<IPRScanMatch> iter = seqHitsSoFar.iterator();
//        while (iter.hasNext() && !overlaps) {
//            IPRScanMatch bestHitSoFarForThisSeqRange = iter.next();
//            // First find out which hits starts earlier
//            int eS, eE, lS, lE; // earlier hit start and end/ later hit start and end
//            int bestHitSoFarLen, matchLen; // hit length for previously stored and the current hit
//            if (match.seqStart <= bestHitSoFarForThisSeqRange.seqStart) {
//                eS = match.seqStart;
//                eE = match.seqEnd;
//                lS = bestHitSoFarForThisSeqRange.seqStart;
//                lE = bestHitSoFarForThisSeqRange.seqEnd;
//                bestHitSoFarLen = lE - lS + 1;
//                matchLen = eE - eS + 1;
//            }
//            else {
//                eS = bestHitSoFarForThisSeqRange.seqStart;
//                eE = bestHitSoFarForThisSeqRange.seqEnd;
//                lS = match.seqStart;
//                lE = match.seqEnd;
//                bestHitSoFarLen = eE - eS + 1;
//                matchLen = lE - lS + 1;
//            }
//
//            /**  Three cases:
//             *  eS_________________eE
//             *          lS_________________lE
//             *
//             *       eS________________________eE
//             *          lS_________________lE
//             *
//             *   eS_________eE
//             *                 lS_________________lE
//             * In all cases, overlapLength = min(eE, lE) - lS. Note that in the last case, overlapLength < 0.
//             */
//
//            // Note that if one hit is inside the other hit, this is treated as an illegal overlap, irrespective of
//            // the  overlapCutOff used
//            boolean oneHitInsideTheOther = false;
//            if (eS <= lS && lE <= eE) {
//                oneHitInsideTheOther = true;
//            }
//            int overlapLen = Consts.min(eE, lE) - lS + 1;
//            float bestHitSoFarLenOverlapCutOff = (DOMAINFINDER_DOMAIN_OVERLAP * bestHitSoFarLen / 100);
//            float matchLenOverlapCutOff = (DOMAINFINDER_DOMAIN_OVERLAP * matchLen / 100);
//            /** Note that the overlap cutoff is based on (already stored) bestHitSoFar, rather than (new) match;
//             * Gene3D like to err on the conservative side - since bestHitSoFar can potentially be shorter than
//             * the currently-considered hit (match), the overlap cutoff in absolute AAs derived from it is likely to
//             * be more restrictive than if it had been derived from the potentially longer current hit (match).
//             * Therefore deriving overlapCutOff from bestHitSoFarLen rather than matchLen will result in rejecting
//             * more hits.
//             */
//
//            if (oneHitInsideTheOther ||
//                    (float) overlapLen > bestHitSoFarLenOverlapCutOff ||
//                    (float) overlapLen > matchLenOverlapCutOff) {
//                // If the current hit overlaps with the best hit so far for that sequence range
//                overlaps = true; // this will break out of the main overlaps processing loop
//                // We already know that bestHitSoFarForThisSeqRange.eValue <= match.eValue because of the
//                // sorted way Gene3D raw hits are retrieved from Oracle
//                // Hence, match has either higher eval than bestHitSoFarForThisSeqRange.eValue or the same eval but is longer
//                // Note that short hits are preferred - GET_GENE3D_RAW_RESULTS_SQL returns them first
//            }
//        }
//
//        if (!overlaps) {
//            seqHitsSoFar.add(match);
//        } else {
//            // There were overlaps, but match was rejected by one of the existsing hits
//        }

        return item;
        
    }
}
