package uk.ac.ebi.interpro.scan.persistence.raw;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.annotation.Resource;
import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Map;


/**
 * Units tests for {@link PfamHmmer3RawMatchDAO}
 *
 * @author Manjula Thimma
 * @author Gift Nuka
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class PfamHmmer3RawMatchDAOTest {

    private final String UPI = "UPI00015AC919";
    private final String MODEL = "PF04041";
    private final SignatureLibrary signatureLibrary = SignatureLibrary.PFAM;
    private final String dbVersion = "24.0";
    private final String hmmBounds = "[]";
    private final String alignment = "";
    private final int start = 15, hmmStart = 18, hmmEnd = 320;
    private final int end = 345;
    private final double evalue = 0.00005, score = 512.3, locationEvalue = -5.60205984115601, locationScore = 779.4;
    //private static Logger LOGGER = Logger.getLogger(PfamHmmer3RawMatchDAOTest.class.getName());

    //private static final String[] ACCESSIONS = {"Q12345","P99999", "IPI01234567", "ENSP120923423423234"};

    private static final Long LONG_ZERO = 0L;

    private static final Long LONG_ONE = 1L;

    // First line of UPI0000000001.fasta
    public static final String GOOD = "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    @Resource(name = "pfamDAO")
    private PfamHmmer3RawMatchDAO dao;

    public void setDao(PfamHmmer3RawMatchDAO dao) {
        this.dao = dao;
    }

    @BeforeEach
    @AfterEach
    public void emptyPfamTable() {
        dao.deleteAll();
        assertEquals(LONG_ZERO, dao.count(),"There should be no pfam entries in the Pfam table following a call to dao.deleteAll");
    }

    /**
     * This test exercises
     * GenericDAOImpl<Protein, Long>
     * .insert(Protein protein)
     * .count()
     * .read(Long id)
     * .retrieveAll()
     * .delete(Protein protein)
     */
    @Test
    public void storeAndRetrieveProtein() {
        emptyPfamTable();
        PfamHmmer3RawMatch p = new PfamHmmer3RawMatch(UPI, MODEL, signatureLibrary, dbVersion, start, end,
                3.7E-9, 0.035, 1, 104, "[]", 3.0, 0, 0, 0, 0, 0, 0, 0);
        assertNotNull(dao, "The PfamDAOImpl object should be not-null.");
        dao.insert(p);
        Long id = p.getId();
        assertEquals(LONG_ONE, dao.count(), "The count of pfams in the database is not correct.");
        PfamHmmer3RawMatch retrievedPfam = dao.read(id);
        assertEquals(p.getModelId(), retrievedPfam.getModelId(), "The pfam methodAc of the retrieved object is not the same as the original object.");
        dao.delete(retrievedPfam);
        List<PfamHmmer3RawMatch> retrievedPfamList = dao.retrieveAll();
        assertEquals(0, retrievedPfamList.size(), "There should be no pfams in the database, following removal of the single pfam that was added.");
        assertEquals(LONG_ZERO, dao.count(), "The count of pfams in the database is not correct.");
        emptyPfamTable();
    }


    /**
     * Test that a PersistenceException is thrown if an attempt is made to
     * insert the same Protein twice.
     */
    @Test //(expected = PersistenceException.class)
    public void testPersistenceExceptionOnSecondInsert() {
        emptyPfamTable();
        PfamHmmer3RawMatch p = getMatchExample(null);
        assertThrows(PersistenceException.class, () -> {
            dao.insert(p);
            dao.insert(p);
        });

    }

    /**
     * Tests the GenericDAO.getMaximumPrimaryKey() method.
     */
    @Test
    public void testMaximumPrimaryKeyCount() {
        emptyPfamTable();
        Long maxPrimaryKey = 0l;
        for (int counter = 0; counter < 10; counter++) {
            PfamHmmer3RawMatch p = getMatchExample(null);
            dao.insert(p);
            if (p.getId() > maxPrimaryKey) {
                maxPrimaryKey = p.getId();
            }
        }
        assertEquals(maxPrimaryKey, dao.getMaximumPrimaryKey(), "The maximum primary key is not as expected");
    }

    private PfamHmmer3RawMatch getMatchExample(String proteinId) {
        if (proteinId == null) {
            proteinId = "UPIblachabla";
        }

        return new PfamHmmer3RawMatch(proteinId, "PF04041", SignatureLibrary.PFAM, dbVersion, (int) (Math.random() * 20), (int) (Math.random() * 100 + 20),
                3.7E-9, 0.035, 1, 104, "[]", 3.0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Test of the getMatchesForProteinIdsInRange() method.
     */
    @Test
    public void testGetMatchesForProteinIdsInRange() {
        emptyPfamTable();
        String[] proteinIds = {"001", "002", "003", "003", "004", "005", "006", "007", "008"};
        for (String proteinId : proteinIds) {
            PfamHmmer3RawMatch p = getMatchExample(proteinId);
            dao.insert(p);
        }
        Map<String, RawProtein<PfamHmmer3RawMatch>> matches = dao.getRawMatchesForProteinIdsInRange(3, 6, dbVersion);
        int matchCount = 0;
        for (RawProtein rawProtein : matches.values()) {
            matchCount += rawProtein.getMatches().size();
        }

        assertEquals(5, matchCount);
    }


    /**
     * Tests the ProteinDAO.getProteinsInTransactionSlice() method.

     @Test public void testGetProteinsInTransactionSlice(){
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
