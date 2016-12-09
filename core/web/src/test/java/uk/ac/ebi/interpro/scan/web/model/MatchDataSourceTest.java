package uk.ac.ebi.interpro.scan.web.model;

import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Check we can get the relevant {@link MatchDataSource} from the supplied {@link SignatureLibrary} name.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MatchDataSourceTest {

    @Test
    public void testPfam() {
        final String name = SignatureLibrary.PFAM.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.PFAM, m);
    }

    @Test
    public void testPrositeProfiles() {
        final String name = SignatureLibrary.PROSITE_PROFILES.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.PROSITE_PROFILES, m);
    }

    @Test
    public void testSignalPEuk() {
        final String name = SignatureLibrary.SIGNALP_EUK.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.SIGNALP_EUK, m);
    }

    @Test
    public void testSignalPGramPos() {
        final String name = SignatureLibrary.SIGNALP_GRAM_POSITIVE.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.SIGNALP_GRAM_POSITIVE, m);
    }

    @Test
    public void testSignalPGramNeg() {
        final String name = SignatureLibrary.SIGNALP_GRAM_NEGATIVE.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.SIGNALP_GRAM_NEGATIVE, m);
    }

    @Test
    public void testCDD() {
        final String name = SignatureLibrary.CDD.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.CDD, m);
    }

    @Test
    public void testSFLD() {
        final String name = SignatureLibrary.SFLD.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.SFLD, m);
        String linkUrl = m.getLinkUrl("SFLDG01135");
        assertTrue(linkUrl.endsWith("/subgroup/01135"));
        linkUrl = m.getLinkUrl("SFLDS00003");
        assertTrue(linkUrl.endsWith("/superfamily/00003"));
        linkUrl = m.getLinkUrl("SFLDF00288");
        assertTrue(linkUrl.endsWith("/family/00288"));
    }

    @Test
    public void testMobiDB() {
        final String name = SignatureLibrary.MOBIDB_LITE.getName();
        final MatchDataSource m = MatchDataSource.parseName(name);
        assertEquals(MatchDataSource.MOBIDB_LITE, m);
    }


}
