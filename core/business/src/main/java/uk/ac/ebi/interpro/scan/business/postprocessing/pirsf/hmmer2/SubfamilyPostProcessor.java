package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfDatFileInfoHolder;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfFileUtil;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfSubfamilyFileParser;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
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
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SubfamilyPostProcessor implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SubfamilyPostProcessor.class.getName());

    //    File parser
    private PirsfSubfamilyFileParser subfamilyFileParser;

    private PirsfDatFileInfoHolder pirsfDatFileInfoHolder;

    @Required
    public void setSubfamilyFileParser(PirsfSubfamilyFileParser subfamilyFileParser) {
        this.subfamilyFileParser = subfamilyFileParser;
    }

    @Required
    public void setPirsfDatFileInfoHolder(PirsfDatFileInfoHolder pirsfDatFileInfoHolder) {
        this.pirsfDatFileInfoHolder = pirsfDatFileInfoHolder;
    }

    /**
     * Performs subfamily post processing.
     *
     * @param rawMatches               Raw matches to post process.
     * @param subFamilyMatchesFilePath Temporary file path and name for recording which protein Ids passed this
     *                                 filtering step.
     * @throws java.io.IOException If subfamilies.out file couldn't be read.
     */
    public void process(final Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches,
                        final String subFamilyMatchesFilePath,
                        final String subFamilyMapFilePath,
                        final Resource subFamilyMapFileResource) throws IOException {
        //Maps superfamily model accessions to PIRSF data record objects
        final Map<String, PirsfDatRecord> pirsfDatRecordMap = pirsfDatFileInfoHolder.getData();

        //Maps subfamilies to super families model accessions
        final Map<String, String> subfamToSuperFamMap = subfamilyFileParser.parse(subFamilyMapFileResource);

        // A Map between protein IDs and the best match (which holds min score criterion and has the smallest e-value)
        Map<String, PIRSFHmmer2RawMatch> proteinIdBestMatchMap = new HashMap<String, PIRSFHmmer2RawMatch>(); // Blast not required

        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (RawProtein<PIRSFHmmer2RawMatch> protein : rawMatches) {
            // Retrieve data necessary to perform filtering on this protein
            String proteinId = protein.getProteinIdentifier();
            PIRSFHmmer2RawMatch bestMatch = doSubfamilyFiltering(protein, pirsfDatRecordMap, subfamToSuperFamMap);
            if (bestMatch != null) {
                // Protein has passed this filtering step
                proteinIdBestMatchMap.put(proteinId, bestMatch);
            }
            // Else no matches survived the filtering stage so nothing to record
        }
        // Ready for persistence now - no blast check required
        PirsfFileUtil.writeProteinBestMatchesToFile(subFamilyMatchesFilePath, proteinIdBestMatchMap);
    }


    /**
     * Performs the subfamily filtering for all HMMER2 matches of the specified protein and then decides on the best
     * match of those that remain (smallest e-value).
     * 1.Step:<br>
     * Check if match is a subfamily
     * 2.Step:<br>
     * Check min score<br>
     * 3. Step:<br>
     * Check e-value
     *
     * @return The best match for this protein (smallest e-value), or NULL if no matches passed the filtering.
     */
    private PIRSFHmmer2RawMatch doSubfamilyFiltering(final RawProtein<PIRSFHmmer2RawMatch> protein,
                                                     final Map<String, PirsfDatRecord> pirsfDatRecordMap,
                                                     final Map<String, String> subfamToSuperFamMap) {
        Double minEvalue = null;
        PIRSFHmmer2RawMatch matchWithMinEvalue = null;

        for (PIRSFHmmer2RawMatch match : protein.getMatches()) {
            String subfamModelId = match.getModelId();
            //Check if match is a subfamily member
            if (subfamToSuperFamMap.containsKey(subfamModelId)) {
                //Get model infos for this sub family
                PirsfDatRecord pirsfDatRecord = pirsfDatRecordMap.get(subfamModelId);
                if (pirsfDatRecord == null) {
                    // If the model Id is not in the pirsf.dat file then we can't do the filtering - reject the match
                    LOGGER.warn("Model Id " + subfamModelId + " not found in the pirsf.dat file, raw match rejected: " + match);
                    continue;
                }

                // Perform first filtering check
                if (!checkMinScoreCriterion(match, pirsfDatRecord)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Removing PIRSF match with model Id " + subfamModelId + "...");
                    }
                    continue; // Failed so move on to the next raw match
                }

                //Perform e-value check
                double eValue = match.getLocationEvalue();
                if (minEvalue == null || eValue < minEvalue) {
                    // Can reduce the minimum mean e-value for this protein
                    minEvalue = eValue;
                    matchWithMinEvalue = match;
                }

            } else {
                LOGGER.debug("Model Id " + subfamModelId + " isn't a subfamilies. Therefore discard this and iterate over the next match.");
                continue;
            }
        }
        return matchWithMinEvalue; // Could still be NULL if no matches passed the filtering
    }


    /**
     * Checks sub family criterion.
     *
     * @param pirsfRawMatch  Raw match data
     * @param pirsfDatRecord Data from pirsf.dat
     * @return TRUE if raw match passed the filter checks, otherwise FALSE.
     */
    protected boolean checkMinScoreCriterion(final PIRSFHmmer2RawMatch pirsfRawMatch,
                                             final PirsfDatRecord pirsfDatRecord) {
        // Data from HMMER2 raw output
        final double locationScore = pirsfRawMatch.getLocationScore();
        // Data from pirsf.dat file
        final double minScore = pirsfDatRecord.getMinScore();

        if (locationScore >= minScore) {
            return true;
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Raw match filtered out: " + pirsfRawMatch);
            }
            return false;
        }
    }
}
