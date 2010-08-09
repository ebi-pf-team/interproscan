package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface Hmmer3ParserSupport<T extends RawMatch> extends Serializable {


    /**
     * Implemented for specific member databases.  Different databases use different specific model classes
     * and may need to filter the matches at this point, based upon different criteria.
     * <p/>
     * This responsibility is delegated to the sub-class.
     *
     * @param methodMatches which contains a localised (parsing-only) data model of the information in
     *                      HMMER 3 hmmsearch output.
     * @param rawResults    being the Map of protein accessions to RawProteins
     *                      that the raw results should be added to.
     * @throws java.io.IOException in the event of an IO problem.
     */
    void addMatch(HmmSearchRecord methodMatches, Map<String, RawProtein<T>> rawResults)
            throws IOException;

    /**
     * For maximum efficiency, specific implementations can choose to ignore the
     * alignment section, in which case this method should return false.
     * <p/>
     * Implementations for member databases such as Gene3D should return true from this method.
     *
     * @return boolean indicating if the alignment section should be parsed.
     */

    boolean parseAlignments();

    /**
     * Returns the model ID or model accession.
     *
     * @param modelIdentLinePatternMatcher Matcher to the Pattern retrieved by the getModelIdentLinePattern method
     * @return the ID or accession of the method.
     */
    String getModelId(Matcher modelIdentLinePatternMatcher);

    /**
     * Returns the model length, or null if this value is not available.
     *
     * @param modelIdentLinePatternMatcher matcher to the Pattern retrieved by the getModelIdentLinePattern method
     * @return the model accession length, or null if this value is not available.
     */
    Integer getModelLength(Matcher modelIdentLinePatternMatcher);

    /**
     * To be flexible with different hmmer3 search output file, this method gets either accession or query
     * accordingly
     */
    enum HmmKey {

        ACCESSION("Accession:"), NAME("Query:");

        private final String prefix;

        public String getPrefix() {
            return prefix;
        }

        HmmKey(String prefix) {
            this.prefix = prefix;
        }


    }


    HmmKey getHmmKey();

    /**
     * As the regular expressions required to parse the 'ID' or 'Accession' lines appear
     * to differ from one member database to another, factored out here.
     *
     * @return a Pattern object to parse the ID / accession line.
     */
    Pattern getModelIdentLinePattern();
}
