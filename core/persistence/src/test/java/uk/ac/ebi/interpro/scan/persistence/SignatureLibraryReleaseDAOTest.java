package uk.ac.ebi.interpro.scan.persistence;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jun-2010
 * Time: 17:54:38
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SignatureLibraryReleaseDAOTest {

    private static final Long LONG_ZERO = 0L;

    @Resource(name = "sigLibReleaseDAO")
    private SignatureLibraryReleaseDAO dao;

//    public void setDao(SignatureLibraryReleaseDAO dao) {
//        this.dao = dao;
//    }

    @Before
    @After
    public void emptySignatureLibraryReleaseTable() {
        dao.deleteAll();
        Assert.assertEquals("There should be no Releases in the SignatureLibraryRelease table following a call to dao.deleteAll", LONG_ZERO, dao.count());
    }

    /**
     * The method being tested should return true if a specific version of
     * a member database release has already been persisted.
     */
    //TODO: Investigate GenericJDBCException and remove @Ignore label when fixed
    @Ignore
    @Test
    public void testIsReleaseAlreadyPersisted() {

        final SignatureLibrary testLibrary = SignatureLibrary.PRINTS;
        final String testVersion = "1.0";
        //TODO: Why calling it explicitly, if we use @Before and @After annotation
        //emptySignatureLibraryReleaseTable();
        assertFalse(dao.isReleaseAlreadyPersisted(testLibrary, testVersion));
        SignatureLibraryRelease sigLib1 = new SignatureLibraryRelease(testLibrary, testVersion, createSignature());
        dao.insert(sigLib1);
        assertTrue(dao.isReleaseAlreadyPersisted(testLibrary, testVersion));
    }

    private Set<Signature> createSignature() {
        Model model = new Model("Model");
        Signature signature = new Signature("Signature");
        signature.addModel(model);
        return Collections.singleton(signature);
    }

}
