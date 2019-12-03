package uk.ac.ebi.interpro.scan.io.tmhmm;

import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;

/**
 * Model installer class for TMHMM prediction.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TMHMMSignatureBuilder extends AbstractModelFileParser {


    /**
     * Returns a SignatureLibraryRelease for TMHMM
     * containing the signatures defined in the {@link uk.ac.ebi.interpro.scan.model.TMHMMSignature} enum.
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {

        final SignatureLibraryRelease release = new SignatureLibraryRelease(
                this.getSignatureLibrary(),
                this.getReleaseVersionNumber());

        for (final TMHMMSignature type : TMHMMSignature.values()) {
            final Model model = new Model(type.getAccession(), type.getShortDesc(), type.getDescription());
            final Signature.Builder builder = new Signature.Builder(type.getAccession());
            final Signature signature = builder
                    .description(type.getDescription())
                    .model(model)
                    .signatureLibraryRelease(release)
                    .build();
            release.addSignature(signature);
        }
        return release;
    }
}
