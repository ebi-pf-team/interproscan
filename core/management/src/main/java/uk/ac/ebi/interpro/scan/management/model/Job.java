package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class describes a Job, which is constructed from N steps.
 * Jobs and Steps are templates for analyses.  To actually run
 * analyses against specific proteins (and perhaps specific models)
 * StepInstances are instantiated.  These instances are then
 * run as StepExecutions.  If a StepExecution fails, and the
 * Step is configured to be repeatable, then another attempt
 * to run the instance will be made.
 * <p/>
 * NOTE: Instances of Jobs and Steps are defined in Spring XML.  They
 * are NOT persisted to the database - only StepInstances and StepExecutions
 * are persisted.
 *
 * @author Phil Jones
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Job implements Serializable, BeanNameAware {

    private String id;

    private boolean analysis = false;

    //The version attribute is only set if a job is of type analysis
    private SignatureLibraryRelease libraryRelease;

    private String description;

    private Map<String, String> mandatoryParameters;

    private List<String> nonEmptyPaths;

    /**
     * This boolean flag is only important if a job is of type analysis
     * <p/>
     * This flag is important for analysis which aren't integrated in the lookup service and makes sure they will run locally
     * <p/>
     * If set to TRUE it will perform the analysis locally instead of using on the lookup service results
     * <p/>
     * Used in the StepCreationSequenceLoadListener to create step instances for such jobs
     */
    private boolean doRunLocally = false;

    /**
     * This boolean flag is only important if a job is of type analysis.
     * <p/>
     * If you configure I5 with multiple versions of a member database, then you should set one of those as active equals TRUE and all other as FALSE.
     * <p/>
     * So if a user doesn't set the 'appl' parameter, I5 will only perform all analysis jobs which are marked as isActive=true.
     * <p/>
     */
    private boolean active = true;

    /**
     *   This boolean flag tests if the job is deprecated.
     */
    private boolean deprecated = false;

    /**
     * List of steps.  this is transient so they don't all get shoved
     * over the wire when each StepExecution is run.
     */
    private transient List<Step> steps = new ArrayList<Step>();

    public Job() {
    }


    public String getDescription() {
        return description;
    }

    /**
     * A descriptive name for this job.
     *
     * @param description a descriptive (and preferably unique)
     *                    name for this job.
     */
    @Required
    public void setDescription(String description) {
        this.description = description;
    }

    @Required
    public void setAnalysis(boolean isAnalysis) {
        this.analysis = isAnalysis;
    }

    public SignatureLibraryRelease getLibraryRelease() {
        return libraryRelease;
    }

    public void setLibraryRelease(SignatureLibraryRelease libraryRelease) {
        this.libraryRelease = libraryRelease;
    }

    public boolean isAnalysis() {
        return analysis;
    }

    public List<Step> getSteps() {
        return steps;
    }

    void addStep(Step step) {
        steps.add(step);
    }

    public String getId() {
        return id;
    }

    public void setBeanName(String id) {
        this.id = id;
    }

    public Map<String, String> getMandatoryParameters() {
        return mandatoryParameters;
    }

    public void setMandatoryParameters(Map<String, String> mandatoryParameters) {
        this.mandatoryParameters = mandatoryParameters;
    }

    public List<String> getNonEmptyPaths() {
        return nonEmptyPaths;
    }

    public void setNonEmptyPaths(List<String> nonEmptyPaths) {
        this.nonEmptyPaths = nonEmptyPaths;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Job");
        if(libraryRelease != null ) {
            sb.append(" (version:").append(libraryRelease.getVersion()).append(")");
        }
        sb.append("{id='").append(id).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", steps=").append(steps);
        sb.append('}');
        return sb.toString();
    }

    public boolean isDoRunLocally() {
        return doRunLocally;
    }

    public void setDoRunLocally(boolean doRunLocally) {
        this.doRunLocally = doRunLocally;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
}
