package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.implementations.MatchAndSitePostProcessingStep;
import uk.ac.ebi.interpro.scan.model.Hmmer3MatchWithSites;
import uk.ac.ebi.interpro.scan.model.raw.PIRSRHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.PIRSRHmmer3RawSite;

/**
 * Filter for PIRSR using HMMER3.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PIRSRHmmer3FilterStep<A extends PIRSRHmmer3RawMatch, B extends Hmmer3MatchWithSites, C extends PIRSRHmmer3RawSite, D extends Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site>  extends MatchAndSitePostProcessingStep<A, B, C, D> {
}
