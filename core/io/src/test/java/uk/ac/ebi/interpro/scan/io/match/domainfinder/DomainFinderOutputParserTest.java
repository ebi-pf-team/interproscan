package uk.ac.ebi.interpro.scan.io.match.domainfinder;




import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;


import javax.annotation.Resource;

import java.io.IOException;
//import java.io.File;





/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 22-Jan-2010
 * Time: 13:23:08
 * To change this template use File | Settings | File Templates.
 * output file from DF3 is of format
 *   HP0834	2hxsA00	1000	178	195	362	5	169	6.500000000000001E-8	35.4	35.4	1	195:362
 *   NT01CJ0385	2hxsA00	1000	178	193	362	4	169	7.300000000000015E-9	38.5	38.5	1	193:362
 *   This DF3OPparser class goes through this file and stores the components of each line into post-processed
 *   Gene3D match.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DomainFinderOutputParserTest {
     //private static final Logger LOGGER = Logger.getLogger(DomainFinderOutputParserTest.class);
     @Resource private org.springframework.core.io.Resource domainFinderOutputFile;
    @Test
    public void testDF3OutputWriter() throws IOException {
        DomainFinderOutputParser dmop = new DomainFinderOutputParser(domainFinderOutputFile.getFile());
    }

    
}
