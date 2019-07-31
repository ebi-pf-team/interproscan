package uk.ac.ebi.interpro.scan.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.ac.ebi.interpro.scan.model.NucleotideSequence;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Maxim Scheremetjew, EMBL-EBI
 * @author Gift Nuka
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class NucleotideSequenceXrefDAOTest {

    @Resource
    private NucleotideSequenceDAO nucleotideSequenceDAO;

    @Resource
    private NucleotideSequenceXrefDAO nucleotideSequenceXrefDAO;

    @Test
    public void testGetNonUniqueXrefs() {
        final String sequence1 = "gatgatgtga";
        final String sequence2 = "ataggaattagatg";

        final String identifier1 = "Q12345";
        final String identifier2 = "P99999";

        NucleotideSequence nucleotideSequence1 = new NucleotideSequence(sequence1);
        NucleotideSequence nucleotideSequence2 = new NucleotideSequence(sequence2);

        nucleotideSequence1.addCrossReference(new NucleotideSequenceXref(identifier1));
        nucleotideSequence1.addCrossReference(new NucleotideSequenceXref(identifier2));
        nucleotideSequence2.addCrossReference(new NucleotideSequenceXref(identifier1));

        // Store the proteins
        nucleotideSequenceDAO.insert(nucleotideSequence1);
        nucleotideSequenceDAO.insert(nucleotideSequence2);

        // Retrieve a List of non-unique sequences - should contain just Q12345.

        List<String> nonUniqueXrefs = nucleotideSequenceXrefDAO.getNonUniqueXrefs();
        assertNotNull(nonUniqueXrefs);
        assertEquals(1, nonUniqueXrefs.size());
        String nonUnique = nonUniqueXrefs.get(0);
        assertEquals(identifier1, nonUnique);
    }
}
