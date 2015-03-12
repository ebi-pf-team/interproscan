package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.business.filter.Gene3dRawMatchFilter;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for {@link Gene3dHmmer3FilterStep}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public final class Gene3dHmmer3FilterStepTest {

    private static final String MODEL_ID    = "2hxsA00";
    private static final String SEQ_ID      = "HP0834";
    private static final double SEQ_EVALUE  = 2.6e-21;
    private static final double SEQ_SCORE   = 79.0;
    private static final double SEQ_BIAS    = 0.1;
    private static final double DOMAIN_SCORE = 41.5;
    private static final double DOMAIN_BIAS = 0.0;
    private static final double DOMAIN_C_EVALUE = 1.7e-11;
    private static final double DOMAIN_I_EVALUE = 8.6e-10;
    private static final int MODEL_START    = 8;
    private static final int MODEL_END      = 171;
    private static final String HMM_BOUNDS  = "..";
    private static final int SEQ_START      = 10;
    private static final int SEQ_END        = 167;
    private static final int ENV_START      = 3;
    private static final int ENV_END        = 171;
    private static final double EXPECTED_ACCURACY = 0.83;
    private static final String ALIGNMENT   = "24M2I9M1D9";

    private static final Gene3dHmmer3RawMatch match =
              new Gene3dHmmer3RawMatch(SEQ_ID, MODEL_ID, "3.3.0",
                                       SEQ_START, SEQ_END, SEQ_EVALUE, SEQ_SCORE,
                                       MODEL_START, MODEL_END, HMM_BOUNDS, DOMAIN_SCORE,
                                       ENV_START, ENV_END, EXPECTED_ACCURACY, SEQ_BIAS,
                                       DOMAIN_C_EVALUE, DOMAIN_I_EVALUE, DOMAIN_BIAS, ALIGNMENT);
    
//    @Resource
//    Gene3dHmmer3FilterStep step;

    @Resource
    Gene3dRawMatchFilter filter;

    @Test
    @Ignore("Relies on DomainFinder binary")
    public void testFilter() {
        RawProtein<Gene3dHmmer3RawMatch> p = new RawProtein<Gene3dHmmer3RawMatch>(SEQ_ID);
        p.addMatch(match);
        Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins = new HashSet<RawProtein<Gene3dHmmer3RawMatch>>();
        rawProteins.add(p);
        Gene3dHmmer3FilterStep step = new Gene3dHmmer3FilterStep();
        step.setFilter(filter);
        Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = step.getFilter().filter(rawProteins);
    }

}
