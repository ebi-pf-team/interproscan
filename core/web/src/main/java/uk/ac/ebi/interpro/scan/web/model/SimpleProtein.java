package uk.ac.ebi.interpro.scan.web.model;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.GoTerm;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.io.FamilyHierachyElementBuilder;

import java.io.Serializable;
import java.util.*;

public final class SimpleProtein implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SimpleProtein.class.getName());

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
    private Set<SimpleEntry> familyEntries = null;
    private final List<SimpleStructuralDatabase> structuralDatabases = new ArrayList<SimpleStructuralDatabase>();


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

    public List<SimpleEntry> getAllEntries() {
        return entries;
    }

    public List<SimpleStructuralDatabase> getStructuralDatabases() {
        return structuralDatabases;
    }

    /* Convenience filter methods for JSPs: */

    // A tautology really -- all entries have integrated signatures! -- so actually better to make this separation
    // when the SimpleProtein is created: entries collection only has "true" entries, unintegrated signatures can
    // be in signatures collection. Same for structural features and predictions. Protein class would then be:
    // protein.entries, protein.signatures, protein.structuralFeatures, protein.structuralDatabases
    // ... with no need for these filters.

    public List<SimpleEntry> getEntries() {
        final List<SimpleEntry> entries = new ArrayList<SimpleEntry>();
        for (SimpleEntry entry : this.entries) {
            if (entry.isIntegrated()) {
                entries.add(entry);
            }
        }
        Collections.sort(entries);
        return entries;
    }

    /**
     * Returns a Set containing the family SimpleEntry's for this Protein. (Lazy creation of Set)
     *
     * @return a Set containing the family SimpleEntry's for this Protein.
     */
    public Set<SimpleEntry> getFamilyEntries() {
        if (familyEntries == null) {
            familyEntries = new HashSet<SimpleEntry>();
            for (SimpleEntry entry : entries) {
                if (entry.isIntegrated() && "family".equalsIgnoreCase(entry.getType())) {
                    familyEntries.add(entry);
                }
            }
        }
        return familyEntries;
    }

    public Set<GoTerm> getGoTerms(GoTerm.GoRoot root) {
        final List<SimpleEntry> entries = this.getEntries();
        if (entries == null || entries.size() == 0) {
            return Collections.emptySet();
        }
        final Set<GoTerm> terms = new TreeSet<GoTerm>();
        for (final SimpleEntry entry : entries) {
            for (final GoTerm term : entry.getGoTerms()) {
                if (term.getRoot() == root) {
                    terms.add(term);
                }
            }
        }
        return terms;
    }

    private Set<GoTerm> processGoTerms;

    private Set<GoTerm> componentGoTerms;

    private Set<GoTerm> functionGoTerms;


    public Set<GoTerm> getProcessGoTerms() {
        return getGoTerms(GoTerm.GoRoot.BIOLOGICAL_PROCESS);
    }

    public Set<GoTerm> getComponentGoTerms() {
        return getGoTerms(GoTerm.GoRoot.CELLULAR_COMPONENT);
    }

    public Set<GoTerm> getFunctionGoTerms() {
        return getGoTerms(GoTerm.GoRoot.MOLECULAR_FUNCTION);
    }


    public List<SimpleSignature> getUnintegratedSignatures() {
        final List<SimpleSignature> signatures = new ArrayList<SimpleSignature>();
        for (SimpleEntry entry : this.entries) {
            if (!entry.isIntegrated()) {
                signatures.addAll(entry.getSignatures());
            }
        }
        Collections.sort(signatures);
        return signatures;
    }

    public List<SimpleStructuralDatabase> getStructuralFeatures() {
        final List<SimpleStructuralDatabase> features = new ArrayList<SimpleStructuralDatabase>();
        for (SimpleStructuralDatabase db : this.structuralDatabases) {
            if (MatchDataSource.isStructuralFeature(db.getDataSource())) {
                features.add(db);
            }
        }
        Collections.sort(features);
        return features;
    }

    public List<SimpleStructuralDatabase> getStructuralPredictions() {
        final List<SimpleStructuralDatabase> predictions = new ArrayList<SimpleStructuralDatabase>();
        for (SimpleStructuralDatabase db : this.structuralDatabases) {
            if (MatchDataSource.isStructuralPrediction(db.getDataSource())) {
                predictions.add(db);
            }
        }
        Collections.sort(predictions);
        return predictions;
    }

    /**
     * Returns a {@link SimpleProtein} from a {@link uk.ac.ebi.interpro.scan.model.Protein}
     *
     * @param protein        Protein
     * @param entryHierarchy Entry hierarchy
     * @return A {@link SimpleProtein} from a {@link uk.ac.ebi.interpro.scan.model.Protein}
     */
    public static SimpleProtein valueOf(Protein protein, EntryHierarchy entryHierarchy) {
        if (entryHierarchy == null) {
            throw new IllegalArgumentException("SimpleProtein.valueOf method: the EntryHierarchy parameter must not be null.");
        }
        // Get protein info
        String proteinAc = "Unknown";
        String proteinName = "Unknown";
        String proteinDesc = "Unknown";
        //TODO: Check if it is possible to grab these information from somewhere (especially if we run I5 standalone)
        String crc64 = "Unknown";
        String taxScienceName = "Unknown";
        String taxFullName = "Unknown";
        if (!protein.getCrossReferences().isEmpty()) {
            ProteinXref xref = protein.getCrossReferences().iterator().next();
            proteinAc = xref.getIdentifier();
            if (xref.getName() != null) {
                proteinName = xref.getName();
            }
            if (xref.getDescription() != null) {
                proteinDesc = xref.getDescription();
            }
        }
        SimpleProtein simpleProtein = new SimpleProtein(proteinAc, proteinName, proteinDesc, protein.getSequenceLength(),
                protein.getMd5(), crc64, 0, taxScienceName, taxFullName);
        // Get entries and corresponding signatures
        for (Match match : protein.getMatches()) {
            // Signature
            Signature signature = match.getSignature();
            String signatureAc = signature.getAccession();
            String signatureName = signature.getName();
            if (signatureName == null || signatureName.length() == 0) {
                signatureName = signatureAc;
            }

            // Entry
            Entry entry = signature.getEntry();

            SimpleEntry simpleEntry;
//            if (e != null) {
            if (entry == null) {
                simpleEntry = new SimpleEntry("", SimpleEntry.UNINTEGRATED, SimpleEntry.UNINTEGRATED, "", entryHierarchy);
            } else {
                simpleEntry = new SimpleEntry(entry.getAccession(), entry.getName(), entry.getDescription(), entry.getType().getName(), entryHierarchy);
            }
            // Add the locations to the Entry from the Signatures
            SimpleSignature ss = new SimpleSignature(signatureAc, signatureName, signature.getSignatureLibraryRelease().getLibrary().getName());
            for (Object o : match.getLocations()) {
                Location location = (Location) o;
                SimpleLocation simpleLocation = new SimpleLocation(location.getStart(), location.getEnd());
                // Adding the same SimpleLocation to both the Signature and the Entry is OK, as the SimpleLocation is immutable.
                ss.getLocations().add(simpleLocation);
                simpleEntry.getLocations().add(simpleLocation);
            }

            if (simpleProtein.getAllEntries().contains(simpleEntry)) {
                // Entry already exists, so get it
                simpleEntry = simpleProtein.getAllEntries().get(simpleProtein.getAllEntries().indexOf(simpleEntry));
            } else {
                // Create new entry
                simpleProtein.getAllEntries().add(simpleEntry);
            }

            simpleEntry.getSignaturesMap().put(signatureAc, ss);
        }
        return simpleProtein;
    }

    public String getFamilyHierarchy() {
        return new FamilyHierachyElementBuilder(this).build().toString();
    }

}
