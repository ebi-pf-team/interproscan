package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model;

import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClan;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamModel;

import java.util.Map;
import java.util.TreeMap;
import java.io.Serializable;

/**
 * The in-memory java representation of a Clan file.
 *
 * (Containing only the information needed for post-processing).
 *
 * @author Phil Jones
 * @version $Id: PfamClanData.java,v 1.3 2009/10/26 14:10:24 pjones Exp $
 * @since 1.0
 */
public class PfamClanData implements Serializable {

    private final Map<String, PfamClan> clanIdToClanMap = new TreeMap<String, PfamClan>();

    private final Map<String, PfamModel> modelAccessionToModelMap = new TreeMap<String, PfamModel>();

    public PfamClanData() {
    }

    /**
     *
     * @param clanId being the unique identifier of the clan.
     * @return a PfamClan object (new instance, or existing if the clan has already been seen).
     */
    PfamClan createOrGetClan(String clanId){
        PfamClan clan = clanIdToClanMap.get(clanId);
        if (clan == null){
            clan = new PfamClan(clanId);
            clanIdToClanMap.put (clanId, clan);
        }
        return clan;
    }

    /**
     * Adds a new Model file.  A bit tricky here as placing the models into two separate maps, so
     * need some sanity checking on the way.
     * @param accession the accession of the Model.
     * @return the Model (new instance, or retrieved from the
     */
    public PfamModel addModel (String accession){
        // Sanity check - if the model has already been stored, it should be in
        // both maps and be the same INSTANCE. (not just equal)
        PfamModel model = modelAccessionToModelMap.get(accession);

        if (model != null){
            throw new IllegalStateException("PfamModel accession " + accession + " appears more than once in the Pfam Clan file.");
        }

        model = new PfamModel(accession);
        modelAccessionToModelMap.put (accession, model);
        return model;
    }

    /**
     * Gets the Clan for a model with the accession passed in as argument.
     * @param modelAccession for which the Clan is required.
     * @return the clan of the model with the accession passed in as argument, or null
     * if it is not in a clan.
     */
    public PfamClan getClanByModelAccession(String modelAccession) {
        PfamModel candidateModel = getModelByModelAccession(modelAccession);
        return (candidateModel == null) ? null : candidateModel.getClan();
    }

    /**
     * Gets the PfamModel object for a particular modelAccession, or null if
     * the model accession is not recognised.
     * @param modelAccession being the accession of the required model
     * @return the corresponding PfamModel object.
     */
    public PfamModel getModelByModelAccession(String modelAccession) {
        return modelAccessionToModelMap.get(modelAccession);
    }

    public int getModelCount() {
        return modelAccessionToModelMap.size();
    }
}
