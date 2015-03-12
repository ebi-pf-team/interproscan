package uk.ac.ebi.interpro.scan.io.signalp.model;

import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

/**
 * Constructs the model for SignalP.
 * Simply contains two Signature objects called "SignalP-TM" and "SignalP-noTM".
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignalPDummyParser extends AbstractModelFileParser {

    // SignalP predictions are made by these neural networks.
    private static final String SIGNALP_SIGNATURE_NAME1 = "SignalP-TM";
    private static final String SIGNALP_SIGNATURE_NAME2 = "SignalP-noTM";

    /**
     * This is rather badly named as there is nothing to parse...
     * <p/>
     * However, the point is that it returns a SignatureLibraryRelease for SignalP,
     * containing two Signature objects called "SignalP-TM" and "SignalP-noTM".
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {

        final SignatureLibraryRelease release = new SignatureLibraryRelease(
                this.getSignatureLibrary(),
                this.getReleaseVersionNumber());

        final Model model1 = new Model(SIGNALP_SIGNATURE_NAME1, SIGNALP_SIGNATURE_NAME1, null, null);
        final Signature.Builder builder1 = new Signature.Builder(SIGNALP_SIGNATURE_NAME1);
        final Signature signature1 = builder1.name(SIGNALP_SIGNATURE_NAME1).model(model1).signatureLibraryRelease(release).build();
        release.addSignature(signature1);

        final Model model2 = new Model(SIGNALP_SIGNATURE_NAME2, SIGNALP_SIGNATURE_NAME2, null, null);
        final Signature.Builder builder2 = new Signature.Builder(SIGNALP_SIGNATURE_NAME2);
        final Signature signature2 = builder2.name(SIGNALP_SIGNATURE_NAME2).model(model2).signatureLibraryRelease(release).build();
        release.addSignature(signature2);

        return release;
    }

}
