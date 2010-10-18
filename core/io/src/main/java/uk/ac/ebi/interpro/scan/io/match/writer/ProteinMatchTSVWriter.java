package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.GoTerm;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.InterProEntry;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.SignatureLibraryIntegratedMethods;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Write matches as output for InterProScan user.
 */
public class ProteinMatchTSVWriter implements ProteinWriter {


    private TSVWriter tsvWriter;

    DateFormat dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
    private boolean mapToInterProEntries;
    private boolean mapToGO;
    private Map<SignatureLibrary, SignatureLibraryIntegratedMethods> interProGoMapping;

    public ProteinMatchTSVWriter(File file) throws IOException {
        tsvWriter = new TSVWriter(new BufferedWriter(new FileWriter(file)));
    }


    public void write(Protein protein) throws IOException {

        String proteinAc = makeProteinAc(protein);
        int length = protein.getSequence().length();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();
        for (Match match : matches) {
            final String signatureAc = match.getSignature().getAccession();
            final SignatureLibrary signatureLibrary = match.getSignature().getSignatureLibraryRelease().getLibrary();
            final String analysis = signatureLibrary.getName();
            final String description = match.getSignature().getDescription();

            Set<Location> locations = match.getLocations();
            for (Location location : locations) {
                String score = "-";
                String status = "T";

                if (location instanceof HmmerLocation) {
                    score = Double.toString(((HmmerLocation) location).getEvalue());
                }

                final List<String> mappingFields = new ArrayList<String>();
                mappingFields.add(proteinAc);
                mappingFields.add(md5);
                mappingFields.add(Integer.toString(length));
                mappingFields.add(analysis);
                mappingFields.add(signatureAc);
                mappingFields.add(description);
                mappingFields.add(Integer.toString(location.getStart()));
                mappingFields.add(Integer.toString(location.getEnd()));
                mappingFields.add(score);
                mappingFields.add(status);
                mappingFields.add(date);


                if (mapToInterProEntries) {
                    final SignatureLibraryIntegratedMethods methodMappings = interProGoMapping.get(signatureLibrary);
                    if (methodMappings != null) {
                        final InterProEntry entry = methodMappings.getEntryByMethodAccession(signatureAc);
                        if (entry != null) {
                            mappingFields.add(entry.getEntryAccession());
                            mappingFields.add(entry.getDescription());
                            if (mapToGO && entry.getGoTerms().size() > 0) {
                                StringBuffer buf = new StringBuffer();
                                for (GoTerm goTerm : entry.getGoTerms()) {
                                    if (buf.length() > 0) {
                                        buf.append(", ");
                                    }
                                    buf.append(goTerm.getRoot().getRootName())
                                            .append(':')
                                            .append(goTerm.getTermName())
                                            .append(" (")
                                            .append(goTerm.getAccession())
                                            .append(")");
                                }
                                mappingFields.add(buf.toString());
                            }
                        }
                    }
                }

                tsvWriter.write(mappingFields);
            }
        }

    }

    @Override
    public void setMapToInterProEntries(boolean mapToInterProEntries) {
        this.mapToInterProEntries = mapToInterProEntries;
    }

    @Override
    public void setMapToGo(boolean mapToGO) {
        this.mapToGO = mapToGO;
    }

    @Override
    public void setInterProGoMapping(Map<SignatureLibrary, SignatureLibraryIntegratedMethods> interProGoMapping) {
        this.interProGoMapping = interProGoMapping;
    }

    private String makeProteinAc(Protein protein) {
        StringBuilder proteinXRef = new StringBuilder();
        Set<ProteinXref> crossReferences = protein.getCrossReferences();
        for (ProteinXref crossReference : crossReferences) {
            if (proteinXRef.length() > 0) proteinXRef.append("|");
            proteinXRef.append(crossReference.getIdentifier());
        }
        return proteinXRef.toString();
    }

    @Override
    public void close() throws IOException {
        tsvWriter.close();
    }
}
