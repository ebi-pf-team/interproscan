package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.ProteinViewController;

import java.io.IOException;
import java.util.*;


/**
 * Analyse query results and construct a more understandable
 * {@link uk.ac.ebi.interpro.scan.web.ProteinViewController.SimpleProtein} object.
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
public class AnalyseMatchDataResult {

    private static final Logger LOGGER = Logger.getLogger(AnalyseMatchDataResult.class.getName());

    private final ResourceReader<MatchDataRecord> reader;

    public AnalyseMatchDataResult(ResourceReader<MatchDataRecord> reader) {
        this.reader = reader;
    }

    /**
     * Convert a collection of {@link MatchDataRecord} objects
     * into a {@link uk.ac.ebi.interpro.scan.web.ProteinViewController.SimpleProtein} object using necessary
     * business logic.
     *
     * @param resource Resource to parse
     * @return The simple protein
     */
    public ProteinViewController.SimpleProtein parseMatchDataOutput(Resource resource) {
        ProteinViewController.SimpleProtein protein = null;
        String queryOutputText = "";
        String line = "";

        /*
         * Example output:
         *
         * PROTEIN_ACCESSION	PROTEIN_ID	PROTEIN_LENGTH	MD5	CRC64	METHOD_AC	METHOD_NAME	METHOD_DATABASE_NAME	POS_FROM	POS_TO	MATCH_SCORE	ENTRY_AC	ENTRY_SHORT_NAME	ENTRY_NAME	ENTRY_TYPE	TAXONOMY_ID	TAXONOMY_SCIENCE_NAME	TAXONOMY_FULL_NAME
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	PS50172	BRCT	PROSITE profiles	1756	1855		IPR001357	BRCT	BRCT	Domain	9606	Homo sapiens	Homo sapiens (Human)
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	PS00518	ZF_RING_1	PROSITE patterns	39	48		IPR017907	Znf_RING_CS	Zinc finger, RING-type, conserved site	Conserved_site	9606	Homo sapiens	Homo sapiens (Human)
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	PB005611	Pfam-B_5611	PfamB	115	238	5.9000000000000110005130767365509328264E-25					9606	Homo sapiens	Homo sapiens (Human)
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	PF00533	BRCT	Pfam	1757	1842	1.3000000000000006908481590183164001544E-08	IPR001357	BRCT	BRCT	Domain	9606	Homo sapiens	Homo sapiens (Human)
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	PR00493	BRSTCANCERI	PRINTS	1430	1446	2.100002678340298509737490372806090548E-92	IPR002378	Brst_cancerI	Breast cancer type I susceptibility protein	Family	9606	Homo sapiens	Homo sapiens (Human)
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	PR00493	BRSTCANCERI	PRINTS	1771	1794	2.100002678340298509737490372806090548E-92	IPR002378	Brst_cancerI	Breast cancer type I susceptibility protein	Family	9606	Homo sapiens	Homo sapiens (Human)
         * P38398	BRCA1_HUMAN	1863	E40F752DEDF675E2F7C99142EBB2607A	89C6D83FF56312AF	G3DSA:3.40.50.10190	G3DSA:3.40.50.10190	GENE3D	1648	1754	5.1000000000000426766658650783671814604E-40					9606	Homo sapiens	Homo sapiens (Human)
         * ...
         */

        String proteinAc;
        String proteinId;
        int proteinLength;
        String md5;
        String crc64;
        int taxId;
        String taxScienceName;
        String taxFullName;
        Collection<MatchDataRecord> records;

        try {
            records = reader.read(resource);
        } catch (IOException e) {
            LOGGER.error("Could not read from query resource: " + resource.getDescription());
            e.printStackTrace();
            return null;
        }

        // Assumption: Query results are for one specific protein accession!
        // Therefore all output relates to the same protein.

        for (MatchDataRecord record : records) {
            // Loop through query output one line at a time

            if (protein == null) {
                // First line of the query results, so we'll need to initialise the SimpleProtein
                proteinAc = record.getProteinAc();
                proteinId = record.getProteinId();
                proteinLength = record.getProteinLength();
                md5 = record.getMd5();
                crc64 = record.getCrc64();
                taxId = record.getTaxId();
                taxScienceName = record.getTaxScienceName();
                taxFullName = record.getTaxFullName();
                protein = new ProteinViewController.SimpleProtein(proteinAc, proteinId, "Name not available",
                        proteinLength, md5, crc64, taxId, taxScienceName, taxFullName);
            }

            String methodAc = record.getMethodAc();
            String methodName = record.getMethodName();
            String methodType = record.getMethodType();
            Integer posFrom = record.getPosFrom();
            Integer posTo = record.getPosTo();
            Double score = record.getScore();
            String entryAc = record.getEntryAc();
            String entryShortName = record.getEntryShortName();
            String entryName = record.getEntryName();
            String entryType = record.getEntryType();

            // Need to eventually associate this match location with the existing SimpleProtein object
            // TODO Set score against location? Could be double or NULL, e.g. PROSITE PROFILES
            ProteinViewController.SimpleLocation location = new ProteinViewController.SimpleLocation(posFrom, posTo);

            // Has this entry already been added to the protein?
            List<ProteinViewController.SimpleEntry> entries = protein.getEntries();
            ProteinViewController.SimpleEntry newEntry = new ProteinViewController.SimpleEntry(entryAc, entryShortName, entryName, entryType);
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
                ProteinViewController.SimpleEntry entry = new ProteinViewController.SimpleEntry(entryAc, entryShortName, entryName, entryType);
                ProteinViewController.SimpleSignature signature = new ProteinViewController.SimpleSignature(methodAc, methodName, methodType);
                signature.addLocation(location);
                entry.getSignaturesMap().put(methodAc, signature);
                entries.add(entry);
            }

            queryOutputText += line + "\n";

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Query returned:\n" + queryOutputText);
        }

        // Start to calculate the supermatches for each entry
        List<ProteinViewController.SimpleEntry> entries = protein.getEntries();
        for (ProteinViewController.SimpleEntry entry : entries) {
            if (entry.getAc() == null || entry.getAc().equals("")) {
                // Un-integrated signatures do not have supermatches
                continue;
            }
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
