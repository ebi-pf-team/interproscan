//package uk.ac.ebi.interpro.scan.io.sequence;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.api.Test;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.ContextConfiguration;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.*;
//
//import uk.ac.ebi.interpro.scan.model.Protein;
//
///**
// * Tests {@link FastaSequenceWriter}.
// *
// * @author  Antony Quinn
// * @version $Id$
// */
//@ExtendWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
//public class FastaSequenceWriterTest {
//
//    private static final String SHORT_SEQUENCE =
//                               "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";
//
//    private static final String MEDIUM_SEQUENCE  =
//                               "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD" +
//                               "ADAQLDAVLSAATETYSGLTPEQKAYVPAMFTAALNIQTSVNTVVRDFENYVKQTCNSSA";
//
//    private static final String LONG_SEQUENCE  =
//                               "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD" +
//                               "ADAQLDAVLSAATETYSGLTPEQKAYVPAMFTAALNIQTSVNTVVRDFENYVKQTCNSSA" +
//                               "VVDNKLKIQNVIIDECYGAPGSPTNLEFINTGSSKGNCAIKALMQLTTKATTQIAPKQVA" +
//                               "GTGVQFYMIVIGVIILAALFMYYAKRMLFTSTNDKIKLILANKENVHWTTYMDTFFRTSP" +
//                               "MVIATTDMQN";
//
//    private static final List<String> EXPECTED_SEQUENCES =
//            Arrays.asList(SHORT_SEQUENCE, MEDIUM_SEQUENCE, LONG_SEQUENCE);
//
//    @Resource
//    private org.springframework.core.io.Resource file;
//
//    @Resource
//    private org.springframework.core.io.Resource defaultFile;
//
//    @Resource
//    private org.springframework.core.io.Resource badPath;
//
//    @Resource
//    private org.springframework.core.io.Resource dummyFile;
//
//    @Resource
//    private SequenceWriter writer;
//
//    @Resource
//    private SequenceWriter defaultWriter;
//
//    @Test
//    public void write() throws IOException {
//        write(getProteins(), writer, file);
//    }
//
//    @Test(expected = IOException.class)
//    public void writeBadFile() throws IOException {
//        write(getProteins(), writer, badPath);
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void writeBadKey() throws IOException {
//        // Protein has not been persisted so ID field is null (ID = database primary key)
//        write(getProteins(), new FastaSequenceWriter(false), dummyFile);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void writeBadWidth() throws IOException {
//        // Width should be greater than 0
//        write(getProteins(), new FastaSequenceWriter(0), dummyFile);
//    }
//
//    private void write(Collection<Protein> proteins,
//                       SequenceWriter writer,
//                       org.springframework.core.io.Resource file) throws IOException {
//        // Write
//        writer.write(proteins, file);
//        // Read
//        final List<String> actualSequences = new ArrayList<String>();
//        SequenceReader reader = new FastaSequenceReader(
//                new SequenceReader.Listener() {
//                    @Override public void mapRecord(SequenceRecord record) {
//                        actualSequences.add(record.getSequence());
//                    }
//                }
//        );
//        reader.read(file);
//        // Check
//        assertEquals("Sequences should be equal", EXPECTED_SEQUENCES, actualSequences);
//    }
//
//    private Collection<Protein> getProteins() {
//        Set<Protein> proteins = new LinkedHashSet<Protein>();
//        for (String sequence : EXPECTED_SEQUENCES)    {
//            proteins.add(new Protein(sequence));
//        }
//        return proteins;
//    }
//
//}