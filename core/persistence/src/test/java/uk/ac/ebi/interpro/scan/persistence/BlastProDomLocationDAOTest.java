package uk.ac.ebi.interpro.scan.persistence;

import static junit.framework.Assert.*;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.BlastProDomLocation;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 21-Jul-2009
 * Time: 15:32:43
 * To change this template use File | Settings | File Templates.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations={"/springconfig/spring-BlastProDomLocationDAOTest-config.xml"} )

public class BlastProDomLocationDAOTest {

      /**
     * Logger for Junit logging. Log messages will be associated with the ProteinPersistenceTest class.
     */
    private static Logger LOGGER = Logger.getLogger(BlastProDomLocationDAOTest.class);

    private static final Long LONG_ZERO = 0L;

    private static final Long LONG_ONE = 1L;

    private static final Long LONG_TWO = 2L;

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
        assertTrue("object is not null",dao!=null);
        dao.deleteAll();
        assertEquals(LONG_ZERO, dao.count());
        
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
    @Ignore
    ("Test works in general, but persistence fails due to constraint violation")
    public void storeAndRetrieveBlastProDomLocation(){
        //emptyBPLocationTable();
        BlastProDomLocation b = new BlastProDomLocation (23, 89,0.01);
        assertNotNull("The BPLDAOImpl object should be not-null.", b);
        dao.insert(b);
        Long id = b.getId();
        assertEquals("The count of BPLocns in the database is not correct.", LONG_ONE, dao.count());
        b =   new BlastProDomLocation (23, 89,0.02);
        assertNotNull("The BPLDAOImpl object should be not-null.", b);
        dao.insert(b);
        id = b.getId();
        assertEquals("The count of BPLocns in the database is not correct.", LONG_ONE, dao.count());
        BlastProDomLocation bp = dao.read(id);
        assertEquals("The BPLocation details of the retrieved object is not the same as the original object.", b.getStart(), bp.getStart());
        //retrieve locations with score equal to or more than 0.01
        List<BlastProDomLocation>  retrievedBPLList = dao.getBlastProDomHitLocationByScore(0.01);
        assertEquals( 2, retrievedBPLList.size());

        dao.delete(bp);
        retrievedBPLList = dao.retrieveAll();
        assertEquals("There should be no BPL in the database, following removal of the single BPL that was added.", 1, retrievedBPLList.size());
        assertEquals("The count of BPLs in the database is not correct.", LONG_ONE, dao.count());
        emptyBPLocationTable();
    }

}
