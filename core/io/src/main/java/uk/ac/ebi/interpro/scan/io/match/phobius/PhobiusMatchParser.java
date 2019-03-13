package uk.ac.ebi.interpro.scan.io.match.phobius;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusFeature;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.model.PhobiusFeatureType;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import uk.ac.ebi.interpro.scan.model.raw.PhobiusRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Parses the output from the Phobius match binary.
 * Returns significant matches as a Set of PhobiusProtein
 * objects (light-weight transitory model objects, just used
 * for parsing).
 * <p/>
 * It is the responsibility of the calling code to build
 * the persistable model objects from these transitory objects
 * and persist them.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class PhobiusMatchParser  implements MatchParser<PhobiusRawMatch> {

    private SignatureLibrary signatureLibrary;
    private  String signatureLibraryRelease;

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }


    public Set<RawProtein<PhobiusRawMatch>> parse(InputStream is) throws IOException, ParseException {

        Map<String, RawProtein<PhobiusRawMatch>> matchData = new HashMap<>();

        Set<PhobiusRawMatch> rawMatches = parseFileInput(is);

        for (PhobiusRawMatch rawMatch : rawMatches) {
            String sequenceId = rawMatch.getSequenceIdentifier();
            if (matchData.containsKey(sequenceId)) {
                RawProtein<PhobiusRawMatch> rawProtein = matchData.get(sequenceId);
                rawProtein.addMatch(rawMatch);
            } else {
                RawProtein<PhobiusRawMatch> rawProtein = new RawProtein<>(sequenceId);
                rawProtein.addMatch(rawMatch);
                matchData.put(sequenceId, rawProtein);
            }
        }

        return new HashSet<>(matchData.values());
    }


    public Set<PhobiusRawMatch> parseFileInput(InputStream is) throws IOException {
        Set<PhobiusRawMatch> matches = new HashSet<>();
        Set<PhobiusRawMatch> matchesPerProtein = new HashSet<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String currentProteinAccession = null;
            int lineNumber = 0;
            String line;
            boolean isSP =  false;
            boolean isTM =  false;
            PhobiusRawMatch phobiusRawMatch = null;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith("//")) {
                    // Process complete record.
                    if (currentProteinAccession == null) {
                        throw new ParseException("Phobius output parsing: Got to the end of an entry marked by //, but don't appear to have a protein ID.", "fileName", line, lineNumber);
                    }
                    if (isTM || isSP) {
                        // Only store details of meaningful matches.
                        // (Single features "CYTOPLASMIC" or "NON-CYTOPLASMIC" are junk
                        // according to Phobius documentation, so only want matches for proteins
                        // that include signal peptide or transmembrane predictions.)
                        matches.addAll(matchesPerProtein);
                        //Utilities.verboseLog("Consider these phobius raw matches");
                    }
                    //Utilities.verboseLog("reset: protein Id: " + currentProteinAccession + " matches = " + matchesPerProtein.size());
                    // Reset flags / proteinId
                    matchesPerProtein = new HashSet<>();
                    currentProteinAccession = null;
                    isSP =  false;
                    isTM =  false;
                } else if (line.startsWith("ID")) {
                    if (currentProteinAccession != null) {
                        throw new ParseException("Phobius output parsing: Found a second protein ID line in the same record.", "fileName", line, lineNumber);
                    }
                    currentProteinAccession = line.substring(2).trim();
                    //Utilities.verboseLog("currentProteinAccession: " + currentProteinAccession);
                    //protein = new PhobiusProtein(line.substring(2).trim());

                } else if (line.startsWith("FT")) {
                    if (currentProteinAccession == null) {
                        throw new ParseException("Phobius output parsing: Found a feature line in a record, but haven't found an ID line yet.", "fileName", line, lineNumber);
                    }
                    Matcher ftLineMatcher = PhobiusFeature.FT_LINE_PATTERN.matcher(line);
                    if (ftLineMatcher.matches()) {
                        //protein.addFeature(new PhobiusFeature(ftLineMatcher));
                        int start = Integer.parseInt(ftLineMatcher.group(2));
                        int stop = Integer.parseInt(ftLineMatcher.group(3));
                        final String type = ftLineMatcher.group(1);
                        final String group4 = ftLineMatcher.group(4);
                        final String qualifier =
                                (group4 != null && group4.trim().length() > 0)
                                        ? group4
                                        : null;
                        PhobiusFeatureType featureType = PhobiusFeatureType.getFeatureTypeByTypeAndQualifier(type, qualifier);

                        if (featureType != null){ // Not all FT lines yield features to be stored.
                            final boolean signalFeature =
                                    PhobiusFeatureType.SIGNAL_PEPTIDE == featureType ||
                                            PhobiusFeatureType.SIGNAL_PEPTIDE_C_REGION == featureType ||
                                            PhobiusFeatureType.SIGNAL_PEPTIDE_N_REGION == featureType ||
                                            PhobiusFeatureType.SIGNAL_PEPTIDE_H_REGION == featureType;
                            isSP =  isSP || signalFeature;
                            isTM =  isTM || PhobiusFeatureType.TRANSMEMBRANE == featureType;
                        }
                        phobiusRawMatch = new PhobiusRawMatch(currentProteinAccession, featureType.getAccession(), signatureLibrary, signatureLibraryRelease, start, stop, featureType, isSP, isTM);
                        matchesPerProtein.add(phobiusRawMatch);
                        //Utilities.verboseLog("phobiusRawMatch: " + phobiusRawMatch.toString());
                    }
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return matches;
    }
}
