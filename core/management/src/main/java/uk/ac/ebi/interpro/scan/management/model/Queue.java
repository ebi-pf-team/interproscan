package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Queue {

    private String name;

    public String getName() {
        return name;
    }

    @Required
    public void setName(String name) {
        this.name = name;
    }
}
