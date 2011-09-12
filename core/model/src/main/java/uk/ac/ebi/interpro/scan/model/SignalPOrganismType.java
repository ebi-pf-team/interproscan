package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.XmlType;

/**
 * Enum for SignalP types.
 * SignalP uses networks and models trained on sequences from the specified organism types.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@XmlType(name = "SignalPOrganismType")
public enum SignalPOrganismType {
    EUK("EUK", "euk", "Eukaryotes"),
    GRAM_POSITIVE("GRAM_POSITIVE", "gram+", "Gram-positive bacteria"),
    GRAM_NEGATIVE("GRAM_NEGATIVE", "gram-", "Gram-negative bacteria");

    private String typeLongName;
    private String typeShortName; // Type as it appears on the binary command line and in the binary output text file
    private String description; // Human readable description

    SignalPOrganismType(String typeLongName, String typeShortName, String description) {
        this.typeLongName = typeLongName;
        this.typeShortName = typeShortName;
        this.description = description;
    }

    /**
     * Get the SignalP organism type enum from the specified type (short name).
     * @param typeShortName Short name for the type (as it appears in the SignalP binary output).
     * @return The SignalP organism type enum.
     */
    public static SignalPOrganismType getSignalPOrganismType(String typeShortName) {
        if (typeShortName != null) {
            for(SignalPOrganismType type : SignalPOrganismType.values()) {
                if (type.getTypeShortName().equals(typeShortName)) {
                    return type;
                }
            }
        }
        return null;
    }

    /**
     * Given a SignalP organism type return the associated SignalP signature library.
     * @param type Organism type
     * @return Signature library
     */
    public static SignatureLibrary getSignatureLibraryFromType(SignalPOrganismType type) {
        if (type.equals(SignalPOrganismType.EUK)) {
            return SignatureLibrary.SIGNALP_EUK;
        }
        else if (type.equals(SignalPOrganismType.GRAM_POSITIVE)) {
            return SignatureLibrary.SIGNALP_GRAM_POSITIVE;
        }
        else if (type.equals(SignalPOrganismType.GRAM_NEGATIVE)) {
            return SignatureLibrary.SIGNALP_GRAM_NEGATIVE;
        }
        return null;
    }

    public String getTypeLongName() {
        return typeLongName;
    }

    public String getTypeShortName() {
        return typeShortName;
    }

    public String getDescription() {
        return description;
    }
}
