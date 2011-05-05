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
import java.util.HashSet;
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
 * Find the model Id with the smallest mean e-value in this protein
 * <p/>
 * STEP 4:
 * -------
 * Run blast (if required - check pirsf.dat file)
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

    //    Specifies the location where to store the temporary file
    private String filteredMatchesFileName;

    private String blastMatchesFileName;


    @Required
    public void setPirsfDatFileParser(PirsfDatFileParser pirsfDatFileParser) {
        this.pirsfDatFileParser = pirsfDatFileParser;
    }

    @Required
    public void setPirsfDatFileResource(Resource pirsfDatFileResource) {
        this.pirsfDatFileResource = pirsfDatFileResource;
    }

    @Required
    public void setFilteredMatchesFileName(String filteredMatchesFilePathTemplate) {
        this.filteredMatchesFileName = filteredMatchesFilePathTemplate;
    }

    @Required
    public void setBlastMatchesFileName(String blastMatchesFilePathTemplate) {
        this.blastMatchesFileName = blastMatchesFilePathTemplate;
    }

    /**
     * Perform overlap post processing.
     *
     * @param rawMatches Raw matches to post process.
     * @return Filtered matches
     * @throws java.io.IOException If pirsf.dat file could not be read
     */
//    TODO: Intermediate state, finish implementation
    public void process(Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches,
                        Map<Long, Integer> proteinLengthsMap,
                        String temporaryFileDirectory) throws IOException {

        // Read in pirsf.dat file
        Map<String, PirsfDatRecord> pirsfDatRecordMap = pirsfDatFileParser.parse(pirsfDatFileResource);

        Set<String> passedProteinIds = new HashSet<String>();
        // A Map between protein IDs and model accessions with the minimum e-value
        Map<String, String> proteinIDModelAccMap = new HashMap<String, String>();
        Set<RawProtein<PIRSFHmmer2RawMatch>> matchesBlastReqd = doOverlapStep(rawMatches, passedProteinIds, proteinLengthsMap, pirsfDatRecordMap, proteinIDModelAccMap);

        PirsfFileUtil.writeFilteredRawMatchesToFile(temporaryFileDirectory, filteredMatchesFileName, passedProteinIds);
        writeBlastRawMatchesToFile(temporaryFileDirectory, matchesBlastReqd, proteinIDModelAccMap);
    }

    private void writeBlastRawMatchesToFile(String temporaryFileDirectory,
                                            Set<RawProtein<PIRSFHmmer2RawMatch>> resultSet,
                                            Map<String, String> proteinIDModelAccMap) throws IOException {
        BufferedWriter writer = null;
        try {
            File file = PirsfFileUtil.createTmpFile(temporaryFileDirectory, blastMatchesFileName);
            if (!file.exists()) {
                return; // File already exists, so don't try to write it again.
            }
            writer = new BufferedWriter(new FileWriter(file));
            for (RawProtein<PIRSFHmmer2RawMatch> protein : resultSet) {
                String protID = protein.getProteinIdentifier();
                writer.write(protID);
                String modelAccession = proteinIDModelAccMap.get(protID);
                if (modelAccession != null) {
                    writer.write('\t');
                    writer.write(modelAccession);
                }
                writer.write('\n');
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Performs the overlap filtering step of the PIRSF post processing. Protein sequences which keep the overlap criterion will
     * be added to the result set. Protein sequences which need to go through the BLAST step will be returned.
     *
     * @return Set of matches which need to go through the BLAST step.
     */
    private Set<RawProtein<PIRSFHmmer2RawMatch>> doOverlapStep(Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches,
                                                               Set<String> proteinIds,
                                                               Map<Long, Integer> proteinLengthsMap,
                                                               Map<String, PirsfDatRecord> pirsfDatRecordMap,
                                                               Map<String, String> proteinIDModelAccMap) {
        Set<RawProtein<PIRSFHmmer2RawMatch>> result = new HashSet<RawProtein<PIRSFHmmer2RawMatch>>();
        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (RawProtein<PIRSFHmmer2RawMatch> protein : rawMatches) {
            // Retrieve data necessary to perform filtering on this protein
            String proteinId = protein.getProteinIdentifier();
            int proteinLength = proteinLengthsMap.get(Long.parseLong(proteinId));
            RawProtein<PIRSFHmmer2RawMatch> resultProtein = doOverlapFiltering(protein, pirsfDatRecordMap, proteinLength, proteinIDModelAccMap);
            if (!doBlastCheck(resultProtein, pirsfDatRecordMap)) {
                proteinIds.add(resultProtein.getProteinIdentifier());
            } else {
                result.add(resultProtein);
            }
        }
        return result;
    }

    /**
     * Checks if protein sequence needs to be BLASTed or not.
     *
     * @return FALSE if not.
     */
    private boolean doBlastCheck(RawProtein<PIRSFHmmer2RawMatch> protein, Map<String, PirsfDatRecord> pirsfDatRecordMap) {
        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (PIRSFHmmer2RawMatch match : protein.getMatches()) {
            String modelId = match.getModelId();
            PirsfDatRecord pirsfDatRecord = pirsfDatRecordMap.get(modelId);
            if (pirsfDatRecord != null && pirsfDatRecord.isBlastRequired()) {
                LOGGER.info("Need to BLAST protein with identifier " + protein.getProteinIdentifier() + " because model Id " + modelId +
                        " is annotated with BLAST=true.");
                return true;
            }
        }
        return false;
    }


    /**
     * Performs the overlap filtering for all HMMR matches of the specified protein.
     *
     * @return A set of filtered raw matches associated to the specified protein identifier.
     */
    private RawProtein<PIRSFHmmer2RawMatch> doOverlapFiltering(RawProtein<PIRSFHmmer2RawMatch> protein,
                                                               Map<String, PirsfDatRecord> pirsfDatRecordMap,
                                                               int proteinLength,
                                                               Map<String, String> proteinIDModelAccMap) {
        RawProtein<PIRSFHmmer2RawMatch> result = new RawProtein<PIRSFHmmer2RawMatch>(protein.getProteinIdentifier());
        // Used to find the model Id with the smallest mean e-value for this protein
        Double minMeanEvalue = null;
        String modelIdWithMinMeanEvalue = null;

        for (PIRSFHmmer2RawMatch match : protein.getMatches()) {
            String modelId = match.getModelId();
            PirsfDatRecord pirsfDatRecord = pirsfDatRecordMap.get(modelId);
            if (pirsfDatRecord == null) {
                // If the model Id is not in the pirsf.dat file then we can't do the filtering - reject the match
                LOGGER.warn("Model Id " + modelId + " not found in the pirsf.dat file, raw match rejected: " + match);
                continue;
            }

            // Perform first filtering check
            if (checkOverlapCriterion(proteinLength, match, pirsfDatRecord)) {
                result.addMatch(match);
            } else {
                LOGGER.info("Removing PIRSF match with model Id " + modelId + "...");
                continue; // Failed so move on to the next raw match
            }

            double eValue = match.getLocationEvalue();
            if ((minMeanEvalue == null && modelIdWithMinMeanEvalue == null) || (eValue < minMeanEvalue)) {
                // Can reduce the minimum mean e-value for this protein
                minMeanEvalue = eValue;
                modelIdWithMinMeanEvalue = modelId;
            }
        }
        if (modelIdWithMinMeanEvalue != null) {
            proteinIDModelAccMap.put(protein.getProteinIdentifier(), modelIdWithMinMeanEvalue);
        }
        return result;
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
