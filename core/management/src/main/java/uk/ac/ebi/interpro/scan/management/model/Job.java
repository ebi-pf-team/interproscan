package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;

import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Job implements Serializable {

    private Long id;

    private String description;

    /**
     * OPTIONAL reference to a SignatureLibrary that this Job
     * has been created to process.
     *
     * This is the library than new Jobs are related to by default.
     *
     * The CONCRETE relationship is to SignatureLibraryRelease, which is
     * used to set up new JobInstances.
     *
     * Based upon this default, when a new SignatureLibraryRelease is created,
     * the loading code should require the installer to decide whether or not the
     *
     */
    private SignatureLibrary defaultSignatureLibrary;

    /**
     * List of SignatureLibraryReleases that this Job has been specified for.
     */
    private List<SignatureLibraryRelease> signatureLibraryReleases;

    private List<Step> steps = new ArrayList<Step>();

    public Job() {
    }


    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * A descriptive name for this job.
     * @param description a descriptive (and preferably unique)
     * name for this job.
     */
    @Required
    public void setDescription(String description) {
        this.description = description;
    }

    public List<Step> getSteps() {
        return steps;
    }

    void addStep(Step step) {
        steps.add(step);
    }
}
