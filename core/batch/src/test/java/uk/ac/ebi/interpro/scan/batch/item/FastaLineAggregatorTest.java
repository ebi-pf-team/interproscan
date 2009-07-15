package uk.ac.ebi.interpro.scan.batch.item;

import static org.junit.Assert.assertEquals;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.junit.runner.RunWith;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.Protein;

/**
 * Tests aggregation of FASTA records from {@link Protein} instances using {@link FormatterLineAggregator}.
 *
 * @author  Antony Quinn
 * @version $Id: FastaLineAggregatorTest.java,v 1.1 2009/06/18 10:53:08 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public final class FastaLineAggregatorTest {

    @Autowired
    private FormatterLineAggregator<Protein> aggregator;

    @Test public final void testAggregate() throws Exception {
        String sequence = "VAVFFGGLSIKKDEEVLKKNCPHIVVGTPGRILALARNKSLNLKHIKHFILDECDKMLEQLDMRRDVQEIFRMTPHEKQV";
        String md5      = "8ba3773a075eb0a3b6ab956f75d462a3"; // echo -n '<sequence>' | md5sum
        String expected = ">" + md5 + "\n" + sequence;
        assertEquals(expected, aggregator.aggregate(new Protein(sequence)));
    }

}