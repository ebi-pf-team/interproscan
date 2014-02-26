package uk.ac.ebi.interpro.scan.web;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Controller
@RequestMapping(value = "/condensed-views", method = RequestMethod.GET)
public class CondensedViewController {

    private static final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10; // Max number of scale markers to include on the match diagram (one will always be 0 and another protein length)!
    private EntryHierarchy entryHierarchy;
    private PageResources pageResources;

    @Resource
    public void setPageResources(PageResources pageResources) {
        this.pageResources = pageResources;
    }

    @RequestMapping
    public String index() {
        return "condensed-views";
    }


    @RequestMapping(value = "/{ida}")
    public ModelAndView condensedView(@PathVariable String ida) {

        // Example IDA string:
        // 513/A0A006#1962:166-422~14729:155-297~14729:357-425#
        ida = "513/A0A006#1962:166-422~14729:155-297~14729:357-425#";

        CondensedView condensedView = idaToCondensedView(ida);
        int proteinLength = condensedView.getProteinLength();

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("standalone", false); // Code not used by InterProScan
        if (condensedView != null) {
            m.put("condensedView", condensedView);
            m.put("proteinLength", proteinLength);
            m.put("entryColours", entryHierarchy.getEntryColourMap());
            m.put("scale", ProteinViewHelper.generateScaleMarkers(proteinLength, MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
        } // Else no protein data was found therefore nothing to display
        if (pageResources != null) {
            Map<String, String> pageResourcesMap = pageResources.getResourcesMap();
            for (String key : pageResourcesMap.keySet()) {
                m.put(key, pageResourcesMap.get(key));
            }
        }
        ModelAndView mav = new ModelAndView("interpro/condensed-view", m);
        return mav;
    }


    private CondensedView idaToCondensedView(String ida) {
        Pattern proteinInfoPattern = Pattern.compile("^(\\d+)/(\\w{6})#");
        Pattern supermatchPattern = Pattern.compile("(\\d{1,6}:\\d+-\\d+[~#])");
        Pattern supermatchInfoPattern = Pattern.compile("(\\d{1,6}):(\\d+)-(\\d+)[~#]");

        String proteinAc = null;
        Integer proteinLength = null;

        Matcher matcher = proteinInfoPattern.matcher(ida);
        if (matcher.find()) {
            if (matcher.groupCount() == 2) {
                proteinLength = Integer.parseInt(matcher.group(1));
                proteinAc = matcher.group(2);
            }
        }

        List<SuperMatchBucket> buckets = new ArrayList<SuperMatchBucket>();

        matcher = supermatchPattern.matcher(ida);
        while (matcher.find()) {
            String superMatchStr = matcher.group();
            Matcher matcher2 = supermatchInfoPattern.matcher(superMatchStr);
            if (matcher2.find()) {
                if (matcher2.groupCount() == 3) {
                    String entryAc = matcher2.group(1);
                    entryAc = "IPR" + StringUtils.leftPad(entryAc, 6, "0");
                    int posFrom = Integer.parseInt(matcher2.group(2));
                    int posTo = Integer.parseInt(matcher2.group(3));
                    SimpleEntry simpleEntry = new SimpleEntry(entryAc, null, null, EntryType.DOMAIN);
                    SimpleLocation simpleLocation = new SimpleLocation(posFrom, posTo);
                    SimpleSuperMatch simpleSuperMatch = new SimpleSuperMatch(simpleEntry, simpleLocation);
                    SuperMatchBucket bucket = new SuperMatchBucket(simpleSuperMatch);
                    buckets.add(bucket);
                }
            }
        }
        return new CondensedView(proteinLength, buckets);

    }

        // Setter methods required by Spring framework

    @Resource
    public void setEntryHierarchy(EntryHierarchy entryHierarchy) {
        this.entryHierarchy = entryHierarchy;
    }

}
