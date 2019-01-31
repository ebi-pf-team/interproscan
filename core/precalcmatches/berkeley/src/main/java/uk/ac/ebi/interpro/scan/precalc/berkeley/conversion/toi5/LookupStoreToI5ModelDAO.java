package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Description
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface LookupStoreToI5ModelDAO {

    void populateProteinMatches(Protein nonPersistedProtein, List<KVSequenceEntry> kvSequenceEntries, Map<String, SignatureLibraryRelease> analysisJobMap);

    void populateProteinMatches(Set<Protein> preCalculatedProteins, List<KVSequenceEntry> kvSequenceEntries, Map<String, SignatureLibraryRelease> analysisJobMap);

    void checkMatchDAO();
}
