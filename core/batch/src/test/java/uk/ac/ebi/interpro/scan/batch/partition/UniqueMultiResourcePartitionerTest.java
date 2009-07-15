package uk.ac.ebi.interpro.scan.batch.partition;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.item.ExecutionContext;
import org.junit.runner.RunWith;
import org.junit.Test;

import java.util.Map;
import java.io.File;

/**
 * Tests {@link UniqueMultiResourcePartitioner}.
 *
 * @author  Antony Quinn
 * @version $Id: UniqueMultiResourcePartitionerTest.java,v 1.2 2009/06/19 09:20:33 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public final class UniqueMultiResourcePartitionerTest {

    @Autowired
    private UniqueMultiResourcePartitioner partitioner;

    @Test public final void testPartition() throws Exception {
        Map<String, ExecutionContext> map = partitioner.partition(0);
        for (ExecutionContext context : map.values())   {
            assertNotNull(context.getString(UniqueMultiResourcePartitioner.FILE_NAME_KEY));
            assertNotNull(context.getString(UniqueMultiResourcePartitioner.FILE_ID_KEY));
            assertNotNull(context.getString(UniqueMultiResourcePartitioner.UUID_KEY));
        }
    }

}