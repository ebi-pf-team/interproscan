package uk.ac.ebi.interpro.scan.web;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Re-initialise the application (without having to restart).
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Controller
@RequestMapping(value = "/reinit", method = RequestMethod.GET)
public class ReinitialiseAppController {
    // Spring managed beans
    private EntryHierarchy entryHierarchy;

    @RequestMapping
    public ModelAndView index() {
        boolean success = entryHierarchy.reinit();

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("entryColours", entryHierarchy.getEntryColourMap());
        m.put("entryHierarchy", entryHierarchy.getEntryHierarchyDataMap());
        m.put("success", success);
        return new ModelAndView("reinit", m);
    }

    // Setter methods required by Spring framework

    @Resource
    public void setEntryHierarchy(EntryHierarchy entryHierarchy) {
        this.entryHierarchy = entryHierarchy;
    }


}
