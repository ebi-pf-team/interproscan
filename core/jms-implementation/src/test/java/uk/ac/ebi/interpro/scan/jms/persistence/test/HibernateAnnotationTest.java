package uk.ac.ebi.interpro.scan.jms.persistence.test;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.persistence.SignatureDAO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * Simple test class to run persistence performance tests on several DAOs. Decided to put it here because all
 * dependencies, like e.g. H2 database, are already in place in this module.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore
public class HibernateAnnotationTest {

    private final Logger log = Logger.getLogger(HibernateAnnotationTest.class.getName());

    @Resource
    private SignatureDAO signatureDAO;

    @Test
    public void performanceTest() {
        List<String> signatureAc = new ArrayList<String>();
        signatureAc.add("PF00003");
        signatureAc.add("G3DSA:1.10.10.10");

        long lnSystemTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Set<Signature> signatures = signatureDAO.getSignatures(signatureAc);
        }
        lnSystemTime = (System.currentTimeMillis() - lnSystemTime) / 1000;
        log.info("Tooks " + lnSystemTime + " seconds.");
    }
}
