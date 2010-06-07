package uk.ac.ebi.interpro.scan.io.prints;

/**
 * Parse model object, used to hold data from the PRINTS kdat file, prior to parsing
 * the PRINTS pval file.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class KdatSignatureData {

    private String name;

    private String printsAbstract;

    public KdatSignatureData(String name, String printsAbstract) {
        this.name = name;
        this.printsAbstract = printsAbstract;
    }

    public String getName() {
        return name;
    }

    public String getPrintsAbstract() {
        return printsAbstract;
    }
}
