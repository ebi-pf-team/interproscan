package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.HmmerLocation;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Write matches as output for InterProScan user.
 */
public class ProteinMatchTSVWriter implements ProteinWriter {

    
    private TSVWriter tsvWriter;

    DateFormat dmyFormat =new SimpleDateFormat("dd-MM-yyyy");

    public ProteinMatchTSVWriter(File file) throws IOException {
        tsvWriter=new TSVWriter(new BufferedWriter(new FileWriter(file)));
    }


    public void write(Protein protein) throws IOException {

        String proteinAc = makeProteinAc(protein);
        int length=protein.getSequence().length();
        String md5=protein.getMd5();
        String date= dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();
        for (Match match : matches) {
            String signatureAc=match.getSignature().getAccession();
            String analysis=match.getSignature().getSignatureLibraryRelease().getLibrary().getName();
            String description=match.getSignature().getDescription();

            Set<Location> locations = match.getLocations();
            for (Location location : locations) {
                String score="-";
                String status="T";

                if (location instanceof HmmerLocation) {
                    score=""+((HmmerLocation) location).getEvalue();
                }

                tsvWriter.write(proteinAc,md5,""+length,analysis,signatureAc,description,""+location.getStart(),""+location.getEnd(),score,status,date);
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
        return proteinXRef.toString();
    }

    @Override
    public void close() throws IOException {
        tsvWriter.close();
    }
}
