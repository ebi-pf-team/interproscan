package uk.ac.ebi.interpro.scan.io.gene3d;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 08/09/11
 * Time: 14:37
 */
public class SsfFileWriter {

    public void writeSsfFile(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins, String ssfInputFilePath) {
        // Generate SSF file
        final Collection<DomainFinderRecord> records = new ArrayList<DomainFinderRecord>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins) {
            for (Gene3dHmmer3RawMatch m : p.getMatches()) {
                records.add(DomainFinderRecord.valueOf(m));
            }
        }
        final Resource ssfFile = new FileSystemResource(ssfInputFilePath);
        final DomainFinderResourceWriter writer = new DomainFinderResourceWriter();
        try {
            writer.write(ssfFile, records);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
