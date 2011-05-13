package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class BerkeleyToI5ModelDAOImpl implements BerkeleyToI5ModelDAO {

    private static final Logger LOGGER = Logger.getLogger(BerkeleyToI5ModelDAOImpl.class.getName());

    private Map<SignatureLibrary, BerkeleyMatchConverter> signatureLibraryToMatchConverter;

    protected EntityManager entityManager;

    @PersistenceContext
    protected void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Required
    public void setSignatureLibraryToMatchConverter(Map<SignatureLibrary, BerkeleyMatchConverter> signatureLibraryToMatchConverter) {
        this.signatureLibraryToMatchConverter = signatureLibraryToMatchConverter;
    }

    /**
     * Method to store matches based upon lookup from the Berkeley match database of precalculated matches.
     *
     * @param nonPersistedProtein being a newly instantiated Protein object
     * @param berkeleyMatches     being a Set of BerkeleyMatch objects, retrieved / unmarshalled from
     *                            the Berekeley Match web service.
     */
    @Override
    @Transactional(readOnly = true)
    public void populateProteinMatches(Protein nonPersistedProtein, List<BerkeleyMatch> berkeleyMatches) {

        // Collection of BerkeleyMatches of different kinds.
        // Iterate over them,
        for (BerkeleyMatch berkeleyMatch : berkeleyMatches) {
            final SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(berkeleyMatch.getSignatureLibraryName());
            // Retrieve Signature to match
            Query sigQuery = entityManager.createQuery("select s from Signature s where s.accession = :sig_ac and s.signatureLibraryRelease.version = :sig_lib_version and s.signatureLibraryRelease.library = :library");
            sigQuery.setParameter("sig_ac", berkeleyMatch.getSignatureAccession());
            sigQuery.setParameter("sig_lib_version", berkeleyMatch.getSignatureLibraryRelease());
            sigQuery.setParameter("library", sigLib);

            @SuppressWarnings("unchecked") List<Signature> signatures = sigQuery.getResultList();
            if (signatures.size() == 0) {   // This Signatures is not in I5, so cannot store this one.
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Retrieved " + berkeleyMatch + ".  Unable to persist this BerkeleyMatch as the combination of" +
                            "Signature accession / SignatureLibrary / SignatureLibraryRelease is not in the I5 database.");
                }
                continue;
            }
            if (signatures.size() > 1) {     // Probably this check is overkill.
                throw new IllegalStateException("There should not be more than one Signature with the same accession for the same Signature Database Release.");
            }
            // determine the type or the match currently being observed
            // Retrieve the appropriate converter to turn the BerkeleyMatch into an I5 match
            // Type is based upon the member database type.

            BerkeleyMatchConverter matchConverter = signatureLibraryToMatchConverter.get(sigLib);
            if (matchConverter != null) {
                Match i5Match = matchConverter.convertMatch(berkeleyMatch, signatures.get(0));
                nonPersistedProtein.addMatch(i5Match);
            }
        }
    }
}
