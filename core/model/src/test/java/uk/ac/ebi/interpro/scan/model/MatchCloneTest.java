package uk.ac.ebi.interpro.scan.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 *
 * @author Phil Jones
 * @author Gift Nuka
 */
public class MatchCloneTest {

    @Test
    public void testMatchCloning() {
        final int matchCount = 3;
        final SignatureLibraryRelease release = new SignatureLibraryRelease(
                SignatureLibrary.PHOBIUS,
                "1");

        for (final PhobiusFeatureType type : PhobiusFeatureType.values()) {
            final Signature.Builder builder = new Signature.Builder(type.getAccession());
            final Signature signature = builder
                    .name(type.getName())
                    .description(type.getDescription())
                    .signatureLibraryRelease(release)
                    .build();
            release.addSignature(signature);
        }

        final Signature coilsSig = new Signature("Coils");
        final Signature pfamSig = new Signature("PF0001");

        final ProteinXref px1 = new ProteinXref("px1");
        final Set<Match> matches = new HashSet<Match>();

        final PhobiusMatch.PhobiusLocation phob1Location = new PhobiusMatch.PhobiusLocation(20, 40);
        final Signature phobiusSig = release.getSignatures().iterator().next();
        final Match phob1Match = new PhobiusMatch(phobiusSig, "MOD001", Collections.singleton(phob1Location));
        matches.add(phob1Match);

        final CoilsMatch.CoilsLocation coilsLocation = new CoilsMatch.CoilsLocation(30, 50);
        final Match coilsMatch = new CoilsMatch(coilsSig, "MOD001", Collections.singleton(coilsLocation));
        matches.add(coilsMatch);

        final Hmmer3Match.Hmmer3Location pfamLocation = new Hmmer3Match.Hmmer3Location(10, 20, 12.2, 2.2, 15, 30, 15, HmmBounds.COMPLETE, 10, 10, false, DCStatus.CONTINUOUS);
        final Hmmer3Match pfamMatch = new Hmmer3Match(pfamSig, "MOD001", 101.0d, 10d, Collections.singleton(pfamLocation));
        matches.add(pfamMatch);

        final Protein p1 = new Protein("ATSASRDXTASFXRATCRFDTSCFSTDCDSCFRTSDCDSC", matches, Collections.singleton(px1));
        int count = 0;

        count = 0;
        for (Match match : p1.getMatches()) {
            assertEquals(p1, match.getProtein());
            count++;
        }

        assertEquals(matchCount, count);
        // Clone the matches
        final Set<Match> clonedMatches = new HashSet<Match>(p1.getMatches().size());
        for (Match match : p1.getMatches()) {
            try {
                final Match clonedMatch = (Match) match.clone();
                assertFalse(match == clonedMatch);
                clonedMatches.add(clonedMatch);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }


        final Protein p2 = new Protein("REASDXRASDXRSAE", clonedMatches, Collections.singleton(px1));


        assertNotNull(p1.getMatches());
        assertNotNull(p2.getMatches());

        assertTrue(!p1.equals(p2));
        count = 0;

        boolean foundPhobius = false;
        boolean foundPfam = false;
        boolean foundCoils = false;

        for (Match match : p2.getMatches()) {
            assertEquals(p2, match.getProtein());
            count++;
            if (match instanceof PhobiusMatch) {
                foundPhobius = true;
                assertFalse(phob1Match == match);
                assertFalse(phob1Match.getLocations().iterator().next() == ((PhobiusMatch) match).getLocations().iterator().next());
                assertTrue(phob1Match.getSignature() == match.getSignature());
                assertTrue(phob1Match.getSignatureModels() == match.getSignatureModels());
                assertTrue(phob1Match.getLocations().size() == ((PhobiusMatch) match).getLocations().size());
                PhobiusMatch.PhobiusLocation clonedLocation = ((PhobiusMatch) match).getLocations().iterator().next();
                assertFalse(phob1Location == clonedLocation);
                assertTrue(phob1Location.getStart() == clonedLocation.getStart());
                assertTrue(phob1Location.getEnd() == clonedLocation.getEnd());
            } else if (match instanceof Hmmer3Match) {
                foundPfam = true;
                assertFalse(pfamMatch == match);
                Hmmer3Match clonedMatch = (Hmmer3Match) match;
                assertFalse(pfamMatch.getLocations().iterator().next() == clonedMatch.getLocations().iterator().next());
                assertTrue(pfamMatch.getSignature() == clonedMatch.getSignature());
                assertTrue(pfamMatch.getSignatureModels() == clonedMatch.getSignatureModels());
                assertTrue(pfamMatch.getEvalue() == clonedMatch.getEvalue());
                assertTrue(pfamMatch.getScore() == clonedMatch.getScore());
                assertTrue(pfamMatch.getLocations().size() == clonedMatch.getLocations().size());
                Hmmer3Match.Hmmer3Location clonedLocation = clonedMatch.getLocations().iterator().next();
                assertFalse(pfamLocation == clonedLocation);
                assertTrue(pfamLocation.getStart() == clonedLocation.getStart());
                assertTrue(pfamLocation.getEnd() == clonedLocation.getEnd());
                assertTrue(pfamLocation.getEnvelopeStart() == clonedLocation.getEnvelopeStart());
                assertTrue(pfamLocation.getEnvelopeEnd() == clonedLocation.getEnvelopeEnd());
                assertEquals(pfamLocation.getHmmBounds(), clonedLocation.getHmmBounds());
                assertTrue(pfamLocation.getEvalue() == clonedLocation.getEvalue());
                assertTrue(pfamLocation.getHmmStart() == clonedLocation.getHmmStart());
                assertTrue(pfamLocation.getHmmEnd() == clonedLocation.getHmmEnd());
                assertTrue(pfamLocation.getHmmLength() == clonedLocation.getHmmLength());
            } else if (match instanceof CoilsMatch) {
                foundCoils = true;
                assertFalse(coilsMatch == match);
                assertFalse(coilsMatch.getLocations().iterator().next() == ((CoilsMatch) match).getLocations().iterator().next());
                assertTrue(coilsMatch.getSignature() == match.getSignature());
                assertTrue(coilsMatch.getSignatureModels() == match.getSignatureModels());
                assertTrue(coilsMatch.getLocations().size() == ((CoilsMatch) match).getLocations().size());
                CoilsMatch.CoilsLocation clonedLocation = ((CoilsMatch) match).getLocations().iterator().next();
                assertFalse(coilsLocation == clonedLocation);
                assertTrue(coilsLocation.getStart() == clonedLocation.getStart());
                assertTrue(coilsLocation.getEnd() == clonedLocation.getEnd());
            }
        }

        assertTrue(foundPfam);
        assertTrue(foundPhobius);
        assertTrue(foundCoils);

        assertEquals(matchCount, count);

        count = 0;
        for (Match match : p1.getMatches()) {
            assertEquals(p1, match.getProtein());
            count++;
        }

        assertEquals(matchCount, count);


    }
}
