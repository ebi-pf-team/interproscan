package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.Query;
import java.util.*;

/**
 * Gene3D filtered match data access object.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class Gene3dHmmer3FilteredMatchDAO extends Hmmer3FilteredMatchDAO<Gene3dHmmer3RawMatch> {


    private static final Logger LOGGER = Logger.getLogger(Gene3dHmmer3FilteredMatchDAO.class.getName());

    /**
     * Helper method to retrieve a Map for lookup of Signature
     * objects by signature accession.
     *
     * @param signatureLibrary        being the SignatureLibrary in this analysis.
     * @param signatureLibraryRelease the current version of the signature library in this analysis.
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    protected Map<String, Signature> getModelAccessionToSignatureMap(SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                                                                     Collection<RawProtein<Gene3dHmmer3RawMatch>> rawProteins) {
        //Model accession to signatures map
        LOGGER.info("Creating model accession to signature map...");
        final Map<String, Signature> result = new HashMap<String, Signature>();

        List<String> modelIDs = new ArrayList<String>();
        for (RawProtein<Gene3dHmmer3RawMatch> rawProtein : rawProteins) {
            for (RawMatch rawMatch : rawProtein.getMatches()) {
                boolean isLibraryGene3d = signatureLibrary.getName().equals(SignatureLibrary.GENE3D.getName());
                String modelAccession = rawMatch.getModelId();
                if (isLibraryGene3d){
                    String gene3dModelAccession = (modelAccession.split("\\|")[2]).split("/")[0];
                    //Utilities.verboseLog("gene3d modelAccession: " + gene3dModelAccession + " from - " + modelAccession );
                    modelAccession = gene3dModelAccession;
                }
                modelIDs.add(modelAccession);
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("... for " + modelIDs.size() + " model IDs.");
        }

        for (int index = 0; index < modelIDs.size(); index += MAXIMUM_IN_CLAUSE_SIZE) {
            int endIndex = index + MAXIMUM_IN_CLAUSE_SIZE;
            if (endIndex > modelIDs.size()) {
                endIndex = modelIDs.size();
            }
            //Signature accession slice
            final List<String> modelIdsSlice = modelIDs.subList(index, endIndex);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Querying a batch of " + modelIdsSlice.size() + " model IDs.");
            }

            final Query query =
                    entityManager.createQuery(
                            "select s from Signature s, Model m " +
                                    "where s.id = m.signature.id " +
                                    "and m.accession in (:accession) " +
                                    "and s.signatureLibraryRelease.version = :version " +
                                    "and s.signatureLibraryRelease.library = :signatureLibrary");
            query.setParameter("accession", modelIdsSlice);
            query.setParameter("signatureLibrary", signatureLibrary);
            query.setParameter("version", signatureLibraryRelease);
            @SuppressWarnings("unchecked") List<Signature> signatures = query.getResultList();

            //Utilities.verboseLog("SignatureModel query: "
            //        + "accession: " + modelIdsSlice.toString()
            //        + " signatureLibrary: " + signatureLibrary
            //        + " version: " + signatureLibraryRelease);
            for (Signature s : signatures) {
                for (Model m : s.getModels().values()) {
                    result.put(m.getAccession(), s);
                    LOGGER.debug("accession: " + m.getAccession() + " signature: " + s);
                    //Utilities.verboseLog("accession: " + m.getAccession() + " signature: " + s);
                }
            }
        }
        return result;
    }


}
