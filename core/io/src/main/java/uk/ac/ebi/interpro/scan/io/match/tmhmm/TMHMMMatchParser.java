package uk.ac.ebi.interpro.scan.io.match.tmhmm;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusFeature;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Parses the output from the TMHMM () match binary.
 * Returns significant matches as a Set of PhobiusProtein
 * objects (light-weight transitory model objects, just used
 * for parsing).
 * <p/>
 * It is the responsibility of the calling code to build
 * the persistable model objects from these transitory objects
 * and persist them.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public class TMHMMMatchParser {

    public Set<PhobiusProtein> parse(InputStream is, String fileName) throws IOException {
        Set<PhobiusProtein> proteinsWithMatches = new HashSet<PhobiusProtein>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            PhobiusProtein protein = null;
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith("//")) {
                    // Process complete record.
                    if (protein == null) {
                        throw new ParseException("Phobius output parsing: Got to the end of an entry marked by //, but don't appear to have a protein ID.", fileName, line, lineNumber);
                    }
                    if (protein.isTM() || protein.isSP()) {
                        // Only store details of meaningful matches.
                        // (Single features "CYTOPLASMIC" or "NON-CYTOPLASMIC" are junk
                        // according to Phobius documentation, so only want matches for proteins
                        // that include signal peptide or transmembrane predictions.)
                        proteinsWithMatches.add(protein);
                    }
                    // Reset flags / proteinId
                    protein = null;
                } else if (line.startsWith("ID")) {
                    if (protein != null) {
                        throw new ParseException("Phobius output parsing: Found a second protein ID line in the same record.", fileName, line, lineNumber);
                    }
                    protein = new PhobiusProtein(line.substring(2).trim());
                } else if (line.startsWith("FT")) {
                    if (protein == null) {
                        throw new ParseException("Phobius output parsing: Found a feature line in a record, but haven't found an ID line yet.", fileName, line, lineNumber);
                    }
                    Matcher ftLineMatcher = PhobiusFeature.FT_LINE_PATTERN.matcher(line);
                    if (ftLineMatcher.matches()) {
                        protein.addFeature(new PhobiusFeature(ftLineMatcher));
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return proteinsWithMatches;
    }
}
