package uk.ac.ebi.interpro.scan.business.postprocessing.smart;

import org.springframework.core.io.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 05/04/12
 * Time: 17:06
 * <p/>
 * This class holds references to the optional SMART resources and is able to determine
 * if they are present.
 * <p/>
 * Purely a singleton populated by injection, so thread safe in the context of i5.
 */
public class SmartResourceManager {

    private Resource thresholdFileResource;
    private Resource overlappingFileResource;

    /**
     * Required for post processing to happen - if not present then match filtering is not performed (all raw matches
     * becomes filtered matches).
     *
     * @param thresholdFileResource The location of the Smart threshold data file, e.g. "THRESHOLDS"
     */
    public void setThresholdFileResource(Resource thresholdFileResource) {
        this.thresholdFileResource = thresholdFileResource;
    }

    /**
     * Required for post processing to happen - if not present then match filtering is not performed (all raw matches
     * becomes filtered matches).
     *
     * @param overlappingFileResource The location of the Smart overlap data file, e.g. "overlapping"
     */
    public void setOverlappingFileResource(Resource overlappingFileResource) {
        this.overlappingFileResource = overlappingFileResource;
    }

    public Resource getThresholdFileResource() {
        return thresholdFileResource;
    }

    public Resource getOverlappingFileResource() {
        return overlappingFileResource;
    }

    /**
     * This method returns true if the threshold and overlap files are present.
     *
     * @return true if the threshold and overlap files are present.
     */
    public boolean isLicensed() {
        return resourceExists(thresholdFileResource) && resourceExists(overlappingFileResource);
    }

    private boolean resourceExists(Resource resource) {
        return resource != null && resource.exists() && resource.isReadable();
    }
}
