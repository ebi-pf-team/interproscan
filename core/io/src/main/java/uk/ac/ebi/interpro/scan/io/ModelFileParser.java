package uk.ac.ebi.interpro.scan.io;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.io.Serializable;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface ModelFileParser extends Serializable {

    /**
     * Method to set the io.Resource that is the model file being loaded.
     *
     * @param modelFiles
     */
    public void setModelFiles(Resource... modelFiles);

    /**
     * Method to set the release version number.
     *
     * @param releaseVersion being the version of the member database release being loaded.
     */
    @Required
    public void setReleaseVersionNumber(String releaseVersion);

    /**
     * Set the SignatureLibrary being loaded.
     *
     * @param library being one of the enum of valid SignatureLibrary objects.
     */
    @Required
    public void setSignatureLibrary(SignatureLibrary library);

    /**
     * Method to retrieve the release version being loaded.
     *
     * @return the release version being loaded.
     */
    public String getReleaseVersionNumber();

    /**
     * Method to retrieve the SignatureLibrary being loaded.
     *
     * @return the SignatureLibrary being loaded.
     */
    public SignatureLibrary getSignatureLibrary();

    /**
     * Allows the model loader to avoid storing the abstract
     * in the database if not required (e.g. for InterProScan mode)
     *
     * @param storeAbstract to indicate if the abstract should be stored.
     */
    public void setStoreAbstract(boolean storeAbstract);

    /**
     * Method to parse the model file(s) and return a SignatureLibraryRelease.
     * If more than one model file was specified then the parser will assume that the files are in the same format.
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Transactional
    public SignatureLibraryRelease parse() throws IOException;

    String getAnalysisJobId();
}
