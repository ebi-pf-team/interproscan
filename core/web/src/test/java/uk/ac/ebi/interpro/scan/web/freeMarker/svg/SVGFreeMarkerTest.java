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
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

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
        variables.put("js_resource_jquery_jscroll", "resources/javascript/jquery/jquery.jscroll.min.js");
        variables.put("img_resource_path", "resources");
        variables.put("css_resource_jquery_ui1817_custom", "resources/javascript/jquery/ui/css/ui-lightness/jquery-ui-1.8.17.custom.css");
        cfg.setAllSharedVariables(new SimpleHash(variables, new DefaultObjectWrapper()));
        //
        SimpleProtein simpleProtein = matchData.queryByAccession("Q97R95");
        SimpleHash model = buildModelMap(simpleProtein, entryHierarchy);
        //
        String templateFile = "svg-protein-view.ftl";
        Template template = cfg.getTemplate(templateFile);
        String result = writeResultToString(model, template);
        assertNotNull(result);
        assertTrue(result.contains("Q97R95"));
    }

    private String writeResultToString(SimpleHash model, Template template) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        template.process(model, out);
        out.flush();
        return out.toString();
    }


    /**
     * Writes FreeMarkers result into a file. Please note: This method is not part of the test. Use this method to see the result file.
     */
    protected void writeResultToFile(SimpleHash model, Template template) throws IOException, TemplateException {
        final String resultFilePath = "${home.dir}/projects/interproscan_svg/test/freemarker.svg";
        Writer out = new PrintWriter(new FileWriter(resultFilePath));
        template.process(model, out);
        out.flush();
    }

    private static SimpleHash buildModelMap(SimpleProtein p, EntryHierarchy entryHierarchy) {
        final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10;
        SimpleHash model = new SimpleHash();
        if (p != null) {
            model.put("protein", p);
            model.put("condensedView", new CondensedView(p));
            model.put("entryColours", entryHierarchy.getEntryColourMap());
            model.put("standalone", true);
            model.put("scale", ProteinViewHelper.generateScaleMarkers(p.getLength(), MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
        } // Else no match data was found for the protein therefore nothing to display
        return model;
    }
}