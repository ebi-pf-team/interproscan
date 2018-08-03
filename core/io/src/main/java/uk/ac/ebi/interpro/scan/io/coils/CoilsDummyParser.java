package uk.ac.ebi.interpro.scan.io.coils;

import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

/**
 * @author Phil Jones
 *         Date: 16/05/11
 *         Time: 15:05
 */
public class CoilsDummyParser extends AbstractModelFileParser {

    private static final String COILS_SIGNATURE_NAME = "Coil";

    /**
     * This is rather badly named as there is nothing to parse...
     * <p/>
     * however, the point is that it returns a SignatureLibraryRelease for Coils,
     * containing one big fat Signature called "Coil".
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {

        final SignatureLibraryRelease release = new SignatureLibraryRelease(
                this.getSignatureLibrary(),
                this.getReleaseVersionNumber());

        final Model model = new Model(COILS_SIGNATURE_NAME, COILS_SIGNATURE_NAME, null);
        final Signature.Builder builder = new Signature.Builder(COILS_SIGNATURE_NAME);
        final Signature signature = builder.name(COILS_SIGNATURE_NAME)
                .signatureLibraryRelease(release)
                .model(model)
                .build();
        release.addSignature(signature);
        return release;
    }
}
