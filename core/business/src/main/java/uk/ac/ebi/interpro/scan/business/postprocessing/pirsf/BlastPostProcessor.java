package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfFileUtil;
import uk.ac.ebi.interpro.scan.io.pirsf.SfTbFileParser;

import java.io.IOException;
import java.io.Serializable;
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
public class BlastPostProcessor implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(BlastPostProcessor.class.getName());

    private SfTbFileParser sfTbFileParser;

    private Resource sfTbFileResource;

    @Required
    public void setSfTbFileParser(SfTbFileParser sfTbFileParser) {
        this.sfTbFileParser = sfTbFileParser;
    }

    @Required
    public void setSfTbFileResource(Resource sfTbFileResource) {
        this.sfTbFileResource = sfTbFileResource;
    }


    /**
     * Perform post processing, using blast output results to confirm the best match.
     *
     * @param proteinIdMap Map protein Id to what the algorithm thinks is the best (lowest e-value) model Id for that
     *                     protein e.g. 2 -> PIRSF000350
     * @param blastResultMap Map proteinId and best model Id (according to blast) to number of hits in the blast output
     *                     e.g. 2-PIRSF000350 -> 12
     * @param blastedMatchesFilePath Name and path of temporary file to record a list of proteins that passed this
     *                               filtering.
     * @throws java.io.IOException If pirsf.dat file could not be read
     */
    public void process(Map<Long, String> proteinIdMap,
                        Map<String, Integer> blastResultMap,
                        String blastedMatchesFilePath) throws IOException {

        // Read in sf.tb file
        Map<String, Integer> sfTbMap = sfTbFileParser.parse(sfTbFileResource);

        Set<String> passedBlastedProteinIds = new HashSet<String>();

        for (Long proteinId : proteinIdMap.keySet()) {

            String modelId = proteinIdMap.get(proteinId);
            modelId = modelId.substring(3); // E.g. convert "PIRSF000350" to "SF000350"
            String key = proteinId.toString() + '-' + modelId;

            if (blastResultMap.containsKey(key)) {
                // Algorithm and blast both agree this is the best model Id match for this protein Id, do final checks
                if (checkBlastCriterion(sfTbMap, blastResultMap, key)) {
                    passedBlastedProteinIds.add(String.valueOf(proteinId));
                }
            }
        }

        PirsfFileUtil.writeFilteredRawMatchesToFile(blastedMatchesFilePath, passedBlastedProteinIds);
    }


    protected boolean checkBlastCriterion(final Map<String, Integer> sfTbMap,
                                          final Map<String, Integer> blastResultMap,
                                          String proteinIdModelId) {
        if (blastResultMap != null && blastResultMap.size() > 0) {
            String[] text = proteinIdModelId.split("-");
            if (text.length == 2) {
                Integer numberOfBlastHits = blastResultMap.get(proteinIdModelId);
                long proteinId = Long.parseLong(text[0]);
                String modelId = text[1];
                Integer sfTbValue = sfTbMap.get(modelId);

                if (numberOfBlastHits != null &&
                        (numberOfBlastHits > 9 ||
                                sfTbValue != null && ((float) numberOfBlastHits / (float) sfTbValue > 0.3334f))) {
                    return true;
                }
            }
            else {
                // Should never happen! Sanity check
                LOGGER.warn("Problem extracting protein Id and model Id when checking blast criteria");
            }
        }
        return false;
    }


}
