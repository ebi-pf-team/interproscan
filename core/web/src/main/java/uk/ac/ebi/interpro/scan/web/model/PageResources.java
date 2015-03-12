package uk.ac.ebi.interpro.scan.web.model;

import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents page resources like CSS, JavaScript OR images,
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PageResources {

    private JavaScriptResources javaScriptResources;

    private CSSResources cssResources;

    private ImageResources imageResources;

    @Required
    public void setJavaScriptResources(JavaScriptResources javaScriptResources) {
        this.javaScriptResources = javaScriptResources;
    }

    @Required
    public void setCssResources(CSSResources cssResources) {
        this.cssResources = cssResources;
    }

    @Required
    public void setImageResources(ImageResources imageResources) {
        this.imageResources = imageResources;
    }

    public Map<String, String> getResourcesMap() {
        Map<String, String> result = new HashMap<String, String>();
        result.putAll(cssResources.getResources());
        result.putAll(javaScriptResources.getResources());
        result.putAll(imageResources.getResources());
        return Collections.unmodifiableMap(result);
    }
}
