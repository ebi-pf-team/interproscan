package uk.ac.ebi.interpro.scan.io.smart;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 05/04/12
 * Time: 16:05
 */
public class SmartOverlaps {

    private Map<String, SmartOverlappingFileParser.SmartOverlap> modelIdToOverlap = new HashMap<String, SmartOverlappingFileParser.SmartOverlap>();

    void addSmartOverlap(SmartThresholds smartThresholds, SmartOverlappingFileParser.SmartOverlap smartOverlap) {
        SmartThresholdFileParser.SmartThreshold smartThreshold = smartThresholds.getThresholdByDomainName(smartOverlap.getDomainName());
        if (smartThreshold != null) {
            modelIdToOverlap.put(smartThreshold.getModelId(), smartOverlap);
        }
    }

    /**
     * For a given modelId, return the correct SmartOverlap object, or null if there is no overlap object.
     *
     * @param modelId to look up SmartOverlap by
     * @return a SmartOverlap object, or null if there is none.
     */
    public SmartOverlappingFileParser.SmartOverlap getSmartOverlapByModelId(String modelId) {
        return modelIdToOverlap.get(modelId);
    }
}
