package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.ProteinViewController;

import java.io.IOException;
import java.util.*;


/**
 * Analyse BioMart query results and construct a more understandable
 * {@link uk.ac.ebi.interpro.scan.web.ProteinViewController.SimpleProtein} object.
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
public class AnalyseBioMartQueryResult {

    private static final Logger LOGGER = Logger.getLogger(AnalyseBioMartQueryResult.class.getName());

    private final ResourceReader<BioMartQueryRecord> reader;

    public AnalyseBioMartQueryResult(ResourceReader<BioMartQueryRecord> reader) {
        this.reader = reader;
    }

    /**
     * Convert a collection of {@link uk.ac.ebi.interpro.scan.web.biomart.BioMartQueryRecord} objects
     * into a {@link uk.ac.ebi.interpro.scan.web.ProteinViewController.SimpleProtein} object using necessary
     * business logic.
     *
     * @return The simple protein
     */
    public ProteinViewController.SimpleProtein parseBioMartQueryOutput(Resource resource) {
        ProteinViewController.SimpleProtein protein = null;
        String queryOutputText = "";
        String line = "";

        // Example output:
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	G3DSA:3.30.40.10	Znf_RING/FYVE/PHD	GENE3D	10	85	2.7e-19	IPR013083	Znf_RING/FYVE/PHD	Zinc finger, RING/FYVE/PHD-type	Domain
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	G3DSA:3.40.50.10190	G3DSA:3.40.50.10190	GENE3D	1648	1754	5.1e-40
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	G3DSA:3.40.50.10190	G3DSA:3.40.50.10190	GENE3D	1756	1858	0
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	PB005611	Pfam-B_5611	PfamB	115	238	5.9e-25
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	PB005611	Pfam-B_5611	PfamB	231	285	5.6e-06
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	PB005611	Pfam-B_5611	PfamB	281	633	7.60064e-42
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	PB005611	Pfam-B_5611	PfamB	640	925	1.6e-09
        // P38398	BRCA1_HUMAN	E40F752DEDF675E2F7C99142EBB2607A	PB005611	Pfam-B_5611	PfamB	995	1196	0
        // ...

        String proteinAc = null;
        String proteinName = null;
        Collection<BioMartQueryRecord> records = null;

        try {
            records = reader.read(resource);
        } catch (IOException e) {
            LOGGER.error("Could not read from BioMart query resource: " + resource.getDescription());
            e.printStackTrace();
            return null;
        }

        // Assumption: Query results are for one specific protein accession!
        // Therefore all BioMart output relates to the same protein.

        for (BioMartQueryRecord record : records) {
            // Loop through BioMart query output one line at a time

            if (protein == null) {
                // First line of the query results, so we'll need to initialise the SimpleProtein
                proteinAc = record.getProteinAc();
                proteinName = record.getProteinName();
                int DUMMY_SEQUENCE_LENGTH = 1863;
                protein = new ProteinViewController.SimpleProtein(proteinAc, "id", proteinName, DUMMY_SEQUENCE_LENGTH); // TODO ID AND SEQ LEN!
            }

            String methodAc = record.getMethodAc();
            String methodName = record.getMethodName();
            String methodType = record.getMethodType();
            Integer posFrom = record.getPosFrom();
            Integer posTo = record.getPosTo();
            String entryAc = record.getEntryAc();
            String entryName = record.getEntryName();
            String entryType = record.getEntryType();

            // Need to eventually associate this match location with the exiting SimpleProtein object
            ProteinViewController.SimpleLocation location = new ProteinViewController.SimpleLocation(posFrom, posTo);

            // Has this entry already been added to the protein?
            List<ProteinViewController.SimpleEntry> entries = protein.getEntries();
            ProteinViewController.SimpleEntry newEntry = new ProteinViewController.SimpleEntry(entryAc, entryName, entryType);
            if (entries.contains(newEntry)) {
                // Entry already exists
                ProteinViewController.SimpleEntry entry = entries.get(entries.indexOf(newEntry));

                // Has this signature already been added to the entry?
                Map<String, ProteinViewController.SimpleSignature> signatures = entry.getSignaturesMap();
                if (signatures.containsKey(methodAc)) {
                    // Signature already exists
                    ProteinViewController.SimpleSignature signature = signatures.get(methodAc);
                    signature.addLocation(location);
                }
                else {
                    // New signature for this entry, add it to the map
                    ProteinViewController.SimpleSignature signature = new ProteinViewController.SimpleSignature(methodAc, methodName, methodType);
                    signature.addLocation(location);
                    signatures.put(methodAc, signature);
                }

            }
            else {
                // New entry for this protein, add it to the map
                ProteinViewController.SimpleEntry entry = new ProteinViewController.SimpleEntry(entryAc, entryName, entryType);
                ProteinViewController.SimpleSignature signature = new ProteinViewController.SimpleSignature(methodAc, methodName, methodType);
                signature.addLocation(location);
                entry.getSignaturesMap().put(methodAc, signature);
                entries.add(entry);
            }

            queryOutputText += line + "\n";

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("BioMart query returned:\n" + queryOutputText);
        }

        // Start to calculate the supermatches for each entry
        // TODO What about for un-integrated signatures? All have entryAc "Unintegrated"...
        List<ProteinViewController.SimpleEntry> entries = protein.getEntries();
        for (ProteinViewController.SimpleEntry entry : entries) {
            List<ProteinViewController.SimpleLocation> superLocations = new ArrayList<ProteinViewController.SimpleLocation>();
            List<ProteinViewController.SimpleLocation> locations = new ArrayList<ProteinViewController.SimpleLocation>();
            Map<String, ProteinViewController.SimpleSignature> signatures = entry.getSignaturesMap();
            for (ProteinViewController.SimpleSignature signature: signatures.values()) {
                locations.addAll(signature.getLocations());
            }
            Collections.sort(locations); // Ordered list of all locations for this entry
            Integer superPosStart = null;
            Integer superPosEnd = null;
            for (ProteinViewController.SimpleLocation location : locations) {
                // Loop through locations, ordered by start position then end position (ascending)
                if (superPosStart == null) {
                    // Looking at the first location
                    superPosStart = location.getStart();
                    superPosEnd = location.getEnd();
                    continue;
                }
                int posStart = location.getStart();
                int posEnd = location.getEnd();
                if (posStart < superPosEnd) {
                    // This match overlaps with the current supermatch under construction, so incorporate this match
                    if (posEnd > superPosEnd) {
                        superPosEnd = posEnd;
                    }
                }
                else {
                    // Doesn't overlap with the current supermatch under construction, so can add that supermatch and
                    // begin constructing the next one.
                    superLocations.add(new ProteinViewController.SimpleLocation(superPosStart, superPosEnd));
                    superPosStart = posStart;
                    superPosEnd = posEnd;
                }
            }
            superLocations.add(new ProteinViewController.SimpleLocation(superPosStart, superPosEnd)); // Don't forget the final supermatch
            Collections.sort(superLocations);
            entry.setLocations(superLocations); // Add the supermatches to this entry
        }

        return protein;
    }

}
