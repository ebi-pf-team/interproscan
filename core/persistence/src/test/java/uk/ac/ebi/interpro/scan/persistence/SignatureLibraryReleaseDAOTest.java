package uk.ac.ebi.interpro.scan.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Set;


/**
 * Created by IntelliJ IDEA.
 * @author P Jones
 * @author Gift Nuka
 * Date: 10-Jun-2010
 * Time: 17:54:38
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class SignatureLibraryReleaseDAOTest {

    private static final Long LONG_ZERO = 0L;

    @Resource(name = "sigLibReleaseDAO")
    private SignatureLibraryReleaseDAO dao;

//    public void setDao(SignatureLibraryReleaseDAO dao) {
//        this.dao = dao;
//    }

    @BeforeEach
    @AfterEach
    public void emptySignatureLibraryReleaseTable() {
        dao.deleteAll();
        assertEquals(LONG_ZERO, dao.count(), "There should be no Releases in the SignatureLibraryRelease table following a call to dao.deleteAll");
    }

    /**
     * The method being tested should return true if a specific version of
     * a member database release has already been persisted.
     */
    //TODO: Investigate GenericJDBCException and remove @Disabled label when fixed
    @Disabled
    @Test
    public void testIsReleaseAlreadyPersisted() {

        final SignatureLibrary testLibrary = SignatureLibrary.PRINTS;
        final String testVersion = "1.0";
        //TODO: Why calling it explicitly, if we use @BeforeEach and @After annotation
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
