package uk.ac.ebi.interpro.scan.persistence;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
//import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.persistence.PersistenceException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
/*import static junit.framework.Assert.fail;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.XrefSequenceIdentifier;  */
import uk.ac.ebi.interpro.scan.model.raw.PfamRawMatch;

import java.util.List;
//import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 06-Nov-2009
 * Time: 16:29:20
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/springconfig/spring-PfamDAOTest-config.xml"})
public class PfamDAOTest {
      /**
     * Logger for Junit logging. Log messages will be associated with the ProteinPersistenceTest class.
       *
     */
       final String UPI       = "UPI00015AC919";
            final String MODEL     = "PF04041";
            final String dbname     = "PFAM";
            final String dbVersion     = "24.0";
            final String generator     = "HMMER3.0";
            final String hmmBounds = "[]";
            final String alignment="";
            final long start =15,  hmmStart=18,hmmEnd=320;
            final long end=345;
            final double evalue=0.00005,score=512.3, locationEvalue=-5.60205984115601,locationScore=779.4;
    //private static Logger LOGGER = Logger.getLogger(PfamDAOTest.class);

    //private static final String[] ACCESSIONS = {"Q12345","P99999", "IPI01234567", "ENSP120923423423234"};

    private static final Long LONG_ZERO = 0L;

    private static final Long LONG_ONE = 1L;

    // First line of UPI0000000001.fasta
    public static final String GOOD       = "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    @Resource(name= "pfamDAO")
    private PfamDAO dao;

    public void setDao(PfamDAO dao) {
        this.dao = dao;
    }

    @Before
    @After
    public void emptyPfamTable(){
        dao.deleteAll();
        assertEquals("There should be no pfam entries in the Pfam table following a call to dao.deleteAll", LONG_ZERO, dao.count());
    }

    /**
     * This test exercises
     * GenericDAOImpl<Protein, Long>
     *                              .insert(Protein protein)
     *                              .count()
     *                              .read(Long id)
     *                              .retrieveAll()
     *                              .delete(Protein protein)
     */
    @Test
    public void storeAndRetrieveProtein(){
        emptyPfamTable();
        PfamRawMatch p = new PfamRawMatch(UPI,MODEL,dbname,dbVersion,generator, start, end);
        assertNotNull("The PfamDAOImpl object should be not-null.", dao);
        dao.insert(p);
        Long id = p.getId();
        assertEquals("The count of pfams in the database is not correct.", LONG_ONE, dao.count());
        PfamRawMatch retrievedPfam = dao.read(id);
        assertEquals("The pfam methodAc of the retrieved object is not the same as the original object.", p.getModel(), retrievedPfam.getModel());
        dao.delete(retrievedPfam);
        List<PfamRawMatch> retrievedPfamList = dao.retrieveAll();
        assertEquals("There should be no pfams in the database, following removal of the single pfam that was added.", 0, retrievedPfamList.size());
        assertEquals("The count of pfams in the database is not correct.", LONG_ZERO, dao.count());
        emptyPfamTable();
    }


    /**
     * Test that a PersistenceException is thrown if an attempt is made to
     * insert the same Protein twice.
     */
    @Test (expected = PersistenceException.class)
    public void testPersistenceExceptionOnSecondInsert(){
        emptyPfamTable();
        PfamRawMatch p = new PfamRawMatch("UPIblachabla","PF04041","PFAM","23.0","HMMER£.0",101,145);
        dao.insert(p);
        dao.insert(p);
    }

    /**
     * Tests the GenericDAO.getMaximumPrimaryKey() method.
     */
    @Test
    public void testMaximumPrimaryKeyCount(){
        emptyPfamTable();
        String[] proteinSequences = new String[]{"ABCD", "QWERTY", "PLOPPY", "GHGHGHGHG", "GRUFF"};
        Long maxPrimaryKey = 0l;
        for (String sequence : proteinSequences){
            PfamRawMatch p = new PfamRawMatch(sequence,"PF04041","PFAM","23.0","HMMER£.0",101,145);
            dao.insert(p);
            if (p.getId() > maxPrimaryKey){
                maxPrimaryKey = p.getId();
            }
        }
        assertEquals("The maximum primary key is not as expected", maxPrimaryKey, dao.getMaximumPrimaryKey());
    }

    /**
     * Tests the ProteinDAO.getProteinsInTransactionSlice() method.

    @Test
    public void testGetProteinsInTransactionSlice(){
        emptyProteinTable();
        String[] proteinSequences = new String[]{"ABCD", "QWERTY", "PLOPPY", "GHGHGHGHG", "GRUFF", "SPLOD", "QUQUQUQU"};
        Long maxPrimaryKey = Long.MIN_VALUE;
        Long minPrimaryKey = Long.MAX_VALUE;
        for (String sequence : proteinSequences){
            Protein protein = new Protein(sequence);
            dao.insert(protein);
            if (protein.getId() > maxPrimaryKey){
                maxPrimaryKey = protein.getId();
            }
            if (protein.getId() < minPrimaryKey){
                minPrimaryKey = protein.getId();
            }
        }
        assertEquals("The difference between the highest and lowest primary key values is not as expected.", (proteinSequences.length - 1), maxPrimaryKey - minPrimaryKey);
        // Build a TransactionSlice including the protein ID range of the proteins just entered.
        //TransactionSlice slice = new RawTransactionSliceImpl(minPrimaryKey, maxPrimaryKey);
        // Now try to retrieve the proteins
        //List<Protein> retrievedProteins = dao.getProteinsInTransactionSlice(slice);
        //assertEquals("The wrong number of proteins were returned from the database.", proteinSequences.length, retrievedProteins.size());

        // Now build a new, smaller slice and see if a smaller number of proteins are returned.
        //slice = new RawTransactionSliceImpl(minPrimaryKey + 1, maxPrimaryKey - 1);
        //retrievedProteins= dao.getProteinsInTransactionSlice(slice);
        //assertEquals("The wrong number of proteins returned from the database for the smaller slice.", (proteinSequences.length - 2), retrievedProteins.size());
    }
      */
}
