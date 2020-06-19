package uk.ac.ebi.interpro.scan.web;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.interpro.scan.web.io.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.CondensedView;
import uk.ac.ebi.interpro.scan.web.model.PageResources;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for InterPro protein structure view.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Controller
@RequestMapping(value = "/protein-structures", method = RequestMethod.GET)
public class ProteinStructureViewController {

    private static final Logger LOGGER = LogManager.getLogger(ProteinStructureViewController.class.getName());
    private static final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10; // Max number of scale markers to include on the match diagram (one will always be 0 and another protein length)!

    // Spring managed beans
    private EntryHierarchy entryHierarchy;
    private CreateSimpleProteinFromMatchData matchData;
    private PageResources pageResources;

    @Resource
    public void setPageResources(PageResources pageResources) {
        this.pageResources = pageResources;
    }

    @RequestMapping
    public String index() {
        return "protein-structures";
    }

    /**
     * Returns protein structure page.
     *
     * @param id Protein accession or MD5 checksum, for example "P38398"
     * @return Protein structure page
     */
    @RequestMapping(value = "/{id}")
    public ModelAndView protein(@PathVariable String id) {
        return new ModelAndView("protein-structure", buildModelMap(retrieve(id), true));
    }

    /**
     * Returns main body of protein structure page for inclusion in DBML
     *
     * @param id Protein accession or MD5 checksum, for example "P38398"
     * @return Main body of protein structure page for inclusion in DBML
     */
    @RequestMapping(value = "/{id}/body")
    public ModelAndView proteinBody(@PathVariable String id) {
        return new ModelAndView("protein-structure-body", buildModelMap(retrieve(id), false));
    }

    private Map<String, Object> buildModelMap(SimpleProtein p, boolean standalone) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("standalone", standalone);
        if (p != null) {
            final int proteinLength = p.getLength();
            final List<SimpleEntry> entries = p.getAllEntries();
            final CondensedView condensedView = new CondensedView(entries, proteinLength);

            m.put("protein", p);
            m.put("condensedView", condensedView);
            m.put("entryColours", entryHierarchy.getEntryColourMap());
            m.put("scale", ProteinViewHelper.generateScaleMarkers(p.getLength(), MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
        } // Else no protein data was found therefore nothing to display
        if (pageResources != null) {
            Map<String, String> pageResourcesMap = pageResources.getResourcesMap();
            for (String key : pageResourcesMap.keySet()) {
                m.put(key, pageResourcesMap.get(key));
            }
        }
        return m;
    }

    private SimpleProtein retrieve(String id) {
        // TODO: Check if id is MD5 using regex (using Protein class code?)
        try {
            return queryByAccession(id);
        } catch (IOException e) {
            // TODO: Do not allow exception to go beyond here, otherwise user will see in browser
            throw new IllegalStateException("Could not retrieve " + id, e);
        }
    }

    /**
     * Returns protein for given accession number
     *
     * @param ac Protein accession, for example "P38398"
     * @return Protein for given accession
     */
    private SimpleProtein queryByAccession(String ac) throws IOException {
        return matchData.queryByAccession(ac);
    }


    // Setter methods required by Spring framework

    @Resource
    public void setEntryHierarchy(EntryHierarchy entryHierarchy) {
        this.entryHierarchy = entryHierarchy;
    }

    @Resource
    public void setMatchData(CreateSimpleProteinFromMatchData matchData) {
        this.matchData = matchData;
    }
}
