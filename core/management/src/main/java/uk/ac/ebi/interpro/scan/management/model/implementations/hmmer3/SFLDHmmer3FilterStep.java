package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.implementations.MatchAndSitePostProcessingStep;
import uk.ac.ebi.interpro.scan.model.Hmmer3MatchWithSites;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawSite;

/**
 * Filter for SFLD using HMMER3.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SFLDHmmer3FilterStep<A extends SFLDHmmer3RawMatch, B extends Hmmer3MatchWithSites, C extends SFLDHmmer3RawSite, D extends Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site>  extends MatchAndSitePostProcessingStep<A, B, C, D> {
}
