package uk.ac.ebi.interpro.scan.io.superfamily.model;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.model.HmmerModelParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.util.Collections;

/**
 * Parse SuperFamily model.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SuperFamilyModelParser extends HmmerModelParser {

    private static final Logger LOGGER = Logger.getLogger(SuperFamilyModelParser.class.getName());

    @Override
    protected Signature createSignature(String accession, String name, String description, SignatureLibraryRelease release, StringBuffer modelBuf) {
        if (accession != null && !accession.startsWith("SSF")) {
            // In the SuperFamily HMM file, the accession is stored as "ACC   81321".
            // Need to prefix the accession with "SSF" to make "SSF81321" as required by InterPro!
            accession = "SSF" + accession;
        }
        Model model = new Model(accession, name, description, null);
        modelBuf.delete(0, modelBuf.length());
        return new Signature(accession, name, null, description, null, release, Collections.singleton(model));
    }


}
