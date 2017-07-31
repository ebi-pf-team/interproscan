package uk.ac.ebi.interpro.scan.web;

import org.apache.log4j.Logger;
import org.springframework.core.io.UrlResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;
import uk.ac.ebi.interpro.scan.web.io.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.CondensedView;
import uk.ac.ebi.interpro.scan.web.model.PageResources;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import javax.annotation.Resource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for InterPro protein view.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Controller
@Component
@RequestMapping(value = "/proteins", method = RequestMethod.GET)
public class ProteinViewController {

    private static final Logger LOGGER = Logger.getLogger(ProteinViewController.class.getName());
    private static final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10; // Max number of scale markers to include on the match diagram (one will always be 0 and another protein length)!

    // Spring managed beans
    private EntryHierarchy entryHierarchy;
    private CreateSimpleProteinFromMatchData matchData;
    private Jaxb2Marshaller marshaller;
    private PageResources pageResources;

    @Resource
    public void setPageResources(PageResources pageResources) {
        this.pageResources = pageResources;
    }

    @Resource(name = "jaxb2")
    public void setMarshaller(Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    @RequestMapping
    public String index() {
        return "proteins";
    }

    /**
     * Returns protein page.
     *
     * @param id Protein accession or MD5 checksum, for example "P38398"
     * @return Protein page
     */
    @RequestMapping(value = "/{id}")
    public ModelAndView protein(@PathVariable String id) {
        return new ModelAndView("protein", buildModelMap(retrieve(id), true));
    }

    /**
     * Returns main body of protein page for inclusion in DBML
     *
     * @param id Protein accession or MD5 checksum, for example "P38398"
     * @return Main body of protein page for inclusion in DBML
     */
    @RequestMapping(value = "/{id}/body")
    public ModelAndView proteinBody(@PathVariable String id) {
        return new ModelAndView("protein-body", buildModelMap(retrieve(id), false));
    }

    /**
     * Takes care of rendering the protein view for a given XML resource (a serialized Protein or ProteinMatchesHolder).
     *
     * @param url XML resource.
     * @return Rendered protein view.
     */
    @RequestMapping(value = "/render", method = RequestMethod.GET)
    public ModelAndView protein(final @RequestParam UrlResource url) {
        Protein protein = deserialise(url);
        if (protein != null) {
            // Handle the possibilities that the Protein object has no Xref, one xref or multiple xrefs.
            ProteinXref xref;
            if (protein.getCrossReferences().size() == 0) {
                // No Xref - create Xref "Unknown"
                xref = new ProteinXref("Unknown");
            } else if (protein.getCrossReferences().size() > 1) {
                // Multiple Xrefs - concatenate them together
                StringBuilder combinedAcs = new StringBuilder();
                for (ProteinXref x : protein.getCrossReferences()) {
                    if (combinedAcs.length() > 0) combinedAcs.append(", ");
                    combinedAcs.append(x.getIdentifier());
                }
                xref = new ProteinXref(combinedAcs.toString());
            } else {
                // One xref - use it directly.
                xref = protein.getCrossReferences().iterator().next();
            }
            return new ModelAndView("protein", buildModelMap(SimpleProtein.valueOf(protein, xref, entryHierarchy), true));
        }
        return new ModelAndView("render-warning");
    }

    protected Protein deserialise(final UrlResource urlResource) {
        if (urlResource.isReadable()) {
            InputStream is = null;
            try {
                is = urlResource.getInputStream();
                return (Protein) marshaller.unmarshal(new StreamSource(new InputStreamReader(is)));
            } catch (IOException e) {
                LOGGER.warn("Couldn't get file from specified URL resource!", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        LOGGER.error("Couldn't close URL resource input stream!", e);
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> buildModelMap(SimpleProtein p, boolean standalone) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("standalone", standalone);
        m.put("version", "5.25-64.0");
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

    /**
     * @param id
     * @return
     */
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
