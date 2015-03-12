//package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;
//
//import junit.framework.TestCase;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;
//import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfBlastResultParser;
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author Matthew Fraser
// * @author Maxim Scheremetjew
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
//public class BlastProgramTest extends TestCase {
//
//    @javax.annotation.Resource
//    private BinaryRunner binRunner;
//
//    @javax.annotation.Resource(name = "inputFile")
//    private Resource inputFileResource;
//
//    @javax.annotation.Resource(name = "blastDbResource")
//    private Resource blastDbResource;
//
//    @Before
//    public void init() {
//        assertNotNull(binRunner);
//    }
//
//    @Test
//    public void testBlastProgram() {
//        //TODO Finish this test!
//
//        String filePath = System.getProperty("user.dir") + "/business/bin/";
//        File f = new File(filePath);
//        if (!f.exists()) {
//            filePath = System.getProperty("user.dir") + "/bin/";
//        }
//        f = new File(filePath);
//        assertTrue("Path to BLAST binary file (" + filePath + ") does not exist!", f.exists());
//        binRunner.setBinaryPath(new FileSystemResource(f.getPath()));
//
//        File inputFile = null;
//        File blastDBFile = null;
//        try {
//            inputFile = inputFileResource.getFile();
//            blastDBFile = blastDbResource.getFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        StringBuffer additionalArgs = null;
//        InputStream is = null;
//        if (inputFile != null) {
//            additionalArgs = new StringBuffer();
//            additionalArgs.append("-i " + inputFile.getAbsolutePath());
//            additionalArgs.append(" -d " + blastDBFile.getAbsolutePath());
////            additionalArgs.append(" -o /tmp/blast/test.out");
//
//            try {
//                is = binRunner.run(additionalArgs.toString());
//                Thread.sleep(1000L);
//                Map<String, Integer> result = PirsfBlastResultParser.parseBlastStandardOutput(is);
//                assertNotNull("Expected a non empty list!", result);
//                assertEquals("Expected a list of size 1!", 1, result.size());
//                assertTrue("Expected key SF000729 in the map!", result.containsKey("SF000729"));
//                assertNotNull("Expected a non null value!", result.get("SF000729"));
//                assertEquals("Expected value 1!", new Integer(1).intValue(), result.get("SF000729").intValue());
//            } catch (IOException e) {
//                throw new IllegalStateException(e);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (is != null)
//                        is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//}
