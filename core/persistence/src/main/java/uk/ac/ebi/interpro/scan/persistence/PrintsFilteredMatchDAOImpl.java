package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.FingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PrintsFilteredMatchDAOImpl extends FilteredMatchDAOImpl<PrintsRawMatch, FingerPrintsMatch> {

    private static final Logger LOGGER = Logger.getLogger(PrintsFilteredMatchDAOImpl.class.getName());

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public PrintsFilteredMatchDAOImpl() {
        super(FingerPrintsMatch.class);
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins      being the Collection of filtered RawProtein objects to persist
     * @param modelIdToSignatureMap a Map of signature accessions to Signature objects.
     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
     */
    @Override
    @Transactional
    protected void persist(Collection<RawProtein<PrintsRawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<PrintsRawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            Set<FingerPrintsMatch.FingerPrintsLocation> locations = null;
            String currentSignatureAc = null;
            Signature currentSignature = null;
            PrintsRawMatch lastRawMatch = null;
            for (PrintsRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) continue;

                if (currentSignatureAc == null || !currentSignatureAc.equals(rawMatch.getModelId())) {
                    if (currentSignatureAc != null) {
                        // Not the first...
                        protein.addMatch(new FingerPrintsMatch(currentSignature, lastRawMatch.getEvalue(), lastRawMatch.getGraphscan(), locations));
                    }
                    // Reset everything
                    locations = new HashSet<FingerPrintsMatch.FingerPrintsLocation>();
                    currentSignatureAc = rawMatch.getModelId();
                    currentSignature = modelIdToSignatureMap.get(currentSignatureAc);
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find PRINTS signature " + currentSignatureAc + " in the database.");
                    }
                }
                locations.add(
                        new FingerPrintsMatch.FingerPrintsLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getPvalue(),
                                rawMatch.getScore(),
                                rawMatch.getMotifNumber()
                        )
                );
                lastRawMatch = rawMatch;
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                protein.addMatch(new FingerPrintsMatch(currentSignature, lastRawMatch.getEvalue(), lastRawMatch.getGraphscan(), locations));
            }
            entityManager.persist(protein);
            entityManager.flush();
        }
    }
}
