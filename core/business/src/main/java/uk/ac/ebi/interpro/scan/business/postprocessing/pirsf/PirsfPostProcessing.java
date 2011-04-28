package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfBlastResultParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatFileParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.io.pirsf.SfTbFileParser;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
public class PirsfPostProcessing implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PirsfPostProcessing.class.getName());

    //    File parser
    private PirsfDatFileParser pirsfDatFileParser;

    private SfTbFileParser sfTbFileParser;

    //    Resources
    private Resource pirsfDatFileResource;

    private Resource sfTbFileResource;

    private Resource blastDbFileResource;

    //    Misc
    private BinaryRunner binaryRunner;


    @Required
    public void setPirsfDatFileParser(PirsfDatFileParser pirsfDatFileParser) {
        this.pirsfDatFileParser = pirsfDatFileParser;
    }

    @Required
    public void setPirsfDatFileResource(Resource pirsfDatFileResource) {
        this.pirsfDatFileResource = pirsfDatFileResource;
    }

    @Required
    public void setSfTbFileParser(SfTbFileParser sfTbFileParser) {
        this.sfTbFileParser = sfTbFileParser;
    }

    @Required
    public void setSfTbFileResource(Resource sfTbFileResource) {
        this.sfTbFileResource = sfTbFileResource;
    }

    @Required
    public void setBinaryRunner(BinaryRunner binaryRunner) {
        this.binaryRunner = binaryRunner;
    }

    @Required
    public void setBlastDbFileResource(Resource blastDbFileResource) {
        this.blastDbFileResource = blastDbFileResource;
    }

    /**
     * Perform post processing.
     *
     * @param rawMatches Raw matches to post process.
     * @return Filtered matches
     * @throws IOException If pirsf.dat file could not be read
     */
//    TODO: Intermediate state, finish implementation
    public Set<RawProtein<PIRSFHmmer2RawMatch>> process(Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches,
                                                        Map<Long, Integer> proteinLengthsMap,
                                                        final String fastaFilePathName) throws IOException {

        // Read in pirsf.dat file
        Map<String, PirsfDatRecord> pirsfDatRecordMap = pirsfDatFileParser.parse(pirsfDatFileResource);

        // Read in sf.tb file
        Map<String, Integer> sfTbMap = sfTbFileParser.parse(sfTbFileResource);

        //Map between a protein and the model ID with MinMeanEvalue
        Map<RawProtein<PIRSFHmmer2RawMatch>, String> proteinModelIDMap = new HashMap<RawProtein<PIRSFHmmer2RawMatch>, String>();
        Set<RawProtein<PIRSFHmmer2RawMatch>> resultOfOverlapStep = doOverlapStep(rawMatches, proteinLengthsMap, pirsfDatRecordMap, proteinModelIDMap);
        Set<RawProtein<PIRSFHmmer2RawMatch>> resultOfBlastAnnotationStep =
                getBlastAnnotatedModelMatches(resultOfOverlapStep, proteinModelIDMap, pirsfDatRecordMap);

        Set<RawProtein<PIRSFHmmer2RawMatch>> resultOfBlastStep = doBlastStep(resultOfBlastAnnotationStep);

        return null;
    }

    //    TODO: Intermediate state, finish implementation
    private Set<RawProtein<PIRSFHmmer2RawMatch>> doBlastStep(Set<RawProtein<PIRSFHmmer2RawMatch>> resultOfBlastAnnotationStep) {
//        getBlastQuerySequences();

        LOGGER.info("Performing BLAST for ? ...");
        LOGGER.debug("Setting additional BLAST arguments...");
//        StringBuilder additionalArgs = getAdditionalBlastArguments(fastaFilePathName);
        InputStream is = null;
//        try {
//            LOGGER.info("Running BLAST from binary file...");
//            LOGGER.debug("...using the following arguments: ");
//            LOGGER.debug(binaryRunner.getArguments());
//            is = binaryRunner.run(additionalArgs.toString());
//        } catch (IOException e) {
//            LOGGER.error("Couldn't run BLAST from binary file!", e);
//        }

//        LOGGER.info("Parsing BLAST standard output...");
//        Map<String, Integer> blastResultMap = PirsfBlastResultParser.parseBlastStandardOutput(is);
//
//        LOGGER.info("Checking BLAST criteria...");
//        if (checkBlastCriterion(blastResultMap, sfTbMap, modelIdWithMinMeanEvalue)) {
//            filteredMatches.add(protein);
//        }
        return null;
    }

    private Set<RawProtein<PIRSFHmmer2RawMatch>> getBlastAnnotatedModelMatches(Set<RawProtein<PIRSFHmmer2RawMatch>> overlapFilteredMatches,
                                                                               Map<RawProtein<PIRSFHmmer2RawMatch>, String> proteinModelIDMap,
                                                                               Map<String, PirsfDatRecord> pirsfDatRecordMap) {
        Set<RawProtein<PIRSFHmmer2RawMatch>> result = new HashSet<RawProtein<PIRSFHmmer2RawMatch>>();
        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (RawProtein<PIRSFHmmer2RawMatch> protein : overlapFilteredMatches) {
            String modelIdWithMinMeanEvalue = proteinModelIDMap.get(protein);
            if (modelIdWithMinMeanEvalue != null) {
                PirsfDatRecord pirsfDatRecord = pirsfDatRecordMap.get(modelIdWithMinMeanEvalue);
                if (pirsfDatRecord == null) {
                    LOGGER.warn("Model Id " + modelIdWithMinMeanEvalue + " could not be found in pirsf.dat, rejected");
                }
                if (pirsfDatRecord.isBlastRequired()) {
                    result.add(protein);
                }
            }
        }
        return result;
    }

    /**
     * Performs the overlap filtering step of the PIRSF post processing.
     *
     * @return Set of overlap filtered raw matches.
     */
    private Set<RawProtein<PIRSFHmmer2RawMatch>> doOverlapStep(Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches,
                                                               Map<Long, Integer> proteinLengthsMap,
                                                               Map<String, PirsfDatRecord> pirsfDatRecordMap,
                                                               Map<RawProtein<PIRSFHmmer2RawMatch>, String> proteinModelIDMap) {
        Set<RawProtein<PIRSFHmmer2RawMatch>> result = new HashSet<RawProtein<PIRSFHmmer2RawMatch>>();
        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (RawProtein<PIRSFHmmer2RawMatch> protein : rawMatches) {
            // Retrieve data necessary to perform filtering on this protein
            String proteinId = protein.getProteinIdentifier();
            int proteinLength = proteinLengthsMap.get(Long.parseLong(proteinId));
            RawProtein<PIRSFHmmer2RawMatch> resultProtein = doOverlapFiltering(protein, pirsfDatRecordMap, proteinLength, proteinModelIDMap);
            result.add(resultProtein);
        }
        return result;
    }

    /**
     * Performs the overlap filtering for all HMMR matches of the specified protein.
     *
     * @return A set of filtered raw matches associated to the specified protein identifier.
     */
    private RawProtein<PIRSFHmmer2RawMatch> doOverlapFiltering(RawProtein<PIRSFHmmer2RawMatch> protein,
                                                               Map<String, PirsfDatRecord> pirsfDatRecordMap,
                                                               int proteinLength,
                                                               Map<RawProtein<PIRSFHmmer2RawMatch>, String> proteinModelIDMap) {
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
            proteinModelIDMap.put(protein, modelIdWithMinMeanEvalue);
        }
        return result;
    }


    protected boolean checkBlastCriterion(final Map<String, Integer> blastResultMap,
                                          final Map<String, Integer> sfTbMap,
                                          String pirsfModelID) {
        if (blastResultMap != null && blastResultMap.size() > 0) {
            pirsfModelID = pirsfModelID.substring(3);
            Integer numberOfBlastHits = blastResultMap.get(pirsfModelID);
            Integer sfTbValue = sfTbMap.get(pirsfModelID);
            if (numberOfBlastHits != null &&
                    (numberOfBlastHits > 9 ||
                            sfTbValue != null && ((float) numberOfBlastHits / (float) sfTbValue > 0.3334f))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a set of additional BLAST program arguments.
     */
    private StringBuilder getAdditionalBlastArguments(final String fastaFilePathName) {
        StringBuilder result = new StringBuilder();
        try {
            result.append("-i ")
                    .append(fastaFilePathName + " ")
                    .append("-d ")
                    .append(blastDbFileResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Couldn't get file resource for BLAST database!", e);
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
