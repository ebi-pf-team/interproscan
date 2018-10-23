package uk.ac.ebi.interpro.scan.business.sequence;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface PrecalculatedProteinLookup {

    Protein getPrecalculated(Protein protein, Map<String, SignatureLibraryRelease> analysisJobMap);

    Set<Protein> getPrecalculated(Set<Protein> proteins,Map<String, SignatureLibraryRelease> analysisJobMap);

    boolean isConfigured();

    /**
     *   If the client and the server are based on the same version of interproscan
     *   return true, otherwise return false
     */
    boolean isSynchronised() throws IOException;


    boolean isAnalysisVersionConsistent(Set<Protein> preCalculatedProteins, List<.KVSequenceEntry> berkeleyMatches, Map<String, SignatureLibraryRelease> analysisJobMap);
}
