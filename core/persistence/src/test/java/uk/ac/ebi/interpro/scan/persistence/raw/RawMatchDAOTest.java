package uk.ac.ebi.interpro.scan.persistence.raw;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link RawMatchDAO}.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RawMatchDAOTest {

    private static final Long SEQ_ID = 1l;
    private static final String MODEL_ID = "2hxsA00";
    private static final String HIT_MODEL_NAME = "2hxsA00-i1";
    private static final String CATH_FAMILY_ID = "3.30.300.20";
    private static final double SEQ_EVALUE = 2.6e-21;
    private static final double SEQ_SCORE = 79.0;
    private static final double SEQ_BIAS = 0.1;
    private static final double DOMAIN_SCORE = 41.5;
    private static final double DOMAIN_BIAS = 0.0;
    private static final double DOMAIN_C_EVALUE = 1.7e-11;
    private static final double DOMAIN_I_EVALUE = 8.6e-10;
    private static final int MODEL_START = 8;
    private static final int MODEL_END = 171;
    private static final String HMM_BOUNDS = "..";
    private static final int SEQ_START = 10;
    private static final int SEQ_END = 167;
    private static final int ENV_START = 3;
    private static final int ENV_END = 171;
    private static final double EXPECTED_ACCURACY = 0.83;
    private static final String ALIGNMENT = "24M2I9M1D9";
    private static final String DB_RELEASE = "4.2.0";
    private static final String REGION = "";

    @Resource
    RawMatchDAO<Gene3dHmmer3RawMatch> dao;

    @Before
    @After
    public void deleteAll() {
        dao.deleteAll();
        assertEquals("Could not delete all proteins", Long.valueOf(0), dao.count());
    }

    @Test
    public void insertProteinMatches() {
        deleteAll();
        dao.insertProteinMatches(createGene3dMatches());
        assertEquals(1, dao.retrieveAll().size());
    }

    @Test
    public void getProteinsByIdRange() {
        deleteAll();
        dao.insertProteinMatches(createGene3dMatches());
        Set<RawProtein<Gene3dHmmer3RawMatch>> proteins =
                dao.getProteinsByIdRange(SEQ_ID, SEQ_ID, DB_RELEASE);
        assertEquals(1, proteins.size());
    }

    private Set<RawProtein<Gene3dHmmer3RawMatch>> createGene3dMatches() {
        Set<RawProtein<Gene3dHmmer3RawMatch>> proteins = new HashSet<RawProtein<Gene3dHmmer3RawMatch>>();
        RawProtein<Gene3dHmmer3RawMatch> p = new RawProtein<Gene3dHmmer3RawMatch>(SEQ_ID.toString());
        Gene3dHmmer3RawMatch match =
                new Gene3dHmmer3RawMatch(SEQ_ID.toString(), MODEL_ID, CATH_FAMILY_ID, HIT_MODEL_NAME, DB_RELEASE,
                        SEQ_START, SEQ_END, SEQ_EVALUE, SEQ_SCORE,
                        MODEL_START, MODEL_END, HMM_BOUNDS, DOMAIN_SCORE,
                        ENV_START, ENV_END, EXPECTED_ACCURACY, SEQ_BIAS,
                        DOMAIN_C_EVALUE, DOMAIN_I_EVALUE, DOMAIN_BIAS, ALIGNMENT, REGION);
        p.addMatch(match);
        proteins.add(p);
        return proteins;
    }

}
