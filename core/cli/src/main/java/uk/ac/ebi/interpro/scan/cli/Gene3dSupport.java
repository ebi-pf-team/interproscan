package uk.ac.ebi.interpro.scan.cli;

import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.io.gene3d.CathDomainListRecord;
import uk.ac.ebi.interpro.scan.io.gene3d.Model2SfReader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Support class for running Gene3D.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class Gene3dSupport {

    // Specify either this:
    private Resource model2Sf;
    // or this:
    private ResourceReader<CathDomainListRecord> reader;
    private Resource cathDomainList;

    private final Map<String, String> map = new HashMap<String, String>();

    public String findSignatureAccession(String modelAccession) throws IOException {
        if (map.isEmpty()) {
            if (model2Sf == null) {
                // Read CathDomainList
                Collection<CathDomainListRecord> records = reader.read(cathDomainList);
                // ... and convert to map
                for (CathDomainListRecord r : records) {
                    // "DomainName" is the CATH name for model accession.
                    map.put(r.getDomainName(), r.getSignatureAccession());
                }
            } else {
                // Do it the easy way!
                Model2SfReader reader = new Model2SfReader();
                reader.setModelFiles(model2Sf);
                map.putAll(reader.parseFileToMap());
            }
        }
        return map.get(modelAccession);
    }

    public void setCathDomainList(Resource cathDomainList) {
        this.cathDomainList = cathDomainList;
    }

    public void setReader(ResourceReader<CathDomainListRecord> reader) {
        this.reader = reader;
    }

    public void setModel2Sf(Resource model2Sf) {
        this.model2Sf = model2Sf;
    }
}
