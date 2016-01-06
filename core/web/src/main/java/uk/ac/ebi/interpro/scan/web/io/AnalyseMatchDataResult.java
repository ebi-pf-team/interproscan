package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.model.*;

import java.util.*;


/**
 * Analyse query results and construct a more understandable
 * {@link SimpleProtein} object.
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public class AnalyseMatchDataResult {

    private static final Logger LOGGER = Logger.getLogger(AnalyseMatchDataResult.class.getName());

    private final ResourceReader<MatchDataRecord> reader;

    private final EntryHierarchy entryHierarchy;

    public AnalyseMatchDataResult(ResourceReader<MatchDataRecord> reader, EntryHierarchy entryHierarchy) {
        this.reader = reader;
        this.entryHierarchy = entryHierarchy;
    }

    public AnalyseMatchDataResult(EntryHierarchy entryHierarchy) {
        this(null, entryHierarchy);
    }

    /**
     * Load and parse a Resource, returning a populated SimpleProtein object.
     *
     * @param resource Resource to parse
     * @return The simple protein, or NULL if nothing found
     */
    public SimpleProtein parseMatchDataOutput(Resource resource) {
        /*
         * Example output:
         *
         * PROTEIN_ACCESSION	PROTEIN_ID	PROTEIN_LENGTH	CRC64	METHOD_AC	METHOD_NAME	METHOD_DATABASE_NAME	POS_FROM	POS_TO	MATCH_SCORE	ENTRY_AC	ENTRY_SHORT_NAME	ENTRY_NAME	ENTRY_TYPE	TAXONOMY_ID	TAXONOMY_SCIENCE_NAME	TAXONOMY_FULL_NAME  PROTEIN_FRAGMENT
         * P38398	BRCA1_HUMAN	1863	89C6D83FF56312AF	PS50172	BRCT	PROSITE profiles	1756	1855		IPR001357	BRCT	BRCT	Domain	9606	Homo sapiens	Homo sapiens (Human)    N
         * P38398	BRCA1_HUMAN	1863	89C6D83FF56312AF	PS00518	ZF_RING_1	PROSITE patterns	39	48		IPR017907	Znf_RING_CS	Zinc finger, RING-type, conserved site	Conserved_site	9606	Homo sapiens	Homo sapiens (Human)    N
         * P38398	BRCA1_HUMAN	1863	89C6D83FF56312AF	PF00533	BRCT	Pfam	1757	1842	1.3000000000000006908481590183164001544E-08	IPR001357	BRCT	BRCT	Domain	9606	Homo sapiens	Homo sapiens (Human)    N
         * P38398	BRCA1_HUMAN	1863	89C6D83FF56312AF	PR00493	BRSTCANCERI	PRINTS	1430	1446	2.100002678340298509737490372806090548E-92	IPR002378	Brst_cancerI	Breast cancer type I susceptibility protein	Family	9606	Homo sapiens	Homo sapiens (Human)    N
         * P38398	BRCA1_HUMAN	1863	89C6D83FF56312AF	PR00493	BRSTCANCERI	PRINTS	1771	1794	2.100002678340298509737490372806090548E-92	IPR002378	Brst_cancerI	Breast cancer type I susceptibility protein	Family	9606	Homo sapiens	Homo sapiens (Human)    N
         * P38398	BRCA1_HUMAN	1863	89C6D83FF56312AF	G3DSA:3.40.50.10190	G3DSA:3.40.50.10190	GENE3D	1648	1754	5.1000000000000426766658650783671814604E-40					9606	Homo sapiens	Homo sapiens (Human)    N
         * ...
         */
        //Read in match data from resource (InterProScan TSV output)
        Collection<MatchDataRecord> records = readInMatchDataFromResource(resource);
        //Create and return SimpleProtein object.
        return createSimpleProtein(records);
    }

    protected Collection<MatchDataRecord> readInMatchDataFromResource(Resource resource) {
        if (resource == null) {
            throw new IllegalStateException("Resource is NULL!");
        }
        // Assumption: Query results are for one specific protein accession!
        // Therefore all output relates to the same protein.
        try {
            Collection<MatchDataRecord> records = reader.read(resource);
            if (records == null || records.size() < 1) {
                LOGGER.info("No matches found in resource: " + resource.getDescription());
                return null;
            }
            return records;

        } catch (Exception e) {
            LOGGER.error("Could not read from query resource: " + resource.getDescription());
            return null;
        }
    }

    /**
     * Convert a collection of {@link MatchDataRecord} objects
     * into a {@link SimpleProtein} object using necessary
     * business logic.
     * Note that the {@link SimpleProtein} is incomplete as in there is no structural match data attached, that can
     * be done afterwards.
     *
     * @param records a Collection of MatchDataRecord objects
     * @return The simple protein, or NULL if nothing found
     */
    public SimpleProtein createSimpleProtein(Collection<MatchDataRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }
        SimpleProtein protein = null;
        for (MatchDataRecord record : records) {
            // Loop through query output one line at a time

            if (protein == null) {
                // First line of the query results, so we'll need to initialise the SimpleProtein
                protein = new SimpleProtein(
                        record.getProteinAc(),
                        record.getProteinId(),
                        record.getProteinDescription(),
                        record.getProteinLength(),
                        "N/A",
                        record.getCrc64(),
                        record.getTaxId(),
                        record.getTaxScienceName(),
                        record.getTaxFullName(),
                        record.isProteinFragment());
            }

            final String methodAc = record.getMethodAc();
            final String methodName = record.getMethodName();
            final String methodDatabase = record.getMethodDatabase();
            final String entryAc = record.getEntryAc();
            final String entryShortName = record.getEntryShortName();
            final String entryName = record.getEntryName();
            final String entryTypeString = record.getEntryType();
            final EntryType entryType = EntryType.parseName(entryTypeString);
            if (entryType == null) {
                throw new IllegalStateException("Cannot convert entry type String " + entryTypeString + " to an EntryType object.");
            }

            // Need to eventually associate this match location with the existing SimpleProtein object
            SimpleLocation location = new SimpleLocation(record.getPosFrom(), record.getPosTo());

            // Has this entry already been added to the protein?
            List<SimpleEntry> entries = protein.getAllEntries();
            SimpleEntry newEntry = new SimpleEntry(entryAc, entryShortName, entryName, entryType, entryHierarchy);
            if (entries.contains(newEntry)) {
                // Entry already exists
                SimpleEntry entry = entries.get(entries.indexOf(newEntry));

                // Has this signature already been added to the entry?
                Map<String, SimpleSignature> signatures = entry.getSignaturesMap();
                if (signatures.containsKey(methodAc)) {
                    // Signature already exists
                    SimpleSignature signature = signatures.get(methodAc);
                    signature.addLocation(location);
                } else {
                    // New signature for this entry, add it to the map
                    SimpleSignature signature =
                            new SimpleSignature(methodAc, methodName, methodDatabase);
                    signature.addLocation(location);
                    signatures.put(methodAc, signature);
                }

            } else {
                // New entry for this protein, add it to the map
                SimpleEntry entry = new SimpleEntry(entryAc, entryShortName, entryName, entryType, entryHierarchy);
                SimpleSignature signature =
                        new SimpleSignature(methodAc, methodName, methodDatabase);
                signature.addLocation(location);
                entry.getSignaturesMap().put(methodAc, signature);
                entries.add(entry);
            }
        }

        if (protein == null) {
            throw new IllegalStateException("Protein is still NULL - no parsable data present?");
        }
        List<SimpleEntry> entries = protein.getAllEntries();
        for (SimpleEntry entry : entries) {
            if (!entry.isIntegrated()) {
                // Un-integrated signatures do not have supermatches
                continue;
            }
            List<SimpleLocation> locations = new ArrayList<SimpleLocation>();
            Map<String, SimpleSignature> signatures = entry.getSignaturesMap();
            for (SimpleSignature signature : signatures.values()) {
                locations.addAll(signature.getLocations());
            }
            if (locations.size() > 0) {
                Collections.sort(locations);
            }
            entry.setLocations(locations);
        }
        return protein;
    }


}
