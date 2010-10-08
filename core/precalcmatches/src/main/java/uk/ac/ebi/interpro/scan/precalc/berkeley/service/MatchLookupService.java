package uk.ac.ebi.interpro.scan.precalc.berkeley.service;

import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.List;

/**
 * Class providing web service lookup of matches.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@WebService
public class MatchLookupService {

    @WebMethod
    public List<BerkeleyMatch> getMatches(String proteinMD5) {
        return null;
    }
}
