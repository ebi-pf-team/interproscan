package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.*;

/**
 * ProDom CRUD database operations.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProDomFilteredMatchDAOImpl extends FilteredMatchDAOImpl<ProDomRawMatch, ProDomMatch> implements ProDomFilteredMatchDAO {

    private String proDomReleaseVersion;

    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public ProDomFilteredMatchDAOImpl(String version) {
        super(ProDomMatch.class);
        this.proDomReleaseVersion = version;
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins      being the Collection of filtered RawProtein objects to persist
     * @param modelIdToSignatureMap a Map of signature accessions to Signature objects.
     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
     */
    @Transactional
    public void persist(Collection<RawProtein<ProDomRawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<ProDomRawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            Set<ProDomMatch.ProDomLocation> locations = null;
            String currentSignatureAc = null;
            Signature currentSignature = null;
            ProDomRawMatch lastRawMatch = null;
            for (ProDomRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) continue;

                if (currentSignatureAc == null || !currentSignatureAc.equals(rawMatch.getModelId())) {
                    if (currentSignatureAc != null) {
                        // Not the first...
                        protein.addMatch(new ProDomMatch(currentSignature, locations));
                    }
                    // Reset everything
                    locations = new HashSet<ProDomMatch.ProDomLocation>();
                    currentSignatureAc = rawMatch.getModelId();
                    currentSignature = modelIdToSignatureMap.get(currentSignatureAc);
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find PRINTS signature " + currentSignatureAc + " in the database.");
                    }
                }
                locations.add(
                        new ProDomMatch.ProDomLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd()
                        )
                );
                lastRawMatch = rawMatch;
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                protein.addMatch(new ProDomMatch(currentSignature, locations));
            }
            entityManager.persist(protein);
            entityManager.flush();
        }
    }
}
