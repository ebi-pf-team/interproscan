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

    private String blastedMatchesFileName;

    public void setBlastedMatchesFileName(String blastedMatchesFileName) {
        this.blastedMatchesFileName = blastedMatchesFileName;
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
     * @return Filtered matches
     * @throws java.io.IOException If pirsf.dat file could not be read
     */
    public void process(Map<Long, String> proteinIdMap,
                        Map<String, Integer> blastResultMap,
                        String temporaryFileDirectory) throws IOException {

        // Read in sf.tb file
        Map<String, Integer> sfTbMap = sfTbFileParser.parse(sfTbFileResource);

        Set<String> passedBlastedProteinIds = new HashSet<String>();

        for (Long proteinId : proteinIdMap.keySet()) {

            String modelAcc = proteinIdMap.get(proteinId);
            if (checkBlastCriterion(sfTbMap, blastResultMap, modelAcc))
                passedBlastedProteinIds.add(String.valueOf(proteinId));
        }

        PirsfFileUtil.writeFilteredRawMatchesToFile(temporaryFileDirectory, blastedMatchesFileName, passedBlastedProteinIds);
    }


    protected boolean checkBlastCriterion(final Map<String, Integer> sfTbMap,
                                          final Map<String, Integer> blastResultMap,
                                          String modelAcc) {
        if (blastResultMap != null && blastResultMap.size() > 0) {
            modelAcc = modelAcc.substring(3);
            Integer numberOfBlastHits = blastResultMap.get(modelAcc);
            Integer sfTbValue = sfTbMap.get(modelAcc);

            if (numberOfBlastHits != null &&
                    (numberOfBlastHits > 9 ||
                            sfTbValue != null && ((float) numberOfBlastHits / (float) sfTbValue > 0.3334f))) {
                return true;
            }
        }
        return false;
    }


}
