package uk.ac.ebi.interpro.scan.io.tmhmm;

/**
 * Defines a prediction for specific amino acids in protein sequences, if they are inside, outside or transmembrane.
 * These terms are used by TMHMM.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum TMHMMPrediction {
    //TODO:Find out what upper case O means in this context
    INSIDE_CELL("inside", "inside cell region"), OUTSIDE_CELL("outside", "outside cell region"), MEMBRANE("TMhelix", "transmembrane helix"), OTHER("O", "O region");

    private String signature;

    private String description;

    private TMHMMPrediction(String signature, String description) {
        this.signature = signature;
        this.description = description;
    }

    public String getSignature() {
        return signature;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return signature;
    }
}
