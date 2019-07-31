package uk.ac.ebi.interpro.scan.web.freemarker;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.context.support.XmlWebApplicationContext;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.web.ProteinViewHelper;
import uk.ac.ebi.interpro.scan.web.io.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.CondensedView;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple main class which processes FreeMarker templates.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FreemarkerTest {

    public static void main(String[] args) throws Exception {
        String directoryForTemplateLoading = "../WEB-INF/freemarker";
        String pathToAppContextFile = "file:../WEB-INF/spring/app-config.xml";
        String resultFilePath = "<home.dir>/test/freemarker.html";
        String testView = "views/protein-structure.ftl";

        Configuration cfg = new Configuration();
        FileTemplateLoader loader = new FileTemplateLoader(new File(directoryForTemplateLoading));
        cfg.setTemplateLoader(loader);
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("css_resource_jquery_qtip2", "resources/javascript/qtip2/jquery.qtip.css");
        variables.put("css_resource_protein", "resources/css/protein.css");
        variables.put("css_resource_type_colours", "resources/css/type_colours.css");
        variables.put("css_resource_database", "resources/css/database.css");
        variables.put("js_resource_jquery171", "resources/javascript/jquery/jquery-1.7.1.min.js");
        variables.put("js_resource_jquery_qtip2", "resources/javascript/qtip2/jquery.qtip.min.js");
        variables.put("js_resource_protein", "resources/javascript/protein.js");
        variables.put("js_resource_jquery_jscroll", "resources/javascript/jquery/jquery.jscroll.min.js");
        variables.put("img_resource_path", "resources");
        variables.put("js_resource_common", "resources/javascript/common.js");
        variables.put("js_resource_protein_popups", "resources/javascript/protein-popups.js");
        variables.put("js_resource_jquery_ui1817_custom", "resources/javascript/jquery/jquery-ui-1.8.17.custom.min.js");
        variables.put("js_resource_protein_jquery_cookie", "resources/javascript/jquery/jquery.cookie.js");
        cfg.setAllSharedVariables(new SimpleHash(variables, new DefaultObjectWrapper()));

        final AbstractApplicationContext ctx = new XmlWebApplicationContext();
        ((XmlWebApplicationContext) ctx).setConfigLocation(pathToAppContextFile);
//        Get beans from context file
        EntryHierarchy entryHierarchy = (EntryHierarchy) ctx.getBean("entryHierarchy");
        Jaxb2Marshaller marshaller = (Jaxb2Marshaller) ctx.getBean("jaxb2");
        CreateSimpleProteinFromMatchData matchData = (CreateSimpleProteinFromMatchData) ctx.getBean("matchData");

        SimpleProtein simpleProtein = matchData.queryByAccession("Q97R95");

        SimpleHash model = buildModelMap(simpleProtein, entryHierarchy);

        Template temp = cfg.getTemplate(testView);
        try (Writer out = Files.newBufferedWriter(Paths.get(resultFilePath))) {
            temp.process(model, out);
            out.flush();
        }
    }

    private static SimpleHash buildModelMap(SimpleProtein p, EntryHierarchy entryHierarchy) {
        final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10;
        SimpleHash model = new SimpleHash();
        if (p != null) {
            final int proteinLength = p.getLength();
            final List<SimpleEntry> entries = p.getAllEntries();
            final CondensedView condensedView = new CondensedView(entries, proteinLength);

            model.put("protein", p);
            model.put("condensedView", condensedView);
            model.put("entryColours", entryHierarchy.getEntryColourMap());
            model.put("standalone", true);
            model.put("scale", ProteinViewHelper.generateScaleMarkers(p.getLength(), MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
        } // Else no protein data was found therefore nothing to display
        return model;
    }

    public static SimpleProtein getSimpleProteinFromXMLFile(Jaxb2Marshaller marshaller,
                                                            EntryHierarchy entryHierarchy) throws IOException {
        Resource resource = new ClassPathResource("protein-freemarker.xml");
        Protein protein = (Protein) marshaller.unmarshal(new StreamSource(resource.getInputStream()));
        return SimpleProtein.valueOf(protein, protein.getCrossReferences().iterator().next(), entryHierarchy);
    }
}
