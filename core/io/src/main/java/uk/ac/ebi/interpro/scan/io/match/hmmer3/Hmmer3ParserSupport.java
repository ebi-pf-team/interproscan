package uk.ac.ebi.interpro.scan.io.match.hmmer3;

import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.HmmsearchOutputMethod;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.util.Map;
import java.io.IOException;
import java.io.Serializable;

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
     *
     * This responsibility is delegated to the sub-class.
     * @param methodMatches which contains a localised (parsing-only) data model of the information in
     * HMMER 3 hmmsearch output.
     * @param rawResults being the Map of protein accessions to RawProteins
     * that the raw results should be added to.
     * @throws java.io.IOException in the event of an IO problem.
     */
    void addMatch(HmmsearchOutputMethod methodMatches, Map<String, RawProtein<T>> rawResults)
            throws IOException;

    /**
     * For maximum efficiency, specific implementations can choose to ignore the
     * alignment section, in which case this method should return false.
     *
     * Implementations for member databases such as Gene3D should return true from this method.
     * @return boolean indicating if the alignment section should be parsed.
     */

    boolean parseAlignments();
    /**
     * To be flexible with different hmmer3 search output file, this method gets either accession or query
     *  accordingly
     *
     */

    enum HmmKey {ACCESSION, NAME}
    HmmKey getHmmKey();
}
