package uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Set;

import uk.ac.ebi.interpro.scan.parser.ParseException;
import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore ("Currently need to have an entire hmm library in the classpath, so this needs to be switched on manually.  Note that the location / name of the hmm linbrary and the hmm results file should be set in the test context.xml file src/test/resources/uk/ac/ebi/interpro/scan/parser/matchparser/hmmer3/Hmmer3SearchParserTest-context.xml")
public class Hmmer3SearchParserTest {

    private static Logger LOGGER = Logger.getLogger(Hmmer3SearchParserTest.class);

    @Resource
    private Hmmer3SearchParser parser;

    @Resource
    private org.springframework.core.io.Resource testFile;



    @Test
    public void testParser(){
        InputStream is = null;
        try{
            is = new FileInputStream(testFile.getFile());
            Set<RawSequenceIdentifier> rawSequenceIdentifiers = parser.parse(is);
            for (RawSequenceIdentifier seqIdentifier : rawSequenceIdentifiers){
                System.out.println("Protein Accession: " + seqIdentifier.getSequenceIdentifier());
                for (RawMatch rawMatch : seqIdentifier.getMatches()){
                    if (rawMatch instanceof PfamHmmer3RawMatch){
                        PfamHmmer3RawMatch pfamMatch = (PfamHmmer3RawMatch) rawMatch;
                        System.out.println("\tpfamMatch.getModel() = " + pfamMatch.getModel());
                        System.out.println("\tstart = " + pfamMatch.getLocationStart());
                        System.out.println("\tend = " + pfamMatch.getLocationEnd());
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
