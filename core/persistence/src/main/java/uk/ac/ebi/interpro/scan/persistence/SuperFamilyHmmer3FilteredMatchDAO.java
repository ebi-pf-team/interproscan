package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SuperFamilyHmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;

import java.util.Collection;
import java.util.Map;

/**
 * SuperFamily database operations.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface SuperFamilyHmmer3FilteredMatchDAO extends FilteredMatchDAO<SuperFamilyHmmer3RawMatch, SuperFamilyHmmer3Match> {

    /**
     * Persists a set of parsed SuperFamily match objects as filtered matches.
     * There is no filtering step required.
     */
    @Transactional
    void persist(Collection<RawProtein<SuperFamilyHmmer3RawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap);

}
