package uk.ac.ebi.interpro.scan.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones, EMBL-EBI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinXrefDAOTest {

    @Resource
    private ProteinDAO proteinDAO;

    @Resource
    private ProteinXrefDAO proteinXrefDAO;

    @Test
    public void testGetNonUniqueXrefs() {
        final String sequence1 = "ASDDFQWERZXCZ";
        final String sequence2 = "YFCSTCSFDCSDTCRFT";

        final String identifier1 = "Q12345";
        final String identifier2 = "P99999";

        Protein protein1 = new Protein(sequence1);
        Protein protein2 = new Protein(sequence2);

        protein1.addCrossReferences(identifier1, identifier2);
        protein2.addCrossReferences(identifier1);

        // Store the proteins
        proteinDAO.insert(protein1);
        proteinDAO.insert(protein2);

        // Retrieve a List of non-unique sequences - should contain just Q12345.

        List<String> nonUniqueXrefs = proteinXrefDAO.getNonUniqueXrefs();
        Assert.assertNotNull(nonUniqueXrefs);
        Assert.assertEquals(1, nonUniqueXrefs.size());
        String nonUnique = nonUniqueXrefs.get(0);
        Assert.assertEquals(identifier1, nonUnique);
    }

    @Test
    public void testGetXrefAndProteinByProteinXrefIdentifier() {
        final String identifier1 = "Q12345";
        final String identifier2 = "P99999";

        Protein protein1 = new Protein("ASDDFQWERZXCZ");
        Protein protein2 = new Protein("YFCSTCSFDCSDTCRFT");
        Protein protein3 = new Protein("YFCS");


        protein1.addCrossReferences(identifier1, identifier2);
        protein2.addCrossReferences(identifier1);
        protein3.addCrossReferences(identifier1);

        // Store the proteins
        proteinDAO.insert(protein1);
        proteinDAO.insert(protein2);
        proteinDAO.insert(protein3);

        List<ProteinXref> proteinXrefs = proteinXrefDAO.getXrefAndProteinByProteinXrefIdentifier("Q12345");
        Assert.assertEquals(3, proteinXrefs.size());
        Long proteinId1 = proteinXrefs.get(0).getProtein().getId();
        Long proteinId2 = proteinXrefs.get(1).getProtein().getId();
        Long proteinId3 = proteinXrefs.get(2).getProtein().getId();
        Assert.assertEquals("", 1, proteinId3.compareTo(proteinId2));
        Assert.assertEquals("", 1, proteinId2.compareTo(proteinId1));
    }
}
