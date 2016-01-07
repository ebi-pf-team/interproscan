package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.match.cdd.ParseCDDMatch;
import uk.ac.ebi.interpro.scan.model.CDDMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignalPMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Gift Nuka
 * @version $id
 * @since v5.16
 */
//public interface CDDFilteredMatchDAO extends GenericDAO<CDDMatch, Long> {
//
//    /**
//     * Persists a set of ParseCDDMatch objects as filtered matches:
//     * there is no filtering step with CDD.
//     *
//     * @param cddMatches being a Set of ParseCoilsMatch objects to be persisted.
//     */
//    @Transactional
//    void persist(Set<ParseCDDMatch> cddMatches);
//}

public interface CDDFilteredMatchDAO extends FilteredMatchDAO<CDDRawMatch, CDDMatch> {

    /**
     * Persists a set of parsed CDD raw match objects as filtered matches.
     * There is no filtering step required.
     *
     * @param filteredProteins
     * @param modelIdToSignatureMap
     * @param proteinIdToProteinMap
     */
    @Transactional
    void persist(Collection<RawProtein<CDDRawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap);

}
