package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatFileParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.io.pirsf.SfTbFileParser;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * PIRSF post-processing.
 *
 * Also requires extra information from:
 * - pirsf.dat file.
 * - sf.tb file.
 *
 * Algorithm:
 * ==========
 *
 * STEP 1:
 * -------
 * For each protein in the i5 input file, e.g. test_proteins.fasta:
 * >A2YIW7
 * MAAEEGVVIACHNKDEFDAQMTKAKEAGKVVIIDFTASWCGPCRFIAPVFAEYAKKFPGAVFLKVDVDELKEVAEKYNVE
 * AMPTFLFIKDGAEADKVVGARKDDLQNTIVKHVGATAASASA
 *
 * Calculate the length of the sequence.
 *
 * STEP 2:
 * -------
 * Use data from the hmmer2 output and the pirsf.dat file to filter matches, ensuring that:
 * - Check that the model covers > 80% of sequence
 * - Score is greater than cutoff
 * - EITHER difference between model length and protein length is less than 3.5 times the standard deviation
 *   OR is less than 50 amino acids
 *
 * STEP 3:
 * -------
 * Find the model Id with the smallest mean e-value in this protein
 *
 * STEP 4:
 * -------
 * Run blast (if required - check pirsf.dat file)
 *
 */
public class PirsfPostProcessing implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PirsfPostProcessing.class.getName());

    private PirsfDatFileParser pirsfDatFileParser;
    private Resource pirsfDatFileResource;

    private SfTbFileParser sfTbFileParser;
    private Resource sfTbFileResource;

    private Map<String, RawProtein<PIRSFHmmer2RawMatch>> allFilteredMatches = new HashMap<String, RawProtein<PIRSFHmmer2RawMatch>>();


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

    /**
     * Perform post processing.
     *
     * @param rawMatchesSet Raw matches to post process.
     * @return Filtered matches
     * @throws IOException If pirsf.dat file could not be read
     */
    public Map<String, RawProtein<PIRSFHmmer2RawMatch>> process(Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatchesSet,
                                                                Map<Long, Integer> proteinLengthsMap) throws IOException {

        // Read in pirsf.dat file
        Map<String, PirsfDatRecord> pirsfDatRecordMap = pirsfDatFileParser.parse(pirsfDatFileResource);

        // Read in sf.tb file
        Map<String, Integer> sfTbMap = sfTbFileParser.parse(sfTbFileResource);

        // Loop through the proteins and see if the matches need to be excluded (filtered) or not
        for (RawProtein<PIRSFHmmer2RawMatch> rawMatch : rawMatchesSet) {
            // Retrieve data necessary to perform filtering on this protein
            String proteinId = rawMatch.getProteinIdentifier();
            long proteinLength = proteinLengthsMap.get(Long.parseLong(proteinId));

            // Now do the processing
            processProtein(rawMatch, allFilteredMatches, pirsfDatRecordMap, proteinLength);
        }

        return allFilteredMatches;
    }

    private void processProtein(RawProtein<PIRSFHmmer2RawMatch> matchRawProtein,
                                Map<String, RawProtein<PIRSFHmmer2RawMatch>> filteredMatches,
                                Map<String, PirsfDatRecord> pirsfDatRecordMap,
                                long proteinLength) {

        for (PIRSFHmmer2RawMatch pirsfRawMatch : matchRawProtein.getMatches()) {

            String sequenceId = pirsfRawMatch.getSequenceIdentifier();

            // Data from HMMER2 raw output
            String modelId = pirsfRawMatch.getModelId();
            int seqFrom = pirsfRawMatch.getLocationStart();
            int seqTo = pirsfRawMatch.getLocationEnd();
            double score = pirsfRawMatch.getLocationScore();
            double evalue = pirsfRawMatch.getLocationEvalue();

            PirsfDatRecord pirsfDatRecord = pirsfDatRecordMap.get(modelId);
            if (pirsfDatRecord == null) {
                // If the model Id is not in the pirsf.dat file then we can't do the filtering - reject the match
                LOGGER.warn("Model Id " + modelId + " not found in the pirsf.dat file, raw match rejected: " + pirsfRawMatch);
                continue;
            }

            // Perform first filtering calculations
            double overlap = (seqTo - seqFrom + 1) / proteinLength;
            double meanSeqLen = pirsfDatRecord.getMeanSeqLen();
            double minScore = pirsfDatRecord.getMinScore();
            double stdDevSeqLen = pirsfDatRecord.getStdDevSeqLen();
            double lenDifference = Math.abs(proteinLength - meanSeqLen);
            if (overlap >= 0.8 && score >= minScore && (lenDifference < (3.5 * stdDevSeqLen) || lenDifference < 50)) {
                // Match is still OK for now, not filtered out yet
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Raw match passes initial filtering, still in with a chance: " + pirsfRawMatch);
                }

            }
            else {
                // This match won't be making it into the filtered list of matches, so now look at the next
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Raw match filtered out: " + pirsfRawMatch);
                }
                continue;
            }

            // TODO Find the model Id with the smallest mean e-value in this protein

            /*
             * TODO Run blast (if necessary).
             *
             * NOTE 15/04/2011:
             *
             * Onion did this:
             * /ebi/extserv/bin/ncbi-blast-2.2.6/blastall -p blastp -F F -e 0.0005 -b 300 -v 300 -d /ebi/sp/pro1/interpro/data/members/pirsf/274/sf.seq -m 8 -i  SEQUENCE FILE > OUTPUT_FILE
             *
             * PIRSF Perl script (pirsf.pl) did this:
             * $blastall -a $cpu -p blastp -F F -e 0.0001 -b 10000 -d $sfseq -i $infile -m 8
             *
             * TODO Which is right?
             */


            RawProtein<PIRSFHmmer2RawMatch> p;
            if (filteredMatches.containsKey(sequenceId)) {
                p = filteredMatches.get(sequenceId);
            } else {
                p = new RawProtein<PIRSFHmmer2RawMatch>(sequenceId);
                filteredMatches.put(sequenceId, p);
            }
            p.addMatch(pirsfRawMatch);
        }

    }

}
