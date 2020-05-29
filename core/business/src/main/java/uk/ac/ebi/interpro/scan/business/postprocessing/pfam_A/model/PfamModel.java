package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model;

import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClan;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

/**
 * Domain class representing a Model, in the context of Clans
 * (intentionally contains very little else - just for post processing).
 *
 * @author Phil Jones
 * @version $Id: PfamModel.java,v 1.3 2009/10/27 12:01:17 pjones Exp $
 * @since 1.0
 */
public class PfamModel implements Serializable {

    /**
     * Optional clan that this model is in.
     */
    PfamClan clan;

    /**
     * List of Models that THIS model is NESTED IN.
     */
    List<PfamModel> nestedIn = Collections.emptyList();

    /**
     * Accession of this model.
     */
    private String accession;

    PfamModel(String accession) {
        if (accession == null){
            throw new IllegalArgumentException ("Both the id and accession used to contruct a PfamModel object must be non-null.");
        }
        this.accession = accession;
    }

    public void setClan(PfamClan clan) {
        if (this.clan != null){
            if (this.clan.equals(clan)) {
                Utilities.verboseLog(10, "new clan: " + clan.getId() + " Pfam  Model: "+ this.toString());
                return;  // deal with duplicates in the clan file
            }
            throw new IllegalStateException ("Found a PfamModel that appears to be in more than one Clan - not an expected state. Model: "+ this.toString());
        }
        this.clan = clan;
    }

    public void addModelThisIsNestedIn(PfamModel model){
        if (nestedIn.size() == 0){   // Points to Collections.EMPTY_LIST
            nestedIn = new ArrayList<PfamModel>();
        }
        nestedIn.add (model);
    }

    public PfamClan getClan() {
        return clan;
    }

    public String getAccession() {
        return accession;
    }

    /**
     * @param candidateModel being the model to compare with.
     * @return true if the models are nested.
     */
    public boolean isNestedIn(PfamModel candidateModel){
        if (candidateModel == null){
            return false;
        }
        if (nestedIn.contains(candidateModel)){
            return true;
        }
        // Recurse...
        for (PfamModel parentModel : nestedIn){
            if (parentModel.isNestedIn(candidateModel)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PfamModel)) return false;

        PfamModel model = (PfamModel) o;

        return accession.equals(model.accession);

    }

    @Override
    public int hashCode() {
        return accession.hashCode();
    }

    @Override
    public String toString() {
        return "PfamModel{" +
                ", accession='" + accession + '\'' +
                ", clan_id=" + ((clan == null) ? null : clan.getId()) +
                "}\n";
    }
}
