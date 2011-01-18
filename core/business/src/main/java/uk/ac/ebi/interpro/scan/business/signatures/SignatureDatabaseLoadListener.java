package uk.ac.ebi.interpro.scan.business.signatures;

import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

/**
 * This listener is responsible for creating new StepInstances
 * when a new member database is loaded.  Naturally it relies upon
 * a new Job XML file having been created.
 * <p/>
 * The jobXML ID is passed in as an argument to allow this to work.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface SignatureDatabaseLoadListener {

    /**
     * A new set of member database signatures has been loaded into the database.
     * For ALL proteins in the database, new StepInstances should be created FOR THIS MEMBER DATABASE RELEASE.
     *
     * @param release      being the SignatureLibraryRelease object - indicates which member database Steps must be created.
     * @param analysisName being the name of the job, for which StepInstances must be added for this SignatureLibraryRelease
     */
    void signatureDatabaseLoaded(SignatureLibraryRelease release, String analysisName);
}
