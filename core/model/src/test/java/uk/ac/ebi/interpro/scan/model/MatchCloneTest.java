package uk.ac.ebi.interpro.scan.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 *
 * @author Phil Jones
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
        final Match phob1Match = new PhobiusMatch(phobiusSig, Collections.singleton(phob1Location));
        matches.add(phob1Match);

        final CoilsMatch.CoilsLocation coilsLocation = new CoilsMatch.CoilsLocation(30, 50);
        final Match coilsMatch = new CoilsMatch(coilsSig, Collections.singleton(coilsLocation));
        matches.add(coilsMatch);

        final Hmmer3Match.Hmmer3Location pfamLocation = new Hmmer3Match.Hmmer3Location(10, 20, 12.2, 2.2, 15, 30, 15, HmmBounds.COMPLETE, 10, 10);
        final Hmmer3Match pfamMatch = new Hmmer3Match(pfamSig, 101.0d, 10d, Collections.singleton(pfamLocation));
        matches.add(pfamMatch);

        final Protein p1 = new Protein("ATSASRDXTASFXRATCRFDTSCFSTDCDSCFRTSDCDSC", matches, Collections.singleton(px1));
        int count = 0;

        count = 0;
        for (Match match : p1.getMatches()) {
            Assert.assertEquals(p1, match.getProtein());
            count++;
        }

        Assert.assertEquals(matchCount, count);
        // Clone the matches
        final Set<Match> clonedMatches = new HashSet<Match>(p1.getMatches().size());
        for (Match match : p1.getMatches()) {
            try {
                final Match clonedMatch = (Match) match.clone();
                Assert.assertFalse(match == clonedMatch);
                clonedMatches.add(clonedMatch);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }


        final Protein p2 = new Protein("REASDXRASDXRSAE", clonedMatches, Collections.singleton(px1));


        Assert.assertNotNull(p1.getMatches());
        Assert.assertNotNull(p2.getMatches());

        Assert.assertTrue(!p1.equals(p2));
        count = 0;

        boolean foundPhobius = false;
        boolean foundPfam = false;
        boolean foundCoils = false;

        for (Match match : p2.getMatches()) {
            Assert.assertEquals(p2, match.getProtein());
            count++;
            if (match instanceof PhobiusMatch) {
                foundPhobius = true;
                Assert.assertFalse(phob1Match == match);
                Assert.assertFalse(phob1Match.getLocations().iterator().next() == ((PhobiusMatch) match).getLocations().iterator().next());
                Assert.assertTrue(phob1Match.getSignature() == match.getSignature());
                Assert.assertTrue(phob1Match.getLocations().size() == ((PhobiusMatch) match).getLocations().size());
                PhobiusMatch.PhobiusLocation clonedLocation = ((PhobiusMatch) match).getLocations().iterator().next();
                Assert.assertFalse(phob1Location == clonedLocation);
                Assert.assertTrue(phob1Location.getStart() == clonedLocation.getStart());
                Assert.assertTrue(phob1Location.getEnd() == clonedLocation.getEnd());
            } else if (match instanceof Hmmer3Match) {
                foundPfam = true;
                Assert.assertFalse(pfamMatch == match);
                Hmmer3Match clonedMatch = (Hmmer3Match) match;
                Assert.assertFalse(pfamMatch.getLocations().iterator().next() == clonedMatch.getLocations().iterator().next());
                Assert.assertTrue(pfamMatch.getSignature() == clonedMatch.getSignature());
                Assert.assertTrue(pfamMatch.getEvalue() == clonedMatch.getEvalue());
                Assert.assertTrue(pfamMatch.getScore() == clonedMatch.getScore());
                Assert.assertTrue(pfamMatch.getLocations().size() == clonedMatch.getLocations().size());
                Hmmer3Match.Hmmer3Location clonedLocation = clonedMatch.getLocations().iterator().next();
                Assert.assertFalse(pfamLocation == clonedLocation);
                Assert.assertTrue(pfamLocation.getStart() == clonedLocation.getStart());
                Assert.assertTrue(pfamLocation.getEnd() == clonedLocation.getEnd());
                Assert.assertTrue(pfamLocation.getEnvelopeStart() == clonedLocation.getEnvelopeStart());
                Assert.assertTrue(pfamLocation.getEnvelopeEnd() == clonedLocation.getEnvelopeEnd());
                Assert.assertEquals(pfamLocation.getHmmBounds(), clonedLocation.getHmmBounds());
                Assert.assertTrue(pfamLocation.getEvalue() == clonedLocation.getEvalue());
                Assert.assertTrue(pfamLocation.getHmmStart() == clonedLocation.getHmmStart());
                Assert.assertTrue(pfamLocation.getHmmEnd() == clonedLocation.getHmmEnd());
                Assert.assertTrue(pfamLocation.getHmmLength() == clonedLocation.getHmmLength());
            } else if (match instanceof CoilsMatch) {
                foundCoils = true;
                Assert.assertFalse(coilsMatch == match);
                Assert.assertFalse(coilsMatch.getLocations().iterator().next() == ((CoilsMatch) match).getLocations().iterator().next());
                Assert.assertTrue(coilsMatch.getSignature() == match.getSignature());
                Assert.assertTrue(coilsMatch.getLocations().size() == ((CoilsMatch) match).getLocations().size());
                CoilsMatch.CoilsLocation clonedLocation = ((CoilsMatch) match).getLocations().iterator().next();
                Assert.assertFalse(coilsLocation == clonedLocation);
                Assert.assertTrue(coilsLocation.getStart() == clonedLocation.getStart());
                Assert.assertTrue(coilsLocation.getEnd() == clonedLocation.getEnd());
            }
        }

        Assert.assertTrue(foundPfam);
        Assert.assertTrue(foundPhobius);
        Assert.assertTrue(foundCoils);

        Assert.assertEquals(matchCount, count);

        count = 0;
        for (Match match : p1.getMatches()) {
            Assert.assertEquals(p1, match.getProtein());
            count++;
        }

        Assert.assertEquals(matchCount, count);


    }
}
