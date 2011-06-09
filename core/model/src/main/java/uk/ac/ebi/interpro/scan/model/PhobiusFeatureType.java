package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.XmlType;

/**
 * This enum defines accessions and names
 * for all of the possible Signatures
 * and Models for Phobius analysis.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
@XmlType(name = "PhobiusFeatureType")
public enum PhobiusFeatureType {

    SIGNAL_PEPTIDE("SIGNAL_PEPTIDE", "Signal Peptide",
            "SIGNAL", null, "Signal peptide region"
    ),
    CYTOPLASMIC_DOMAIN("CYTOPLASMIC_DOMAIN", "Cytoplasmic domain",
            "DOMAIN", "CYTOPLASMIC.",
            "Region of a membrane-bound protein predicted to be outside the membrane, in the cytoplasm."),
    NON_CYTOPLASMIC_DOMAIN("NON_CYTOPLASMIC_DOMAIN", "Non cytoplasmic domain",
            "DOMAIN", "NON CYTOPLASMIC.",
            "Region of a membrane-bound protein predicted to be outside the membrane, in the extracellular region."),
    TRANSMEMBRANE("TRANSMEMBRANE", "Transmembrane region",
            "TRANSMEM", null,
            "Region of a membrane-bound protein predicted to be embedded in the membrane."),
    SIGNAL_PEPTIDE_N_REGION("SIGNAL_PEPTIDE_N_REGION", "Signal peptide N-region",
            "DOMAIN", "N-REGION.",
            "N-terminal region of a signal peptide."),
    SIGNAL_PEPTIDE_H_REGION("SIGNAL_PEPTIDE_H_REGION", "Signal peptide H-region",
            "DOMAIN", "H-REGION.",
            "Hydrophobic region of a signal peptide."),
    SIGNAL_PEPTIDE_C_REGION("SIGNAL_PEPTIDE_C_REGION", "Signal peptide C-region",
            "DOMAIN", "C-REGION.",
            "C-terminal region of a signal peptide.");

    PhobiusFeatureType(String accession, String name, String featureType, String featureTypeQualifier, String description) {
        this.accession = accession;
        this.name = name;
        this.featureType = featureType;
        this.featureTypeQualifier = featureTypeQualifier;
        this.description = description;
    }

    private String accession;

    private String name;

    private String featureType;

    private String featureTypeQualifier;

    private String description;

    /**
     * Returns the value to be used for setting the Signature.accession or the Model.accesion.
     *
     * @return the value to be used for setting the Signature.accession or the Model.accesion.
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Returns the value to be used for setting the Signature.accession or the Model.name.
     *
     * @return the value to be used for setting the Signature.accession or the Model.name.
     */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the correct PhobiusFeatureType based upon the feature type and
     * the feature type qualifier in the flat file.
     *
     * @param featureType          in the flat file.
     * @param featureTypeQualifier
     * @return
     */
    public static PhobiusFeatureType getFeatureTypeByTypeAndQualifier(String featureType, String featureTypeQualifier) {
        for (PhobiusFeatureType type : PhobiusFeatureType.values()) {
            if (equalsIncludingNull(type.featureType, featureType)
                    &&
                    equalsIncludingNull(type.featureTypeQualifier, featureTypeQualifier)) {
                return type;
            }
        }
        return null;
    }

    private static boolean equalsIncludingNull(String one, String two) {
        return (one == null && two == null) || (one != null && one.equals(two));
    }

    /**
     * Utility method to check that a given Signature is valid.
     * This method is light-weight - it is NOT checking that the
     * SignatureLibrary associated with the Signature is of the correct
     * type.
     *
     * @param signature being checked.
     * @return true of the Signature accession and name are recognised.
     */
    public static boolean isValidSignature(Signature signature) {
        for (PhobiusFeatureType type : PhobiusFeatureType.values()) {
            if (type.getAccession().equals(signature.getAccession())
                    && type.getName().equals(signature.getName())) {
                return true;
            }
        }
        return false;
    }

}
