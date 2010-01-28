package uk.ac.ebi.interpro.scan.model;

/**
 * This enum defines accessions and names
 * for all of the possible Signatures
 * and Models for Phobius analysis.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public enum PhobiusFeatureType {

    CYTOPLASMIC_DOMAIN("CYTOPLASMIC_DOMAIN", "Cytoplasmic domain",
            "Region of a membrane-bound protein predicted to be outside the membrane, in the cytoplasm."),
    NON_CYTOPLASMIC_DOMAIN("NON_CYTOPLASMIC_DOMAIN", "Non cytoplasmic domain",
            "Region of a membrane-bound protein predicted to be outside the membrane, in the extracellular region."),
    TRANSMEMBRANE ("TRANSMEMBRANE", "Transmembrane region",
            "Region of a membrane-bound protein predicted to be embedded in the membrane."),
    SIGNAL_PEPTIDE_N_REGION ("SIGNAL_PEPTIDE_N_REGION", "Signal peptide N-region",
            "N-terminal region of a signal peptide."),
    SIGNAL_PEPTIDE_H_REGION ("SIGNAL_PEPTIDE_H_REGION", "Signal peptide H-region",
            "Hydrophobic region of a signal peptide."),
    SIGNAL_PEPTIDE_C_REGION ("SIGNAL_PEPTIDE_C_REGION", "Signal peptide C-region",
            "C-terminal region of a signal peptide.");


    PhobiusFeatureType(String accession, String name, String description) {
        this.accession = accession;
        this.name = name;
    }

    private String accession;

    private String name;

    private String description;

    /**
     * Returns the value to be used for setting the Signature.accession or the Model.accesion.
     * @return the value to be used for setting the Signature.accession or the Model.accesion.
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Returns the value to be used for setting the Signature.accession or the Model.name.
     * @return the value to be used for setting the Signature.accession or the Model.name.
     */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Utility method to check that a given Signature is valid.
     * This method is light-weight - it is NOT checking that the
     * SignatureLibrary associated with the Signature is of the correct
     * type.
     * @param signature being checked.
     * @return true of the Signature accession and name are recognised.
     */
    public static boolean isValidSignature (Signature signature){
        for (PhobiusFeatureType type : PhobiusFeatureType.values()){
            if (type.getAccession().equals(signature.getAccession())
                    && type.getName().equals(signature.getName())){
                return true;
            }
        }
        return false;
    }
}
