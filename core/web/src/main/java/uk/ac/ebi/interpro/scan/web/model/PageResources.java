package uk.ac.ebi.interpro.scan.web.model;

import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a page resources,
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PageResources {

    private JavaScriptResources javaScriptResources;

    private CSSResources cssResources;

    @Required
    public void setJavaScriptResources(JavaScriptResources javaScriptResources) {
        this.javaScriptResources = javaScriptResources;
    }

    public Map<String, String> getJavaScriptResourcesMap() {
        return Collections.unmodifiableMap(javaScriptResources.getResources());
    }

    @Required
    public void setCssResources(CSSResources cssResources) {
        this.cssResources = cssResources;
    }

    public Map<String, String> getCssResourcesMap() {
        return Collections.unmodifiableMap(cssResources.getResources());
    }
}