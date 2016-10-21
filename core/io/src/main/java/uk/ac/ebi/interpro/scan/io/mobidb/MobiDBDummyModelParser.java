package uk.ac.ebi.interpro.scan.io.mobidb;

import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

/**
 * @author Gift Nuka
 *
 */
public class MobiDBDummyModelParser extends AbstractModelFileParser {

    private static final String MOBIDB_SIGNATURE_NAME = "mobidb-lite";

    /**
     * This is rather badly named as there is nothing to parse...
     * <p/>
     * however, the point is that it returns a SignatureLibraryRelease for MOBIDB,
     * containing one big fat Signature called "mobidb-lite".
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {

        final SignatureLibraryRelease release = new SignatureLibraryRelease(
                this.getSignatureLibrary(),
                this.getReleaseVersionNumber());

        final Signature.Builder builder = new Signature.Builder(MOBIDB_SIGNATURE_NAME);
        final Signature signature = builder.name(MOBIDB_SIGNATURE_NAME).signatureLibraryRelease(release).build();
        release.addSignature(signature);
        return release;
    }
}
