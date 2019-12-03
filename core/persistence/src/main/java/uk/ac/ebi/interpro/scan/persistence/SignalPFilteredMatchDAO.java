package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.SignalPMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;

import java.util.Collection;
import java.util.Map;

/**
 * SignalP filtered match database operations.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface SignalPFilteredMatchDAO extends FilteredMatchDAO<SignalPRawMatch, SignalPMatch> {

    /**
     * Persists a set of parsed SignalP raw match objects as filtered matches.
     * There is no filtering step required.
     *
     * @param filteredProteins
     * @param modelIdToSignatureMap
     * @param proteinIdToProteinMap
     */
    @Transactional
    void persist(Collection<RawProtein<SignalPRawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap);

}
