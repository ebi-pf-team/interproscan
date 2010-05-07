package uk.ac.ebi.interpro.scan.io.match;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.Location;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Xref;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.io.*;
import java.util.Set;                                           

/**
 * Write matches as output for InterProScan user.
 */
public class MatchWriter implements Closeable {
    private TSVWriter tsvWriter;

    public MatchWriter(File file) throws IOException {
        tsvWriter=new TSVWriter(new BufferedWriter(new FileWriter(file)));
    }

    
    public void write(Protein protein) throws IOException {

        String proteinAc = makeProteinAc(protein);

        Set<Match> matches = protein.getMatches();
        for (Match match : matches) {
            String signatureAc=match.getSignature().getAccession();
            Set<Location> locations = match.getLocations();
            for (Location location : locations) {
                tsvWriter.write(proteinAc,signatureAc,""+location.getStart(),""+location.getEnd());
            }
        }

    }

    private String makeProteinAc(Protein protein) {
        StringBuilder proteinXRef=new StringBuilder();
        Set<Xref> crossReferences = protein.getCrossReferences();
        for (Xref crossReference : crossReferences) {
            if (proteinXRef.length()>0) proteinXRef.append("|");
            proteinXRef.append(crossReference.getIdentifier());
        }
        String proteinAc=proteinXRef.toString();
        return proteinAc;
    }

    @Override
    public void close() throws IOException {
        tsvWriter.close();
    }
}
