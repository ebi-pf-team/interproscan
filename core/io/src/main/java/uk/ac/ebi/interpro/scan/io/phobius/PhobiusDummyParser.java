package uk.ac.ebi.interpro.scan.io.phobius;

import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.PhobiusFeatureType;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 16/05/11
 * Time: 15:05
 */
public class PhobiusDummyParser extends AbstractModelFileParser {


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

        for (final PhobiusFeatureType type : PhobiusFeatureType.values()) {
            final Model model = new Model(type.getAccession(), type.getName(), type.getDescription());
            final Signature.Builder builder = new Signature.Builder(type.getAccession());
            final Signature signature = builder
                    .name(type.getName())
                    .model(model)
                    .description(type.getDescription())
                    .signatureLibraryRelease(release)
                    .build();
            release.addSignature(signature);
        }
        return release;
    }
}
