package uk.ac.ebi.interpro.scan.io.match.writer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a test for GFFResultWriterForNucSeqs.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class GFFResultWriterForNucSeqsTest {

    private Map<String, String> identifierToSeqMap;

    @Before
    public void init() {
        identifierToSeqMap = new TreeMap<String, String>(new ProteinMatchesGFFResultWriter.Gff3FastaSeqIdComparator());
    }


    /**
     * The expected sorting order is represented by the hash map values (indices).
     */
    @Test
    public void testGff3FastaSeqIdComparator() {
        Assert.assertNotNull("Tree map shouldn't be NULL!", identifierToSeqMap);
        Assert.assertEquals("Tree map should be empty!", 0, identifierToSeqMap.size());
        //target sequences - map values represent the index after sorting
        identifierToSeqMap.put("LLLLL", "5");
        identifierToSeqMap.put("ABCDE", "4");
        identifierToSeqMap.put("ABCDD", "3");
        identifierToSeqMap.put("ABCCC", "2");
        identifierToSeqMap.put("ABBBB", "1");
        identifierToSeqMap.put("AAAAA", "0");
        identifierToSeqMap.put("ZZZZZ", "8");
        identifierToSeqMap.put("Z1ZZZ", "7");
        identifierToSeqMap.put("Z10ZZ", "6");
        //example matches with one location
        String matchString = ProteinMatchesGFFResultWriter.MATCH_STRING;
        //example matches with multiple locations
        identifierToSeqMap.put(matchString + "8_100_110", "13");
        identifierToSeqMap.put(matchString + "8_35_50", "12");
        identifierToSeqMap.put(matchString + "8_25_30", "11");
        identifierToSeqMap.put(matchString + "8_15_20", "10");
        identifierToSeqMap.put(matchString + "8_1_10", "9");
        //
        Assert.assertEquals("Size of tree map incorrect", 14, identifierToSeqMap.size());

        //Copy values into a list and check if they are corrected indexed
        List<String> indexList = new ArrayList<String>();
        for (String value : identifierToSeqMap.values()) {
            indexList.add(value);
        }

        for (int i = 0; i < indexList.size(); i++) {
            Assert.assertEquals("Index and value should be the same!", i, Integer.parseInt(indexList.get(i)));
        }
    }
}
