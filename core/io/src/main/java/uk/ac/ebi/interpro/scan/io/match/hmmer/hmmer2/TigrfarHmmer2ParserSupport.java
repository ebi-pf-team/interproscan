package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel.Hmmer2SearchRecord;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
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
public class TigrfarHmmer2ParserSupport<T extends RawMatch> implements Hmmer2ParserSupport<T> {
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
    @Override
    public void addMatch(Hmmer2SearchRecord methodMatches, Map<String, RawProtein<T>> rawResults) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * For maximum efficiency, specific implementations can choose to ignore the
     * alignment section, in which case this method should return false.
     * <p/>
     * Implementations for member databases such as Gene3D should return true from this method.
     *
     * @return boolean indicating if the alignment section should be parsed.
     */
    @Override
    public boolean parseAlignments() {
        return false;
    }

    /**
     * Returns the model ID or model accession.
     *
     * @param modelIdentLinePatternMatcher Matcher to the Pattern retrieved by the getModelIdentLinePattern method
     * @return the ID or accession of the method.
     */
    @Override
    public String getModelId(Matcher modelIdentLinePatternMatcher) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the model length, or null if this value is not available.
     *
     * @param modelIdentLinePatternMatcher matcher to the Pattern retrieved by the getModelIdentLinePattern method
     * @return the model accession length, or null if this value is not available.
     */
    @Override
    public Integer getModelLength(Matcher modelIdentLinePatternMatcher) {
        return null;
    }

    @Override
    public HmmKey getHmmKey() {
        return HmmKey.QUERY;
    }

    /**
     * As the regular expressions required to parse the 'ID' or 'Accession' lines appear
     * to differ from one member database to another, factored out here.
     *
     * @return a Pattern object to parse the ID / accession line.
     */
    @Override
    public Pattern getModelIdentLinePattern() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
