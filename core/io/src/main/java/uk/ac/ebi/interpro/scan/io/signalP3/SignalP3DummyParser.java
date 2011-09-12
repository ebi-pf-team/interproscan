package uk.ac.ebi.interpro.scan.io.signalP3;

import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 16/05/11
 * Time: 15:05
 */
public class SignalP3DummyParser extends AbstractModelFileParser {

    private static final String SIGNAL_P_3_SIGNATURE_NAME = "SignalPeptide";


    /**
     * This is rather badly named as there is nothing to parse...
     * <p/>
     * however, the point is that it returns a SignatureLibraryRelease for Phobius,
     * containing the signatures defined in the PhobiusFeatureType enum.
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {

        final SignatureLibraryRelease release = new SignatureLibraryRelease(
                this.getSignatureLibrary(),
                this.getReleaseVersionNumber());

        final Signature.Builder builder = new Signature.Builder(SIGNAL_P_3_SIGNATURE_NAME);
        final Signature signature = builder.name(SIGNAL_P_3_SIGNATURE_NAME).signatureLibraryRelease(release).build();
        release.addSignature(signature);
        return release;
    }
}
