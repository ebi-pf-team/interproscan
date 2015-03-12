package uk.ac.ebi.interpro.scan.web.model;

import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * Represents a set of JavaScript resources.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class JavaScriptResources {

    private Map<String, String> resources;

    public Map<String, String> getResources() {
        return resources;
    }

    @Required
    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }
}
