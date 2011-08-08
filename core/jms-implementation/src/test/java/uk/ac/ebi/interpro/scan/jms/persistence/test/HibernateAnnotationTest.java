package uk.ac.ebi.interpro.scan.jms.persistence.test;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.persistence.EntryDAO;
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

    @Resource
    private EntryDAO entryDAO;

    @Test
    public void selectPerformanceTest() {
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

    @Test
    public void updatePerformanceTest() {
        for (int i = 0; i < 100; i++) {
            Entry entry1 = new Entry.Builder("IPR011991").build();
            entryDAO.insert(entry1);
            //
            Signature signature = signatureDAO.getSignatureByAccession("PF00003");
            entry1.addSignature(signature);
            signatureDAO.update(signature);
            if (i % 10 == 0) {
                printMemory();
            }
        }
        printMemory();
    }

    private void printMemory() {
        if (log.isInfoEnabled()) {
            long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            log.info("Current memory usage: " + heap + " bytes (" + (heap / 131072 * 0.125) + " MB)");
        }
    }
}