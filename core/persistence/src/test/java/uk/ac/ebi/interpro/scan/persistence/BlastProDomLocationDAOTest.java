package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Resource;
import javax.persistence.PersistenceException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.XrefSequenceIdentifier;
import uk.ac.ebi.interpro.scan.model.BlastProDomLocation;
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.model.transactiontracking.RawTransactionSliceImpl;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 21-Jul-2009
 * Time: 15:32:43
 * To change this template use File | Settings | File Templates.
 */
public class BlastProDomLocationDAOTest {

      /**
     * Logger for Junit logging. Log messages will be associated with the ProteinPersistenceTest class.
     */
    private static Logger LOGGER = Logger.getLogger(BlastProDomLocationDAOTest.class);

    private static final Long LONG_ZERO = 0L;

    private static final Long LONG_ONE = 1L;

    // First line of UPI0000000001.fasta
   // public static final String GOOD       = "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    @Resource(name= "blastProdomLocationDAO")
    private BlastProDomLocationDAO dao;

    public void setDao(BlastProDomLocationDAO dao) {
        this.dao = dao;
    }

    @Before
    @After
    public void emptyBPLocationTable(){
        dao.deleteAll();
        assertEquals("There should be no BPLocations in the BPL table following a call to dao.deleteAll", LONG_ZERO, dao.count());
    }

    /**
     * This test exercises
     * GenericDAOImpl<BlastProDomLocation, Long>
     *                              .insert(BlastProDomLocation bpl)
     *                              .count()
     *                              .read(Long id)
     *                              .retrieveAll()
     *                              .delete(BlastProDomLocation bpl)
     */
    @Test
    public void storeAndRetrieveBlastProDomLocation(){
        emptyBPLocationTable();
        BlastProDomLocation b = new BlastProDomLocation (23, 89,0.000009);
        assertNotNull("The BPLDAOImpl object should be not-null.", dao);
        dao.insert(b);
        Long id = b.getId();
        assertEquals("The count of BPLocns in the database is not correct.", LONG_ONE, dao.count());
        BlastProDomLocation bp = dao.read(id);
        assertEquals("The BPLocation details of the retrieved object is not the same as the original object.", b.getStart(), bp.getStart());
        dao.delete(bp);
        List<BlastProDomLocation> retrievedBPLList = dao.retrieveAll();
        assertEquals("There should be no BPL in the database, following removal of the single BPL that was added.", 0, retrievedBPLList.size());
        assertEquals("The count of BPLs in the database is not correct.", LONG_ZERO, dao.count());
        emptyBPLocationTable();
    }

}
