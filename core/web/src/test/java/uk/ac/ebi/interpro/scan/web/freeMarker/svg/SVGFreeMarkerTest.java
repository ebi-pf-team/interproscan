package uk.ac.ebi.interpro.scan.web.freeMarker.svg;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.ProteinViewHelper;
import uk.ac.ebi.interpro.scan.web.io.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.CondensedView;
import uk.ac.ebi.interpro.scan.web.model.EntryType;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests FreeMarker SVG generation.
 *
 * @author Maxim Scheremetjew
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SVGFreeMarkerTest {

    @javax.annotation.Resource
    private EntryHierarchy entryHierarchy;

    @javax.annotation.Resource
    private CreateSimpleProteinFromMatchData matchData;

    @Test
    public void testSVGFileCreation() throws IOException, TemplateException {
        //Set FreeMarker template loading directory using Springs class path resource
        String directoryForTemplateLoading = "uk/ac/ebi/interpro/scan/web/freeMarker/svg";
        Resource resource = new ClassPathResource(directoryForTemplateLoading);
        assertNotNull("Can not find template loading directory!", resource);

        //Set up FreeMarker configuration
        Configuration cfg = new Configuration();
        FileTemplateLoader loader = new FileTemplateLoader(resource.getFile());
        cfg.setTemplateLoader(loader);
        Map<String, Object> variables = new HashMap<String, Object>();
        cfg.setAllSharedVariables(new SimpleHash(variables, new DefaultObjectWrapper()));
        //Good protein test examples: Q97R95, A2T929, A0JM20 (none), P15385, A2ARV4, P01308, P22298, A2VDN9, A2YIW7
        String proteinAccession = "Q97R95";
        SimpleProtein simpleProtein = matchData.queryByAccession(proteinAccession);
        SimpleHash model = buildModelMap(simpleProtein, entryHierarchy);
        //
        String templateFile = "svg-protein-view.ftl";
        Template template = cfg.getTemplate(templateFile);
//        writeResultToFile(model, template, proteinAccession);
        String result = writeResultToString(model, template);
        assertNotNull(result);
        //Check certain key words
        assertTrue(result.contains("Q97R95"));
        assertTrue(result.contains("Length"));
        assertTrue(result.contains("Domains and repeats"));
        assertTrue(result.contains("Detailed signature matches"));
        assertTrue(result.contains("GO Term prediction"));
        //Test the existence/non-existence of certain elements and attributes in the SVG document
        //Make sure you never have the case of blobs with width zero
        assertFalse(result.contains("width=\"0px\""));
        assertTrue(result.contains("<rect class="));
        assertTrue(result.contains("<a xlink:href=\"http://www.ebi.ac.uk/interpro/entry/"));
        assertTrue(result.contains("onmouseover=\"ShowTooltip("));
    }

    private String writeResultToString(SimpleHash model, Template template) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        template.process(model, out);
        out.flush();
        return out.toString();
    }


    /**
     * Writes FreeMarkers result into a file. Please note: This method is not part of the test.
     * <p/>
     * Use this method for debugging only.
     */
    protected void writeResultToFile(SimpleHash model, Template template, String fileName) throws IOException, TemplateException {
        final String resultFilePath = "${home.dir}/{test.dir}" + fileName + ".svg";
        Writer out = new PrintWriter(new FileWriter(resultFilePath));
        template.process(model, out);
        out.flush();
    }

    private static SimpleHash buildModelMap(SimpleProtein p, EntryHierarchy entryHierarchy) {
        final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10;
        SimpleHash model = new SimpleHash();
        if (p != null) {
            final int proteinLength = p.getLength();
            final List<SimpleEntry> entries = p.getAllEntries();
            final CondensedView condensedView = new CondensedView(entries, proteinLength);
            final CondensedView condensedHSView = new CondensedView(entries, proteinLength, Arrays.asList(EntryType.HOMOLOGOUS_SUPERFAMILY));

            model.put("protein", p);
            model.put("condensedView", condensedView);
            model.put("condensedHSView", condensedHSView);
            model.put("entryColours", entryHierarchy.getEntryColourMap());
            model.put("scale", ProteinViewHelper.generateScaleMarkers(p.getLength(), MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
            model.put("svgDocumentHeight", ProteinViewHelper.calculateSVGDocumentHeight(p, condensedView, condensedHSView, 30, 180, 18, 19, 30));
        } // Else no protein data was found therefore nothing to display
        return model;
    }
}
