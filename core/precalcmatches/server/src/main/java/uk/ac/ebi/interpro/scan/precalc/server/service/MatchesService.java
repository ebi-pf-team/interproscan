package uk.ac.ebi.interpro.scan.precalc.server.service;

import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.List;

/**
 * Web service interface - just the ability to "getMatches"
 * for the moment.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface MatchesService {

    /**
     * Web service request for a set of matches, based upon
     * protein MD5 sequence checksums.
     *
     * @param proteinMD5s md5 checksum of sequences.
     * @return a List of matches for these proteins.
     */
    List<BerkeleyMatch> getMatches(List<String> proteinMD5s);

    /**
     * Web service request for a List of protein sequence MD5
     * checksums where the protein sequence has been run through
     * the analysis pipeline and so should NOT be recalculated
     * (i.e. any returned MD5s should NOT be run against
     * the models).
     *
     * @param proteinMD5s md5 checksum of sequences.
     * @return a List of MD5s for proteins that have been calculated previously.
     */
    List<String> isPrecalculated(List<String> proteinMD5s);

    /**
     * Cleanly shuts down the Berkeley DB environment.
     */
    void shutdown();
}
