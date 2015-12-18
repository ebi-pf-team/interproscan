package uk.ac.ebi.interpro.scan.management.model.implementations.smart;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.business.postprocessing.smart.SmartPostProcessing;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.HmmPfamParser;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.persistence.SignatureDAO;
import uk.ac.ebi.interpro.scan.persistence.SignatureLibraryReleaseDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.SmartHmmer2RawMatchDAO;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link HmmPfamParser<SmartRawMatch>} and {@link SmartPostProcessing}.
 *
 * @author Antony Quinn
 * @version $Id$
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SmartParseFilterTest {

    @Resource
    private HmmPfamParser<SmartRawMatch> parser;

    @Resource
    private ProteinDAO proteinDAO;

    @Resource
    private SignatureLibraryReleaseDAO signatureLibraryReleaseDAO;

    @Resource
    private SignatureDAO signatureDAO;

    @Resource
    private SmartHmmer2RawMatchDAO rawMatchDAO;

    @Resource
    private FilteredMatchDAO<SmartRawMatch, Hmmer2Match> filteredMatchDAO;

    @Resource
    private SmartPostProcessing postProcessor;

    @Resource
    private org.springframework.core.io.Resource fastaFile;

    @Resource
    private org.springframework.core.io.Resource hmmerOutput;

    @Resource
    private Long bottomProtein;

    @Resource
    private Long topProtein;

    @Resource
    private Long expectedProteins;

    @Resource
    private Long expectedRawMatches;

    @Resource
    private Long expectedFilteredMatches;

    @Test
    @Ignore("This test appears to be reproducing some testing already run in the business module.")
    public void testFilter() throws IOException {

        // Parse and store raw matches
        final Set<RawProtein<SmartRawMatch>> parsedResults = parser.parse(hmmerOutput.getInputStream());
        rawMatchDAO.insertProteinMatches(parsedResults);
        assertEquals(expectedRawMatches.longValue(), getMatchCount(parsedResults));


        // TODO: Why getRawMatchesForProteinIdsInRange() in specific Smart class and not the standard RawMatchDAO.getProteinsByIdRange() ?
        // Read raw matches
        Map<String, RawProtein<SmartRawMatch>> rawMatches =
                rawMatchDAO.getRawMatchesForProteinIdsInRange(bottomProtein, topProtein, parser.getSignatureLibraryRelease());
        assertEquals(expectedRawMatches.longValue(), getMatchCount(rawMatches.values()));

        // Filter raw matches
        Map<String, RawProtein<SmartRawMatch>> filteredMatches = postProcessor.process(rawMatches);
        assertNotNull(filteredMatches);
        assertEquals(expectedProteins.longValue(), filteredMatches.values().size());
        assertEquals(expectedFilteredMatches.longValue(), getMatchCount(filteredMatches.values()));

        // TODO: The remainder is only required to test persistence of filtered matches -- need to fix LazyInitializationError first:
        // org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease.signatures, no session or session was closed

        // Insert proteins
//        final Set<Protein> proteins = new HashSet<Protein>();
//        SequenceReader reader = new FastaSequenceReader(
//                new SequenceReader.Listener() {
//                    @Override public void mapRecord(SequenceRecord r) {
//                        Protein p =
//                                new Protein.Builder(r.getSequence()).crossReference(new ProteinXref(r.getId())).build();
//                        proteins.add(p);
//                    }
//                }
//        );
//        reader.read(fastaFile);
//        ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewNucleotideSequences(proteins);
//        assertEquals(expectedProteins.longValue(), persistedProteins.getNewSequences().size());

        // Insert signatures -- we're saving time and memory by only inserting the signatures we need
//        SignatureLibraryRelease release =
//                new SignatureLibraryRelease(SignatureLibrary.SMART, parser.getSignatureLibraryRelease());
//        for (RawProtein<SmartRawMatch> p : filteredMatches.values()) {
//            for (SmartRawMatch m : p.getMatches()) {
//                String id = m.getModelId();
//                Signature s = new Signature.Builder(id).model(new Model(id)).build();
//                release.addSignature(s);
//            }
//        }
//        signatureLibraryReleaseDAO.insert(release);
//        signatureDAO.insert(release.getSignatures());
//        filteredMatchDAO.persist(filteredMatches.values());

    }

    private long getMatchCount(Collection<RawProtein<SmartRawMatch>> proteins) {
        long count = 0;
        for (RawProtein<SmartRawMatch> p : proteins) {
            count += p.getMatches().size();
        }
        return count;
    }

}
