package uk.ac.ebi.interpro.scan.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for SignalP types.
 * SignalP uses networks and models trained on sequences from the specified organism types.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum SignalPOrganismType {
    EUK("EUK", "euk", "Eukaryotes", "SIGNALP_EUK"),
    GRAM_POSITIVE("GRAM_POSITIVE", "gram+", "Gram-positive bacteria", "SIGNALP_GRAM_POSITIVE"),
    GRAM_NEGATIVE("GRAM_NEGATIVE", "gram-", "Gram-negative bacteria", "SIGNALP_GRAM_NEGATIVE");

    private String typeLongName; // InterProScan type name
    private String typeShortName; // Type as it appears on the binary command line and in the binary output text file
    private String description; // Human readable description
    private String onionName;  // Onion type name

    private static final Map<String, SignalPOrganismType> ONION_TO_TYPE = new HashMap<String, SignalPOrganismType>();
    private static final Map<String, SignalPOrganismType> SHORTNAME_TO_TYPE = new HashMap<String, SignalPOrganismType>();

    static {
        for (SignalPOrganismType type : SignalPOrganismType.values()) {
            ONION_TO_TYPE.put(type.getOnionName(), type);
            SHORTNAME_TO_TYPE.put(type.getTypeShortName(), type);
        }
    }

    SignalPOrganismType(String typeLongName, String typeShortName, String description, String onionName) {
        this.typeLongName = typeLongName;
        this.typeShortName = typeShortName;
        this.description = description;
        this.onionName = onionName;
    }

    private String getOnionName() {
        return onionName;
    }

    /**
     * Get the SignalP organism type enum from the specified type (short name).
     *
     * @param typeShortName Short name for the type (as it appears in the SignalP binary output).
     * @return The SignalP organism type enum.
     */
    public static SignalPOrganismType getSignalPOrganismTypeByShortName(String typeShortName) {
        if (SHORTNAME_TO_TYPE.containsKey(typeShortName)) {
            return SHORTNAME_TO_TYPE.get(typeShortName);
        }
        return null;
    }

    public static SignalPOrganismType getSignalPOrganismTypeByOnionType(String onionType) {
        if (ONION_TO_TYPE.containsKey(onionType)) {
            return ONION_TO_TYPE.get(onionType);
        }
        return null;
    }

    /**
     * Given a SignalP organism type return the associated SignalP signature library.
     *
     * @param type Organism type
     * @return Signature library
     */
    public static SignatureLibrary getSignatureLibraryFromType(SignalPOrganismType type) {
        if (type.equals(SignalPOrganismType.EUK)) {
            return SignatureLibrary.SIGNALP_EUK;
        } else if (type.equals(SignalPOrganismType.GRAM_POSITIVE)) {
            return SignatureLibrary.SIGNALP_GRAM_POSITIVE;
        } else if (type.equals(SignalPOrganismType.GRAM_NEGATIVE)) {
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
