package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfFileUtil;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatFileParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * PIRSF post-processing.
 * <p/>
 * Also requires extra information from:
 * - pirsf.dat file.
 * - sf.tb file.
 * <p/>
 * Algorithm:
 * ==========
 * <p/>
 * STEP 1:
 * -------
 * For each protein in the i5 input file, e.g. test_proteins.fasta:
 * >A2YIW7
 * MAAEEGVVIACHNKDEFDAQMTKAKEAGKVVIIDFTASWCGPCRFIAPVFAEYAKKFPGAVFLKVDVDELKEVAEKYNVE
 * AMPTFLFIKDGAEADKVVGARKDDLQNTIVKHVGATAASASA
 * <p/>
 * Calculate the length of the sequence.
 * <p/>
 * STEP 2:
 * -------
 * Use data from the hmmer2 output and the pirsf.dat file to filter matches, ensuring that:
 * - Check that the model covers > 80% of sequence
 * - Score is greater than cutoff
 * - EITHER difference between model length and protein length is less than 3.5 times the standard deviation
 * OR is less than 50 amino acids
 * <p/>
 * STEP 3:
 * -------
 * Find the match with the smallest e-value in this protein (a model Id will have 0 or 1 match)
 * <p/>
 * STEP 4:
 * -------
 * Run blast (if required - check pirsf.dat file) to check Blast agrees that this model Id is the best match
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class OverlapPostProcessor implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(OverlapPostProcessor.class.getName());

    //    File parser
    private PirsfDatFileParser pirsfDatFileParser;

    //    Resources
    private Resource pirsfDatFileResource;


    @Required
    public void setPirsfDatFileParser(PirsfDatFileParser pirsfDatFileParser) {
        this.pirsfDatFileParser = pirsfDatFileParser;
    }

    @Required
    public void setPirsfDatFileResource(Resource pirsfDatFileResource) {
        this.pirsfDatFileResource = pirsfDatFileResource;
    }

    /**
     * Perform overlap post processing.
     *
     * @param rawMatches Raw matches to post process.
     * @param proteinLengthsMap Map of protein Id to sequence length.
     * @param filteredMatchesFilePath Temporary file path and name for recording which protein Ids passed this
     *                                filtering step and DO NOT need to be blasted.
     * @param blastMatchesFilePath Temporary file path and name for recording which protein Ids passed this
     *                             filtering step but also DO need to be blasted next.
     * @throws java.io.IOException If pirsf.dat file could not be read
     */
    public void process(Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches,
                        Map<Long, Integer> proteinLengthsMap,
                        String filteredMatchesFilePath,
                        String blastMatchesFilePath) throws IOException {

        // Read in pirsf.dat file
        Map<String, PirsfDatRecord> pirsfDatRecordMap = pirsfDatFileParser.parse(pirsfDatFileResource);

        // A Map between protein IDs and the match with the best match (smallest e-value)
        Map<String, PIRSFHmmer2RawMatch> proteinIdBestMatchMap = new HashMap<String, PIRSFHmmer2RawMatch>(); // Blast not required
        Map<String, PIRSFHmmer2RawMatch> proteinIdBestMatchToBeBlastedMap = new HashMap<String, PIRSFHmmer2RawMatch>(); // Blast required

        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (RawProtein<PIRSFHmmer2RawMatch> protein : rawMatches) {
            // Retrieve data necessary to perform filtering on this protein
            String proteinId = protein.getProteinIdentifier();
            int proteinLength = proteinLengthsMap.get(Long.parseLong(proteinId));
            PIRSFHmmer2RawMatch bestMatch = doOverlapFiltering(protein, pirsfDatRecordMap, proteinLength);
            if (bestMatch != null) {
                if (doBlastCheck(proteinId, bestMatch, pirsfDatRecordMap)) {
                    // Protein has passed this filtering step but some blast post processing is still required later
                    proteinIdBestMatchToBeBlastedMap.put(proteinId, bestMatch);
                } else {
                    // Protein has passed this filtering step and blast post processing is not required
                    proteinIdBestMatchMap.put(proteinId, bestMatch);
                }
            }
            // Else no matches survived the filtering stage so nothing to record
        }

        // Ready for persistence now - no blast check required
        PirsfFileUtil.writeProteinBestMatchesToFile(filteredMatchesFilePath, proteinIdBestMatchMap);

        // Need to be blasted in final filtering step
        PirsfFileUtil.writeProteinBestMatchesToFile(blastMatchesFilePath, proteinIdBestMatchToBeBlastedMap);
    }

    /**
     * Checks if protein sequence needs to be BLASTed or not.
     *
     * @return FALSE if not.
     */
    private boolean doBlastCheck(String proteinId, PIRSFHmmer2RawMatch match, Map<String, PirsfDatRecord> pirsfDatRecordMap) {
        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        String modelId = match.getModelId();
        PirsfDatRecord pirsfDatRecord = pirsfDatRecordMap.get(modelId);
        if (pirsfDatRecord != null && pirsfDatRecord.isBlastRequired()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Need to BLAST protein with identifier " + proteinId + " because model Id " + modelId +
                        " is annotated with BLAST=true.");
            }
            return true;
        }
        return false;
    }


    /**
     * Performs the overlap filtering for all HMMER2 matches of the specified protein and then decides on the best
     * match of those that remain (smallest e-value).
     *
     * @return The best match for this protein (smallest e-value), or NULL if no matches passed the filtering.
     */
    private PIRSFHmmer2RawMatch doOverlapFiltering(RawProtein<PIRSFHmmer2RawMatch> protein,
                                                   Map<String, PirsfDatRecord> pirsfDatRecordMap,
                                                   int proteinLength) {

        Double minEvalue = null;
        PIRSFHmmer2RawMatch matchWithMinEvalue = null;

        for (PIRSFHmmer2RawMatch match : protein.getMatches()) {
            String modelId = match.getModelId();
            PirsfDatRecord pirsfDatRecord = pirsfDatRecordMap.get(modelId);
            if (pirsfDatRecord == null) {
                // If the model Id is not in the pirsf.dat file then we can't do the filtering - reject the match
                LOGGER.warn("Model Id " + modelId + " not found in the pirsf.dat file, raw match rejected: " + match);
                continue;
            }

            // Perform first filtering check
            if (!checkOverlapCriterion(proteinLength, match, pirsfDatRecord)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Removing PIRSF match with model Id " + modelId + "...");
                }
                continue; // Failed so move on to the next raw match
            }

            double eValue = match.getLocationEvalue();
            if ((minEvalue == null && matchWithMinEvalue == null) || (eValue < minEvalue)) {
                // Can reduce the minimum mean e-value for this protein
                minEvalue = eValue;
                matchWithMinEvalue = match;
            }
        }
        return matchWithMinEvalue; // Could still be NULL if no matches passed the filtering
    }


    /**
     * Initial filter checks for a raw match:
     * - Check that the model covers > 80% of sequence
     * - Score is greater than cutoff
     * - EITHER difference between model length and protein length is less than 3.5 times the standard deviation
     * OR is less than 50 amino acids
     *
     * @param proteinLength  Length of the sequence
     * @param pirsfRawMatch  Raw match data
     * @param pirsfDatRecord Data from pirsf.dat
     * @return TRUE if raw match passed the filter checks, otherwise FALSE.
     */
    protected boolean checkOverlapCriterion(int proteinLength, PIRSFHmmer2RawMatch pirsfRawMatch, PirsfDatRecord pirsfDatRecord) {
        // Data from HMMER2 raw output
        int seqFrom = pirsfRawMatch.getLocationStart();
        int seqTo = pirsfRawMatch.getLocationEnd();
        double locationScore = pirsfRawMatch.getLocationScore();

        // Data from pirsf.dat file
        double meanSeqLen = pirsfDatRecord.getMeanSeqLen();
        double minScore = pirsfDatRecord.getMinScore();
        double stdDevSeqLen = pirsfDatRecord.getStdDevSeqLen();

        // Perform calculations
        float overlap = (float) (seqTo - seqFrom + 1) / (float) proteinLength;
        double lenDifference = Math.abs((double) proteinLength - meanSeqLen);
        if (overlap >= 0.8f && locationScore >= minScore && (lenDifference < 3.5d * stdDevSeqLen || lenDifference < 50.0d)) {
            return true;
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Raw match filtered out: " + pirsfRawMatch);
            }
            return false;
        }
    }

}
