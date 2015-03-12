package uk.ac.ebi.interpro.scan.io.smart;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 05/04/12
 * Time: 15:59
 */
public class SmartThresholds {

    private Map<String, SmartThresholdFileParser.SmartThreshold> domainNameToSmartThreshold = new HashMap<String, SmartThresholdFileParser.SmartThreshold>();

    private Map<String, SmartThresholdFileParser.SmartThreshold> modelIdToSmartThreshold = new HashMap<String, SmartThresholdFileParser.SmartThreshold>();

    void addThreshold(SmartThresholdFileParser.SmartThreshold smartThreshold) {
        domainNameToSmartThreshold.put(smartThreshold.getDomainName(), smartThreshold);
        modelIdToSmartThreshold.put(smartThreshold.getModelId(), smartThreshold);
    }

    public SmartThresholdFileParser.SmartThreshold getThresholdByDomainName(String domainName) {
        return domainNameToSmartThreshold.get(domainName);
    }

    public SmartThresholdFileParser.SmartThreshold getThresholdByModelId(String modelId) {
        return modelIdToSmartThreshold.get(modelId);
    }
}
