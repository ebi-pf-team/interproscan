package uk.ac.ebi.interpro.scan.persistence;

import java.util.List;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 20-Jul-2009
 * Time: 15:29:15
 *
 * @author Phil Jones, EMBL-EBI
 */
public class XmlsForTesting {

    private List<String> xmls;

    public XmlsForTesting(List<String> xmls){
        this.xmls = xmls;
    }

    public List<String> getXmls() {
        return xmls;
    }
}
