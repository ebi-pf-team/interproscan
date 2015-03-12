package uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel;

import uk.ac.ebi.interpro.scan.model.PhobiusFeatureType;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple model class to hold details of Phobius matches
 * during parsing.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class PhobiusProtein {

    private String proteinIdentifier;

    private List<PhobiusFeature> features = new ArrayList<PhobiusFeature>();

    private boolean isSP = false,
            isTM = false;

    public PhobiusProtein(String proteinIdentifier) {
        this.proteinIdentifier = proteinIdentifier;
    }

    public void addFeature (PhobiusFeature feature){
        if (feature.getFeatureType() != null){ // Not all FT lines yield features to be stored.
            final boolean signalFeature =
                    PhobiusFeatureType.SIGNAL_PEPTIDE == feature.getFeatureType() ||
                    PhobiusFeatureType.SIGNAL_PEPTIDE_C_REGION == feature.getFeatureType() ||
                    PhobiusFeatureType.SIGNAL_PEPTIDE_N_REGION == feature.getFeatureType() ||
                    PhobiusFeatureType.SIGNAL_PEPTIDE_H_REGION == feature.getFeatureType();
            isSP = isSP || signalFeature;
            isTM = isTM || PhobiusFeatureType.TRANSMEMBRANE == feature.getFeatureType();
            features.add (feature);
        }
    }

    public String getProteinIdentifier() {
        return proteinIdentifier;
    }

    public List<PhobiusFeature> getFeatures() {
        return features;
    }

    public boolean isSP() {
        return isSP;
    }

    public boolean isTM() {
        return isTM;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(proteinIdentifier);
        sb.append(":  ").append(features);
        sb.append("\n\n");
        return sb.toString();
    }
}
