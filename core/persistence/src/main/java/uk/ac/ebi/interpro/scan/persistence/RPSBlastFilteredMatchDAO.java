package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.Map;

/**
 * @author Gift Nuka
 * @version $id
 * @since v5.16
 */
//public interface RPSBlastFilteredMatchDAO extends GenericDAO<RPSBlastMatch, Long> {
//
//    /**
//     * Persists a set of ParseRPSBlastMatch objects as filtered matches:
//     * there is no filtering step with RPSBlast.
//     *
//     * @param RPSBlastMatches being a Set of ParseCoilsMatch objects to be persisted.
//     */
//    @Transactional
//    void persist(Set<ParseRPSBlastMatch> rpsBlastMatches);
//}

public interface RPSBlastFilteredMatchDAO extends FilteredMatchDAO<RPSBlastRawMatch, RPSBlastMatch> {

    /**
     * Persists a set of parsed RPSBlast raw match objects as filtered matches.
     * There is no filtering step required.
     *
     * @param filteredProteins
     * @param modelIdToSignatureMap
     * @param proteinIdToProteinMap
     */
    @Transactional
    void persist(Collection<RawProtein<RPSBlastRawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap);

}
