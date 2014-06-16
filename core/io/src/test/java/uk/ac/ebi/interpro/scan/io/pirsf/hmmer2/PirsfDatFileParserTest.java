package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Run tests for parsing the pirsf.dat file.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfDatFileParserTest extends TestCase {
    private static final String TEST_FILE_NAME = "data/pirsf/hmmer2/pirsf.dat";

    @Test
    public void testPirsfDatParser() {

        // Setup expected result
        final String MODEL_ACC_1 = "PIRSF000077";
        PirsfDatRecord pirsfDat1 = new PirsfDatRecord(
                MODEL_ACC_1,
                "Thioredoxin",
                new String[]{"110.136452241715", "9.11541109440914", "20.3", "167.482261208577", "57.6586203540026"},
                false);

        final String MODEL_ACC_2 = "PIRSF000729";
        PirsfDatRecord pirsfDat2 = new PirsfDatRecord(
                MODEL_ACC_2,
                "Glutamate 5-kinase",
                new String[]{"358.270731707317", "40.8471702485214", "47.1", "486.230487804878", "168.540809659098"},
                true);

        final String MODEL_ACC_3 = "PIRSF016158";
        Set<String> subFams = new HashSet<String>();
        subFams.add("PIRSF500165");
        subFams.add("PIRSF500166");
        PirsfDatRecord pirsfDat3 = new PirsfDatRecord(
                MODEL_ACC_3,
                "Methenyltetrahydromethanopterin dehydrogenase, Hmd type",
                new String[]{"343.416666666667", "12.4422910294134", "550.9", "678.941666666667", "75.8024760729346"},
                false, subFams);

        final String MODEL_ACC_4 = "PIRSF000331";
        subFams = new HashSet<String>();
        subFams.add("PIRSF500125");
        PirsfDatRecord pirsfDat4 = new PirsfDatRecord(
                MODEL_ACC_4,
                "4-hydroxyphenylacetate 3-monooxygenase/4-hydroxyphenylacetate-3-hydroxylase",
                new String[]{"500.683168316832", "19.8690365609757", "187.3", "739.055445544554", "234.211730908273"},
                true, subFams);

        Map<String, PirsfDatRecord> expectedResult = new HashMap<String, PirsfDatRecord>();
        expectedResult.put(MODEL_ACC_1, pirsfDat1);
        expectedResult.put(MODEL_ACC_2, pirsfDat2);
        expectedResult.put(MODEL_ACC_3, pirsfDat3);
        expectedResult.put(MODEL_ACC_4, pirsfDat4);

        // Run test method
        Resource testFile = new ClassPathResource(TEST_FILE_NAME);

        Map<String, PirsfDatRecord> actualResult = null;
        try {
            actualResult = PirsfDatFileParser.parse(testFile);
        } catch (IOException e) {
            assertTrue("IO exception occurred during parsing of pirsf.dat file!", false);
            e.printStackTrace();
        }

        assertNotNull("Expected result should be null at this time!", expectedResult);
        assertNotNull("Actual result should be null at this time!", actualResult);
        // Compare actual result with expected result
        assertEquals("The expected result size is different to the actual!", 4, actualResult.size());
        assertEquals("The expected result size is different to the actual!", expectedResult.size(), actualResult.size());
        // Test model accession 1
        assertEquals("Attribute blast required is different to the expected one!", expectedResult.get(MODEL_ACC_1).isBlastRequired(), actualResult.get(MODEL_ACC_1).isBlastRequired());
        assertEquals("Attribute mean score is different to the expected one!", expectedResult.get(MODEL_ACC_1).getMeanScore(), actualResult.get(MODEL_ACC_1).getMeanScore());
        assertEquals("Attribute mean sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_1).getMeanSeqLen(), actualResult.get(MODEL_ACC_1).getMeanSeqLen());
        assertEquals("Attribute min score is different to the expected one!", expectedResult.get(MODEL_ACC_1).getMinScore(), actualResult.get(MODEL_ACC_1).getMinScore());
        assertEquals("Attribute model accession is different to the expected one!", expectedResult.get(MODEL_ACC_1).getModelAccession(), actualResult.get(MODEL_ACC_1).getModelAccession());
        assertEquals("Attribute model name is different to the expected one!", expectedResult.get(MODEL_ACC_1).getModelName(), actualResult.get(MODEL_ACC_1).getModelName());
        assertEquals("Attribute standard deviation score is different to the expected one!", expectedResult.get(MODEL_ACC_1).getStdDevScore(), actualResult.get(MODEL_ACC_1).getStdDevScore());
        assertEquals("Attribute standard deviation sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_1).getStdDevSeqLen(), actualResult.get(MODEL_ACC_1).getStdDevSeqLen());
        assertEquals("Attribute subfamilies is different to the expected one!", expectedResult.get(MODEL_ACC_1).getSubFamilies(), actualResult.get(MODEL_ACC_1).getSubFamilies());
        // Test model accession 2
        assertEquals("Attribute blast required is different to the expected one!", expectedResult.get(MODEL_ACC_2).isBlastRequired(), actualResult.get(MODEL_ACC_2).isBlastRequired());
        assertEquals("Attribute mean score is different to the expected one!", expectedResult.get(MODEL_ACC_2).getMeanScore(), actualResult.get(MODEL_ACC_2).getMeanScore());
        assertEquals("Attribute mean sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_2).getMeanSeqLen(), actualResult.get(MODEL_ACC_2).getMeanSeqLen());
        assertEquals("Attribute min score is different to the expected one!", expectedResult.get(MODEL_ACC_2).getMinScore(), actualResult.get(MODEL_ACC_2).getMinScore());
        assertEquals("Attribute model accession is different to the expected one!", expectedResult.get(MODEL_ACC_2).getModelAccession(), actualResult.get(MODEL_ACC_2).getModelAccession());
        assertEquals("Attribute model name is different to the expected one!", expectedResult.get(MODEL_ACC_2).getModelName(), actualResult.get(MODEL_ACC_2).getModelName());
        assertEquals("Attribute standard deviation score is different to the expected one!", expectedResult.get(MODEL_ACC_2).getStdDevScore(), actualResult.get(MODEL_ACC_2).getStdDevScore());
        assertEquals("Attribute standard deviation sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_2).getStdDevSeqLen(), actualResult.get(MODEL_ACC_2).getStdDevSeqLen());
        assertEquals("Attribute subfamilies is different to the expected one!", expectedResult.get(MODEL_ACC_2).getSubFamilies(), actualResult.get(MODEL_ACC_2).getSubFamilies());
        // Test model accession 3
        assertEquals("Attribute blast required is different to the expected one!", expectedResult.get(MODEL_ACC_3).isBlastRequired(), actualResult.get(MODEL_ACC_3).isBlastRequired());
        assertEquals("Attribute mean score is different to the expected one!", expectedResult.get(MODEL_ACC_3).getMeanScore(), actualResult.get(MODEL_ACC_3).getMeanScore());
        assertEquals("Attribute mean sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_3).getMeanSeqLen(), actualResult.get(MODEL_ACC_3).getMeanSeqLen());
        assertEquals("Attribute min score is different to the expected one!", expectedResult.get(MODEL_ACC_3).getMinScore(), actualResult.get(MODEL_ACC_3).getMinScore());
        assertEquals("Attribute model accession is different to the expected one!", expectedResult.get(MODEL_ACC_3).getModelAccession(), actualResult.get(MODEL_ACC_3).getModelAccession());
        assertEquals("Attribute model name is different to the expected one!", expectedResult.get(MODEL_ACC_3).getModelName(), actualResult.get(MODEL_ACC_3).getModelName());
        assertEquals("Attribute standard deviation score is different to the expected one!", expectedResult.get(MODEL_ACC_3).getStdDevScore(), actualResult.get(MODEL_ACC_3).getStdDevScore());
        assertEquals("Attribute standard deviation sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_3).getStdDevSeqLen(), actualResult.get(MODEL_ACC_3).getStdDevSeqLen());
        assertEquals("Attribute subfamilies is different to the expected one!", expectedResult.get(MODEL_ACC_3).getSubFamilies(), actualResult.get(MODEL_ACC_3).getSubFamilies());
        // Test model accession 4
        assertEquals("Attribute blast required is different to the expected one!", expectedResult.get(MODEL_ACC_4).isBlastRequired(), actualResult.get(MODEL_ACC_4).isBlastRequired());
        assertEquals("Attribute mean score is different to the expected one!", expectedResult.get(MODEL_ACC_4).getMeanScore(), actualResult.get(MODEL_ACC_4).getMeanScore());
        assertEquals("Attribute mean sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_4).getMeanSeqLen(), actualResult.get(MODEL_ACC_4).getMeanSeqLen());
        assertEquals("Attribute min score is different to the expected one!", expectedResult.get(MODEL_ACC_4).getMinScore(), actualResult.get(MODEL_ACC_4).getMinScore());
        assertEquals("Attribute model accession is different to the expected one!", expectedResult.get(MODEL_ACC_4).getModelAccession(), actualResult.get(MODEL_ACC_4).getModelAccession());
        assertEquals("Attribute model name is different to the expected one!", expectedResult.get(MODEL_ACC_4).getModelName(), actualResult.get(MODEL_ACC_4).getModelName());
        assertEquals("Attribute standard deviation score is different to the expected one!", expectedResult.get(MODEL_ACC_4).getStdDevScore(), actualResult.get(MODEL_ACC_4).getStdDevScore());
        assertEquals("Attribute standard deviation sequence length is different to the expected one!", expectedResult.get(MODEL_ACC_4).getStdDevSeqLen(), actualResult.get(MODEL_ACC_4).getStdDevSeqLen());
        assertEquals("Attribute subfamilies is different to the expected one!", expectedResult.get(MODEL_ACC_4).getSubFamilies(), actualResult.get(MODEL_ACC_4).getSubFamilies());
    }
}