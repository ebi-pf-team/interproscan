package uk.ac.ebi.interpro.scan.web;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.interpro.scan.web.io.*;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for InterPro protein view.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Controller
@RequestMapping(value = "/proteins", method = RequestMethod.GET)
public class ProteinViewController {

    private static final Logger LOGGER = Logger.getLogger(ProteinViewController.class.getName());
    private static final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10; // Max number of scale markers to include on the match diagram (one will always be 0 and another protein length)!

    // Spring managed beans
    private EntryHierarchy entryHierarchy;
    private CreateSimpleProteinFromMatchData matchData;

    @RequestMapping
    public String index() {
        return "proteins";
    }

    /**
     * Returns protein page.
     *
     * @param  id   Protein accession or MD5 checksum, for example "P38398"
     * @return Protein page
     */
    @RequestMapping(value = "/{id}")
    public ModelAndView protein(@PathVariable String id) {
        return new ModelAndView("protein", buildModelMap(retrieve(id)));
    }

    /**
     * Returns main body of protein page for inclusion in DBML
     *
     * @param  id   Protein accession or MD5 checksum, for example "P38398"
     * @return Main body of protein page for inclusion in DBML
     */
    @RequestMapping(value = "/{id}/body")
    public ModelAndView proteinBody(@PathVariable String id) {
        return new ModelAndView("protein-body", buildModelMap(retrieve(id)));
    }

    /**
     * Returns protein features for inclusion in DBML
     *
     * @param  id   Protein accession or MD5 checksum, for example "P38398"
     * @return Protein features for inclusion in DBML
     */
    @RequestMapping(value = "/{id}/features")
    public ModelAndView proteinFeatures(@PathVariable String id) {
        return new ModelAndView("protein-features", buildModelMap(retrieve(id)));
    }

    private Map<String, Object> buildModelMap(SimpleProtein p) {
        Map<String, Object> m = new HashMap<String, Object>();
        if (p != null) {
            m.put("protein", p);
            m.put("entryColours", entryHierarchy.getEntryColourMap());
            m.put("scale", generateScaleMarkers(p.getLength(), MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
        } // Else no match data was found for the protein therefore nothing to display
        return m;
    }

    private SimpleProtein retrieve(String id) {
        // TODO: Check if id is MD5 using regex (using Protein class code?)
        try {
            return queryByAccession(id);
        }
        catch (IOException e) {
            // TODO: Do not allow exception to go beyond here, otherwise user will see in browser
            throw new IllegalStateException("Could not retrieve " + id, e);
        }
    }

    /**
     * Returns protein for given accession number
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return Protein for given accession
     */
    private SimpleProtein queryByAccession(String ac) throws IOException {
        SimpleProtein protein = matchData.queryByAccession(ac);
        return protein;
    }

    /**
     * Generate a string of comma-separated numbers that will be used to mark the scale of the match diagram.
     * @param maxNumScaleMarkers Maximum number of scale markers (should be 2 or more)!
     * @return Comma-separated list of marker positions
     */
    private String generateScaleMarkers(int proteinLength, int maxNumScaleMarkers) {
        if (maxNumScaleMarkers < 2) {
            // Doesn't make sense! Zero and protein length should always be included whatever happens!
            LOGGER.warn("Protein match diagram given un-expected number of scale markers! Was " + maxNumScaleMarkers +
                    " but should be 2 or more!");
            maxNumScaleMarkers = 2;
        }
        int scale = calcScale(proteinLength, maxNumScaleMarkers);
        StringBuilder sb = new StringBuilder("0"); // The first scale marker (position zero)
        int index = 0;
        int numRemaining = proteinLength;
        while (index <= proteinLength) {
            index += scale;
            numRemaining-= scale;
            sb.append(",");
            if ((numRemaining > 0) && (numRemaining < scale)) {
                // This will be the penultimate marker, unless...
                if (numRemaining < (scale / 2)) {
                    // Not many amino acids remaining, may as well just use the protein length
                    // (E.g. Don't want something like "0,200,400,401" since the 400 and 401 would be too close
                    // together to display both bits of text)!
                    sb.append(proteinLength);
                    break;
                }

            }
            if (index >= proteinLength) {
                // The last scale marker (at protein length position)!
                sb.append(proteinLength);
            }
            else {
                // Append and continue
                sb.append(index);
            }
        }
        return sb.toString();
    }

    /**
     * Calculate scale marker intervals.
     *
     * @param maxNumScaleMarkers Maximum number of scale markers
     * @return Default gap between each scale markers
     */
    private int calcScale(int proteinLength, int maxNumScaleMarkers) {
        int scale = 1;
        while (true) {
            if (proteinLength / (scale) <= maxNumScaleMarkers) {
                return scale;
            }
            else if (proteinLength / (scale*2) <= maxNumScaleMarkers) {
                return scale*2;
            }
            else if (proteinLength / (scale*5) <= maxNumScaleMarkers) {
                return scale*5;
            }
            scale *= 10;
        }
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
