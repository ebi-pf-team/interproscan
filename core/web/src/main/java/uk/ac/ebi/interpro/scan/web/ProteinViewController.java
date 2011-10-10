package uk.ac.ebi.interpro.scan.web;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.web.biomart.AnalyseMatchDataResult;
import uk.ac.ebi.interpro.scan.web.biomart.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.biomart.MatchDataResourceReader;

import java.io.IOException;
import java.util.*;

/**
 * Controller for InterPro protein view.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Controller
public class ProteinViewController {

    private static final Logger LOGGER = Logger.getLogger(ProteinViewController.class.getName());

    /**
     * Returns protein page.
     *
     * @param  id   Protein accession or MD5 checksum, for example "P38398"
     * @return Protein page
     */
    @RequestMapping(value = "/protein/{id}", method = RequestMethod.GET)
    public ModelAndView protein(@PathVariable String id) {
        return new ModelAndView("protein", "protein", retrieve(id));
    }

    /**
     * Returns protein features for inclusion in DBML
     *
     * @param  id   Protein accession or MD5 checksum, for example "P38398"
     * @return Protein features for inclusion in DBML
     */
    @RequestMapping(value = "/protein-features/{id}", method = RequestMethod.GET)
    public ModelAndView proteinFeatures(@PathVariable String id) {
        return new ModelAndView("protein-features", "protein", retrieve(id));
    }

    /**
     * Returns main body of protein page for inclusion in DBML
     *
     * @param  id   Protein accession or MD5 checksum, for example "P38398"
     * @return Main body of protein page for inclusion in DBML
     */
    @RequestMapping(value = "/protein-body/{id}", method = RequestMethod.GET)
    public ModelAndView proteinBody(@PathVariable String id) {
        return new ModelAndView("protein-body", "protein", retrieve(id));
    }

    private SimpleProtein retrieve(String id) {
        // TODO: Check if id is MD5 using regex (using Protein class code?)
        //SimpleProtein p = SimpleProtein.valueOf(sampleProtein(id));
        try {
            SimpleProtein p = queryByAccession(id);
            p.sort();
            return p;
        }
        catch (IOException e) {
            // TODO: Do not allow exception to go beyond here, otherwise user will see in browser
            throw new IllegalStateException("Could not retrieve " + id, e);
        }
    }

    /**
     * Returns protein for given accession number
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return Protein for given accession
     */
    private SimpleProtein queryByAccession(String ac) throws IOException {
        // TODO: Configure analyser via Spring context
        AnalyseMatchDataResult analyser = new AnalyseMatchDataResult(new MatchDataResourceReader());
        CreateSimpleProteinFromMatchData biomart = new CreateSimpleProteinFromMatchData(analyser);
        return biomart.queryByAccession(ac);

    }

    /**
     * Returns protein for given accession number
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return Protein for given accession
     */
    private Protein sampleProtein(String ac) {

        // Create protein
        Protein p = new Protein.Builder("MPTIKQLIRNARQPIRNVTKSPALRGCPQRRGTCTRVYTITPKKPNSALRKVARVRLTSG\n" +
                "FEITAYIPGIGHNLQEHSVVLVRGGRVKDLPGVRYHIVRGTLDAVGVKDRQQGRSKYGVK\n" +
                "KPK")
                .crossReference(new ProteinXref("UniProt", "A0A314", "RR12_COFAR", "30S ribosomal protein S12, chloroplastic"))
                .build();

        // Add matches
        Set<Hmmer3Match.Hmmer3Location> l1 = new HashSet<Hmmer3Match.Hmmer3Location>();
        l1.add(new Hmmer3Match.Hmmer3Location(1, 123, -8.9, 0.28, 63, 82, 114, 73, 94));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("G3DSA:2.40.50.140")
                        .name("Nucleic acid-binding proteins")
                        .entry(new Entry.Builder("IPR012340")
                                .description("Nucleic acid-binding, OB-fold")
                                .type(EntryType.DOMAIN)
                                .build())
                        .build(),
                -8.9, 0.28, l1));


        Entry entry = new Entry.Builder("IPR016027")
                .description("Nucleic acid-binding, OB-fold-like")
                .type(EntryType.DOMAIN)
                .build();
        Set<Hmmer3Match.Hmmer3Location> l2 = new HashSet<Hmmer3Match.Hmmer3Location>();
        l2.add(new Hmmer3Match.Hmmer3Location(2, 123, -8.9, 0.28, 63, 82, 114, 73, 94));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("SSF50249")
                        .name("Nucleic_acid_OB")
                        .entry(entry)
                        .build(),
                -8.9, 0.28, l2));
        p.addMatch(new Hmmer3Match(
                new Signature.Builder("SSF50250")
                        .name("Made up name")
                        .entry(entry)
                        .build(),
                -8.9, 0.28, l2));

        return p;

    }

    // TODO: Make top-level class in web.model package
    public final static class SimpleProtein {

        private final String ac;      // eg. P38398
        private final String id;      // BRCA1_HUMAN
        private final String name;    // eg. Breast cancer type 1 susceptibility
        private final int length;
        private final String md5;
        private final String crc64;
        private final int taxId;
        private final String taxScienceName;
        private final String taxFullName;
        private final List<SimpleEntry> entries = new ArrayList<SimpleEntry>();
        private final List<SimpleStructuralMatch> structuralMatches = new ArrayList<SimpleStructuralMatch>();

        public SimpleProtein(String ac, String id, String name, int length, String md5, String crc64,
                             int taxId, String taxScienceName, String taxFullName) {
            this.ac = ac;
            this.id = id;
            this.name = name;
            this.length = length;
            this.md5 = md5;
            this.crc64 = crc64;
            this.taxId = taxId;
            this.taxScienceName = taxScienceName;
            this.taxFullName = taxFullName;
        }

        public String getAc() {
            return ac;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getLength() {
            return length;
        }

        public String getMd5() {
            return md5;
        }

        public String getCrc64() {
            return crc64;
        }

        public int getTaxId() {
            return taxId;
        }

        public String getTaxScienceName() {
            return taxScienceName;
        }

        public String getTaxFullName() {
            return taxFullName;
        }

        public List<SimpleEntry> getEntries() {
            return entries;
        }

        /**
         * Returns a {@link SimpleProtein} from a {@link Protein}
         *
         * @param p Protein
         * @return A {@link SimpleProtein} from a {@link Protein}
         */
        public static SimpleProtein valueOf(Protein p) {
            // Get protein info
            String proteinAc   = "Unknown";
            String proteinName = "Unknown";
            String proteinDesc = "Unknown";
            if (!p.getCrossReferences().isEmpty()) {
                ProteinXref x = p.getCrossReferences().iterator().next();
                proteinAc     = x.getIdentifier();
                proteinName   = x.getName();
                proteinDesc   = x.getDescription();
            }
            SimpleProtein sp = new SimpleProtein(proteinAc, proteinName, proteinDesc, p.getSequenceLength(),
                    p.getMd5(), null, 0, null,null);// TODO Populate values properly instead of null or 0!
            // Get entries and corresponding signatures
            for (Match m : p.getMatches()) {
                // Signature
                Signature s = m.getSignature();
                String signatureAc = s.getAccession();
                SimpleSignature ss = new SimpleSignature(signatureAc, s.getName(), s.getType());
                for (Object o : m.getLocations()) {
                    Location l = (Location) o;
                    ss.getLocations().add(new SimpleLocation(l.getStart(), l.getEnd()));
                }
                // Entry
                Entry e = s.getEntry();
                SimpleEntry se = new SimpleEntry(e.getAccession(), e.getName(), e.getDescription(), e.getType().getName());
                if (sp.getEntries().contains(se))    {
                    // Entry already exists, so get it
                    se = sp.getEntries().get(sp.getEntries().indexOf(se));
                }
                else {
                    // Create new entry
                    sp.getEntries().add(se);
                }
//                if (sp.getEntriesMap().containsKey(entryAc)) {
//                    // Entry already exists
//                    se = sp.getEntriesMap().get(entryAc);
//                }
//                else {
//                    // Create new entry
//                    Entry e = s.getEntry();
//                    se = new SimpleEntry(entryAc, e.getDescription(), e.getType().getName());
//                    // Add to protein
//                    sp.getEntriesMap().put(entryAc, se);
//                }
                // Add signature to entry
                se.getSignaturesMap().put(signatureAc, ss);
            }
            for (SimpleEntry se : sp.entries) {
                // TODO: Calculate super-match start and end locations from signature matches
                if (se.getAc().equals("IPR012340")) {
                    se.getLocations().add(new SimpleLocation(1, 123));
                }
                else {
                    se.getLocations().add(new SimpleLocation(10, 30));
                    se.getLocations().add(new SimpleLocation(35, 60));
                    se.getLocations().add(new SimpleLocation(80, 110));
                }
            }
            return sp;
        }

        public void sort() {
            // Sort by entry start, and then by signature start
            Collections.sort(entries);
            for (SimpleEntry e : entries) {
                // TODO: Sort signatures
                //Collections.sort(e.signatures);
                for (SimpleSignature s : e.getSignatures()) {
                    Collections.sort(s.locations);
                }
            }
        }

    }

    public final static class SimpleEntry implements Comparable<SimpleEntry>  {

        private final String ac;
        private final String shortName;
        private final String name;
        private final String type;
        private List<SimpleLocation> locations = new ArrayList<SimpleLocation>(); // super matches
        private Map<String, SimpleSignature> signatures = new HashMap<String, SimpleSignature>();

        public SimpleEntry(String ac, String shortName, String name, String type) {
            this.ac         = ac;
            this.shortName  = shortName;
            this.name       = name;
            this.type       = type;
        }

        public String getAc() {
            return ac;
        }

        public String getShortName() {
            return shortName;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public List<SimpleLocation> getLocations() {
            return locations;
        }

        public void setLocations(List<SimpleLocation> locations) {
            this.locations = locations;
        }

        public Collection<SimpleSignature> getSignatures() {
            return signatures.values();
        }

        public Map<String, SimpleSignature> getSignaturesMap() {
            return signatures;
        }

        @Override public int compareTo(SimpleEntry that) {
            if (this == that) {
                return 0;
            }
            return Collections.min(this.locations).compareTo(Collections.min(that.locations));
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof SimpleEntry))
                return false;
            return this.ac.equals(((SimpleEntry)o).ac);
        }

    }

    public final static class SimpleSignature {

        private final String ac;
        private final String name;
        private final String type;
        private final List<SimpleLocation> locations;

        public SimpleSignature(String ac, String name, String type) {
            this.ac = ac;
            this.name = name;
            this.type = type;
            this.locations  = new ArrayList<SimpleLocation>();
        }

        public String getAc() {
            return ac;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public List<SimpleLocation> getLocations() {
            return locations;
        }

        public void addLocation(SimpleLocation location) {
            this.locations.add(location);
        }


    }

    public final static class SimpleLocation implements Comparable<SimpleLocation> {

        private final int start;
        private final int end;

        public SimpleLocation(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override public int compareTo(SimpleLocation that) {
            // Equal
            if (this == that || this.equals(that)) {
                return 0;
            }
            // Before
            if (this.getStart() < that.getStart()) {
                return -1;
            }
            // After
            if (this.getStart() > that.getStart()) {
                return 1;
            }
            // If same start, show lowest first
            if (this.getStart() == that.getStart()) {
                return this.getEnd() - that.getEnd();
            }
            // Should never get here, but...
            return this.getStart() - that.getEnd();
        }

    }

    public final static class SimpleStructuralMatch {

        private final String databaseName;
        private final String domainId;
        private final String classId;
        private final List<SimpleLocation> locations;

        public SimpleStructuralMatch(String databaseName, String domainId, String classId) {
            this.databaseName = databaseName;
            this.domainId = domainId;
            this.classId = classId;
            this.locations  = new ArrayList<SimpleLocation>();
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getDomainId() {
            return domainId;
        }

        public String getClassId() {
            return classId;
        }

        public List<SimpleLocation> getLocations() {
            return locations;
        }

        public void addLocation(SimpleLocation location) {
            this.locations.add(location);
        }


    }


}
