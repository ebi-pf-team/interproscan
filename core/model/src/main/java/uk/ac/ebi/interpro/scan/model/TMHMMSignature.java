package uk.ac.ebi.interpro.scan.model;

/**
 * Defines a prediction for specific amino acids in protein sequences, if they are inside, outside or transmembrane.
 * These terms are used by TMHMM.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum TMHMMSignature {
    //TODO:Find out what upper case O means in this context
    INSIDE_CELL("inside", "inside cell region", "inside cell region"),
    OUTSIDE_CELL("outside", "outside cell region", "outside cell region"),
    MEMBRANE("TMhelix", "transmembrane helix", "Region of a membrane-bound protein predicted to be embedded in the membrane."),
    OTHER("O", "O region", null);

    private String accession;

    private String shortDesc;

    private String description;

    private TMHMMSignature(String accession, String shortDesc, String description) {
        this.accession = accession;
        this.shortDesc = shortDesc;
        this.description = description;
    }

    public String getAccession() {
        return accession;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return accession;
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
        for (TMHMMSignature type : TMHMMSignature.values()) {
            if (type.getAccession().equals(signature.getAccession())) {
                return true;
            }
        }
        return false;
    }
}