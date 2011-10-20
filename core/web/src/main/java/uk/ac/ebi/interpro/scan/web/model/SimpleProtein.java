package uk.ac.ebi.interpro.scan.web.model;

import uk.ac.ebi.interpro.scan.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SimpleProtein {

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

    public List<SimpleStructuralMatch> getStructuralMatches() {
        return structuralMatches;
    }

    /* Convenience filter methods for JSPs: */

    public List<SimpleStructuralMatch> getStructuralFeatures() {
        final List<SimpleStructuralMatch> features = new ArrayList<SimpleStructuralMatch>();
        for(SimpleStructuralMatch m : structuralMatches) {
            if (MatchDataSources.isStructuralFeature(m.getDatabaseName())) {
                features.add(m);
            }
        }
        return features;
    }    
    
    public List<SimpleStructuralMatch> getStructuralPredictions() {
        final List<SimpleStructuralMatch> features = new ArrayList<SimpleStructuralMatch>();
        for(SimpleStructuralMatch m : structuralMatches) {
            if (MatchDataSources.isStructuralPrediction(m.getDatabaseName())) {
                features.add(m);
            }
        }
        return features;
    }

    public void sort() {
        // Sort by entry start, and then by signature start
        Collections.sort(entries);
        for (SimpleEntry e : entries) {
            // TODO: Sort signatures
            //Collections.sort(e.signatures);
            for (SimpleSignature s : e.getSignatures()) {
                s.sort();
            }
        }
    }    

    /**
     * Returns a {@link SimpleProtein} from a {@link uk.ac.ebi.interpro.scan.model.Protein}
     *
     * @param p Protein
     * @return A {@link SimpleProtein} from a {@link uk.ac.ebi.interpro.scan.model.Protein}
     */
    public static SimpleProtein valueOf(Protein p) {
        // Get protein info
        String proteinAc = "Unknown";
        String proteinName = "Unknown";
        String proteinDesc = "Unknown";
        if (!p.getCrossReferences().isEmpty()) {
            ProteinXref x = p.getCrossReferences().iterator().next();
            proteinAc = x.getIdentifier();
            proteinName = x.getName();
            proteinDesc = x.getDescription();
        }
        SimpleProtein sp = new SimpleProtein(proteinAc, proteinName, proteinDesc, p.getSequenceLength(),
                p.getMd5(), null, 0, null, null);// TODO Populate values properly instead of null or 0!
        // Get entries and corresponding signatures
        for (Match m : p.getMatches()) {
            // Signature
            Signature s = m.getSignature();
            String signatureAc = s.getAccession();
            SimpleSignature ss = new SimpleSignature(signatureAc, s.getName(), s.getSignatureLibraryRelease().getLibrary().getName());
            for (Object o : m.getLocations()) {
                Location l = (Location) o;
                ss.getLocations().add(new SimpleLocation(l.getStart(), l.getEnd()));
            }
            // Entry
            Entry e = s.getEntry();
            SimpleEntry se = new SimpleEntry(e.getAccession(), e.getName(), e.getDescription(), e.getType().getName());
            if (sp.getEntries().contains(se)) {
                // Entry already exists, so get it
                se = sp.getEntries().get(sp.getEntries().indexOf(se));
            } else {
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
            } else {
                se.getLocations().add(new SimpleLocation(10, 30));
                se.getLocations().add(new SimpleLocation(35, 60));
                se.getLocations().add(new SimpleLocation(80, 110));
            }
        }
        return sp;
    }

}