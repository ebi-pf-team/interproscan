package uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Instances of this class hold all of the methods for a particular member database
 * that are integrated into InterPro entries.  Note that if a valid method ID
 * does not appear, this should be interpreted as meaning that the model is not
 * integrated.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class SignatureLibraryIntegratedMethods implements Serializable {

    private final SignatureLibrary signatureLibrary;

    /**
     * Map of method accession (ID) numbers to the appropriate InterProEntry.
     */
    private final Map<String, InterProEntry> methodAccessionToEntry = new HashMap<String, InterProEntry>();

    SignatureLibraryIntegratedMethods(SignatureLibrary signatureLibrary) {
        if (signatureLibrary == null) {
            throw new IllegalArgumentException("A SignatureLibraryIntegratedMethods object cannot be instantiated with a null SignatureLibrary argument.");
        }
        this.signatureLibrary = signatureLibrary;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    /**
     * Returns the MethodMapping object for this SignatureLibrary
     * that has the methodAccession passed in, or null if it is not
     * present.  In use, a null return indicates that the method is not
     * integrated.
     *
     * @param methodAccession being the accession for which the MethodMapping
     *                        object is required.
     * @return the appropriate MethodMapping object.
     */
    public InterProEntry getEntryByMethodAccession(String methodAccession) {
        return methodAccessionToEntry.get(methodAccession);
    }

    /**
     * Method used by the XML unmarshaller to build the object model.
     *
     * @param methodAccession of the member database model / method / signature
     * @param entry           being the Object representing the InterPro entry
     */
    void addMethodEntryMapping(String methodAccession, InterProEntry entry) {
        if (methodAccessionToEntry.containsKey(methodAccession)) {
            throw new IllegalStateException(String.format(
                    "Appear to have found more than one reference to method accession %s in Signature Library %s attempting to map to %s but have found that this method is already mapped to %s",
                    methodAccession,
                    signatureLibrary,
                    entry.getEntryAccession(),
                    methodAccessionToEntry.get(methodAccession).getEntryAccession()));
        }
        methodAccessionToEntry.put(methodAccession, entry);
    }

    /**
     * Convenience method that returns true if the method accession passed in as argument exists in the
     * mapping of methods to InterPro entries.
     *
     * @param methodAccession to be tested
     * @return true if the method accession passed in as argument exists in the
     *         mapping of methods to InterPro entries.
     */
    public boolean containsAccession(String methodAccession) {
        return methodAccessionToEntry.containsKey(methodAccession);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + signatureLibrary + ":\n" +
                methodAccessionToEntry +
                "\n\n\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SignatureLibraryIntegratedMethods that = (SignatureLibraryIntegratedMethods) o;

        if (!methodAccessionToEntry.equals(that.methodAccessionToEntry)) return false;
        if (signatureLibrary != that.signatureLibrary) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = signatureLibrary.hashCode();
        result = 31 * result + methodAccessionToEntry.hashCode();
        return result;
    }
}
