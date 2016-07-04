package uk.ac.ebi.interpro.scan.management.model.implementations.cdd;

import uk.ac.ebi.interpro.scan.management.model.implementations.CompositeParseStep;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawSite;

/**
 * Parses the output of CDD and stores the results to the database
 * (as filtered results - there is currently no second filtering step.)
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 17.0
 */

public class ParseCDDOutputStep extends CompositeParseStep<CDDRawMatch, CDDRawSite> {

}
