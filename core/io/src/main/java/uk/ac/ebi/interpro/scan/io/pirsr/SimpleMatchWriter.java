package uk.ac.ebi.interpro.scan.io.pirsr;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceWriter;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.PIRSRHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class SimpleMatchWriter {

        public void writeSimpleMatchFile(Set<RawProtein<PIRSRHmmer3RawMatch>> rawProteins, String pirsrInputFilePath) {
            // Generate simple Match file
            final Collection<String> records = new ArrayList<>();
            for (RawProtein<PIRSRHmmer3RawMatch> p : rawProteins) {
                for (PIRSRHmmer3RawMatch match : p.getMatches()) {
                    StringBuilder output = new StringBuilder()
                            .append(match.getId()).append("\t")
                            .append(match.getModelId()).append("\t")
                            .append(match.getLocationStart()).append("\t")
                            .append(match.getLocationEnd()).append("\t");

                    records.add(output.toString());
                }
            }
            final Resource simpleMatchFile = new FileSystemResource(pirsrInputFilePath);
            final PIRSRResourceWriter writer = new PIRSRResourceWriter();
            try {
                writer.write(simpleMatchFile, records);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        }
}
