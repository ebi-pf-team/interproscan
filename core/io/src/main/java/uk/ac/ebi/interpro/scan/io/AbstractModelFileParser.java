package uk.ac.ebi.interpro.scan.io;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

/**
 * Base abstract class for Model File parsers.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class AbstractModelFileParser implements ModelFileParser {

    protected Resource modelFile;
    protected String releaseVersion;
    protected SignatureLibrary library;
    protected boolean storeAbstract = true;

    /**
     * Method to set the io.Resource that is the model file being loaded.
     *
     * @param modelFile the io.Resource that is the model file being loaded.
     */
    @Required
    public void setModelFile(Resource modelFile) {
        this.modelFile = modelFile;
    }

    /**
     * Method to set the release version number.
     *
     * @param releaseVersion being the version of the member database release being loaded.
     */
    @Required
    public void setReleaseVersionNumber(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    /**
     * Set the SignatureLibrary being loaded.
     *
     * @param library being one of the enum of valid SignatureLibrary objects.
     */
    @Required
    public void setSignatureLibrary(SignatureLibrary library) {
        this.library = library;
    }

    /**
     * Method to retrieve the release version being loaded.
     *
     * @return the release version being loaded.
     */
    public String getReleaseVersionNumber() {
        return releaseVersion;
    }

    /**
     * Method to retrieve the SignatureLibrary being loaded.
     *
     * @return the SignatureLibrary being loaded.
     */
    public SignatureLibrary getSignatureLibrary() {
        return library;
    }

    /**
     * Allows the model loader to avoid storing the abstract
     * in the database if not required (e.g. for InterProScan mode)
     *
     * @param storeAbstract to indicate if the abstract should be stored.
     */
    @Override
    public void setStoreAbstract(boolean storeAbstract) {
        this.storeAbstract = storeAbstract;
    }
}
