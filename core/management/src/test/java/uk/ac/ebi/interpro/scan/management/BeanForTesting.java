package uk.ac.ebi.interpro.scan.management;

import org.springframework.beans.factory.BeanNameAware;

/**
 * TODO Description of class...
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class BeanForTesting implements BeanNameAware {
    private String id;

    @Override
    public void setBeanName(String s) {
        this.id = s;
    }

    public String getId() {
        return id;
    }
}
