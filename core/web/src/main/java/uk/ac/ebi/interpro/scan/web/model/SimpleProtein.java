package uk.ac.ebi.interpro.scan.web.model;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.GoTerm;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.io.FamilyHierachyElementBuilder;
import uk.ac.ebi.interpro.scan.web.io.svg.FamilyHierachySvgElementBuilder;

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
    private final Integer taxId;
    private final String taxScienceName;
    private final String taxFullName;
    private final boolean isProteinFragment;
    private final List<SimpleEntry> entries = new ArrayList<SimpleEntry>();
    private Set<SimpleEntry> familyEntries = null;
    private List<SimpleStructuralDatabase> structuralDatabases = new ArrayList<SimpleStructuralDatabase>();

    private List<SimpleSite> sites = new ArrayList<>();


    public SimpleProtein(String ac, String id, String name, int length, String md5, String crc64,
                         int taxId, String taxScienceName, String taxFullName, boolean isProteinFragment) {
        this.ac = ac;
        this.id = id;
        this.name = name;
        this.length = length;
        this.md5 = md5;
        this.crc64 = crc64;
        this.taxId = taxId;
        this.taxScienceName = taxScienceName;
        this.taxFullName = taxFullName;
        this.isProteinFragment = isProteinFragment;
    }

    /**
     * Construct a SimpleProtein without the taxonomy information.
     *
     * @param ac                Protein accession
     * @param id                Protein identifier
     * @param name              Protein name
     * @param length            Protein length
     * @param md5               MD5
     * @param crc64             CRC64
     * @param isProteinFragment True if this is a protein fragment or false if complete
     */
    public SimpleProtein(String ac, String id, String name, int length, String md5, String crc64,
                         boolean isProteinFragment) {
        this.ac = ac;
        this.id = id;
        this.name = name;
        this.length = length;
        this.md5 = md5;
        this.crc64 = crc64;
        this.taxId = null;
        this.taxScienceName = null;
        this.taxFullName = null;
        this.isProteinFragment = isProteinFragment;
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

    public boolean isProteinFragment() {
        return isProteinFragment;
    }

    public List<SimpleEntry> getAllEntries() {
        return entries;
    }

    public List<SimpleStructuralDatabase> getStructuralDatabases() {
        return structuralDatabases;
    }

    public void setStructuralDatabases(List<SimpleStructuralDatabase> structuralDatabases) {
        this.structuralDatabases = structuralDatabases;
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
            familyEntries = new TreeSet<SimpleEntry>();
            for (SimpleEntry entry : entries) {
                if (entry.isIntegrated() && EntryType.FAMILY == entry.getType()) {
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

    /**
     * USED BY FREEMARKER - DON'T DELETE
     *
     * @return
     */
    public Set<GoTerm> getProcessGoTerms() {
        return getGoTerms(GoTerm.GoRoot.BIOLOGICAL_PROCESS);
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     *
     * @return
     */
    public Set<GoTerm> getComponentGoTerms() {
        return getGoTerms(GoTerm.GoRoot.CELLULAR_COMPONENT);
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     *
     * @return
     */
    public Set<GoTerm> getFunctionGoTerms() {
        return getGoTerms(GoTerm.GoRoot.MOLECULAR_FUNCTION);
    }

    /**
     * Returns the height in pixel for the GO annotation section within the SVG template.
     *
     * @param heightPerXrefLine
     * @param globalHeight
     * @return
     */
    public int getProteinXrefComponentHeightForSVG(int heightPerXrefLine, int globalHeight) {
        int maxNumberOfXrefs = 0;
        int functionSize = getFunctionGoTerms().size();
        int componentSize = getComponentGoTerms().size();
        int processSize = getProcessGoTerms().size();
        if (functionSize > maxNumberOfXrefs) {
            maxNumberOfXrefs = functionSize;
        }
        if (componentSize > maxNumberOfXrefs) {
            maxNumberOfXrefs = componentSize;
        }
        if (processSize > maxNumberOfXrefs) {
            maxNumberOfXrefs = processSize;
        }
        return maxNumberOfXrefs * heightPerXrefLine + globalHeight;
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     *
     * @return
     */
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

    /**
     * Returns the height in pixel for the un-integrated signatures section within the SVG template.
     *
     * @param heightPerSignatureLine
     * @param globalHeight
     * @return
     */
    public int getUnintegratedSignaturesComponentHeightForSVG(int heightPerSignatureLine, int globalHeight) {
        //default
        int resultValue = 0;
        if (getUnintegratedSignatures() != null) {
            resultValue = (getUnintegratedSignatures().size() - 1) * heightPerSignatureLine + globalHeight;
        }
        return resultValue;
    }

    public List<SimpleSite> getSites() {
        Collections.sort(sites);
        return sites;
    }

    public void setSites(List<SimpleSite> sites) {
        this.sites = sites;
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     *
     * @return
     */
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

    /**
     * USED BY FREEMARKER - DON'T DELETE
     *
     * @return
     */
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
     * USED BY FREEMARKER - DON'T DELETE
     * <p/>
     * Method to return an HTML attribute class="disabled" if there is no
     * match to an Entry of the specified type.
     *
     * @param type of the Entry, e.g. domain, family, site etc.
     * @return the number of matches of the specified type.
     */
    public String disabledStyleIfNoMatches(String type) {
        return (hasMatches(type)) ? "" : "class=\"disabled\"";
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     * <p/>
     * Method to return an HTML attribute disabled="disabled" if there is no
     * match to an Entry of the specified type.
     *
     * @param type of the Entry, e.g. domain, family, site etc.
     * @return the number of matches of the specified type.
     */
    public String disableIfNoMatches(String type) {
        return (hasMatches(type)) ? "" : "disabled=\"disabled\"";
    }


    private final Map<String, Boolean> hasMatchCache = new HashMap<String, Boolean>();

    /**
     * USED BY FREEMARKER - DON'T DELETE
     * <p/>
     * Method to return an HTML attribute class="disabled" if there is no
     * match to an Entry of the specified type.
     *
     * @param type of the Entry, e.g. domain, family, site etc.
     * @return the number of matches of the specified type.
     */
    public boolean hasMatches(String type) {
        if (hasMatchCache.containsKey(type)) {
            return hasMatchCache.get(type);
        }
        EntryType entryType = EntryType.valueOf(type);
        if (entryType == null) {
            throw new IllegalArgumentException("The argument to hasMatches MUST be the name of a member of the uk.ac.ebi.interpro.scan.web.model.EntryType enum.");
        }
        for (final SimpleEntry entry : entries) {
            if (entryType == entry.getType()) {
                hasMatchCache.put(type, true);
                return true;
            }
        }
        hasMatchCache.put(type, false);
        return false;
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     * <p/>
     * Method to return an HTML attribute class="disabled" if there are no sites.
     *
     * @return true if one or more sites are present, otherwise false
     */
    public boolean hasSites() {
        if (this.sites == null || this.sites.size() < 1) {
            return false;
        }
        return true;
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     * <p/>
     * Method to return an HTML attribute class="disabled" if there are no sites.
     *
     * @return required HTML snippet.
     */
    public String disabledStyleIfNoSites() {
        return (hasSites()) ? "" : "class=\"disabled\"";
    }

    /**
     * USED BY FREEMARKER - DON'T DELETE
     * <p/>
     * Method to return an HTML attribute disabled="disabled" if there are no sites.
     *
     * @return required HTML snippet.
     */
    public String disableIfNoSites() {
        return (hasSites()) ? "" : "disabled=\"disabled\"";
    }


    private static final String UNKNOWN = "Unknown";

    /**
     * Returns a {@link SimpleProtein} from a {@link uk.ac.ebi.interpro.scan.model.Protein}
     *
     * @param protein        Protein
     * @param xref           The ProteinXref currently being considered
     * @param entryHierarchy Entry hierarchy
     * @return A {@link SimpleProtein} from a {@link uk.ac.ebi.interpro.scan.model.Protein}
     */
    public static SimpleProtein valueOf(Protein protein, ProteinXref xref, EntryHierarchy entryHierarchy) {
        if (entryHierarchy == null) {
            throw new IllegalArgumentException("SimpleProtein.valueOf method: the EntryHierarchy parameter must not be null.");
        }

        // 1) Create SimpleProtein object
        String crc64 = UNKNOWN;
        String taxScienceName = UNKNOWN;
        String taxFullName = UNKNOWN;
        boolean isProteinFragment = false;
        final String proteinAc = xref.getIdentifier();
        final String proteinName = (xref.getName() == null) ? UNKNOWN : xref.getName();
        final String proteinDesc = (xref.getDescription() == null) ? UNKNOWN : xref.getDescription();
        final SimpleProtein simpleProtein = new SimpleProtein(proteinAc, proteinName, proteinDesc, protein.getSequenceLength(),
                protein.getMd5(), crc64, 0, taxScienceName, taxFullName, isProteinFragment);

        //Iterate over all protein matches
        for (final Match match : protein.getMatches()) {
            // 2) Create SimpleSignature object
            final Signature signature = match.getSignature();
            final String signatureAc = signature.getAccession();
            final String signatureName = (signature.getName() == null || signature.getName().length() == 0)
                    ? signatureAc
                    : signature.getName();
            SignatureLibraryRelease signatureLibraryRelease = signature.getSignatureLibraryRelease();
            final String signatureLibraryName = signatureLibraryRelease == null ? "UNKNOWN" : signatureLibraryRelease.getLibrary().getName();
            SimpleSignature simpleSignature = new SimpleSignature(signatureAc, signatureName, signatureLibraryName);

            // 3) Create SimpleEntry object
            final Entry entry = signature.getEntry();
            SimpleEntry simpleEntry = (entry == null)
                    ? new SimpleEntry("", SimpleEntry.UNINTEGRATED, SimpleEntry.UNINTEGRATED, EntryType.UNKNOWN, entryHierarchy)
                    : new SimpleEntry(entry.getAccession(), entry.getName(), entry.getDescription(), EntryType.mapFromModelEntryType(entry.getType()), entryHierarchy);

            // If SimpleEntry entry already exists, get it
            if (simpleProtein.getAllEntries().contains(simpleEntry)) {
                simpleEntry = simpleProtein.getAllEntries().get(simpleProtein.getAllEntries().indexOf(simpleEntry));
            } else {//Else add it to the list of simple protein entries
                simpleProtein.getAllEntries().add(simpleEntry);
            }

            // If SimpleSignature entry already exists, get it
            if (simpleEntry.getSignaturesMap().containsKey(signatureAc)) {
                simpleSignature = simpleEntry.getSignaturesMap().get(signatureAc);
            } else {
                simpleEntry.getSignaturesMap().put(signatureAc, simpleSignature);
            }

            //Iterate over match locations
            for (Object o : match.getLocations()) {

                // 4) Create SimpleLocation object
                final Location location = (Location) o;
                final SimpleLocation simpleLocation = new SimpleLocation(location.getStart(), location.getEnd());

                // Adding the same SimpleLocation to both the Signature and the Entry is OK, as the SimpleLocation is immutable.
                simpleSignature.getLocations().add(simpleLocation);
                // Add location to the list of super matches
                simpleEntry.getLocations().add(simpleLocation);

                // Add any sites from that location
                if (location instanceof LocationWithSites) {
                    final Set<Site> siteSet = ((LocationWithSites) location).getSites();
                    if (siteSet != null) {
                        long i = 1L;
                        for (Site site : siteSet) {
                            Long siteId = site.getId();
                            if (siteId == null) {
                                siteId = i; // Auto-allocate a temporary ID unique to this site/protein when not already set (e.g. for convert mode)
                            }
                            SimpleSite simpleSite = new SimpleSite(siteId, site.getDescription(), site.getNumLocations(), simpleSignature, simpleEntry);
                            for (SiteLocation siteLocation : site.getSiteLocations()) {
                                SimpleSiteLocation simpleSiteLocation = new SimpleSiteLocation(siteLocation.getResidue(), new SimpleLocation(siteLocation.getStart(), siteLocation.getEnd()));
                                simpleSite.addSiteLocation(simpleSiteLocation);
                            }
                            simpleProtein.getSites().add(simpleSite);
                            i++;
                        }
                    }
                }
            }
        }
        return simpleProtein;
    }

    public String getFamilyHierarchy() {
        return new FamilyHierachyElementBuilder(this).build().toString();
    }

    public String getFamilyHierarchyForSvg() {
        return new FamilyHierachySvgElementBuilder(this).build().toString();
    }

    /**
     * Returns the height in pixel for family hierarchy section within the SVG template.
     *
     * @param heightPerHierarchyLevel
     * @param globalHeight
     * @return
     */
    public int getFamilyComponentHeight(int heightPerHierarchyLevel, int globalHeight) {
        FamilyHierachySvgElementBuilder instance = new FamilyHierachySvgElementBuilder(this);
        instance.build();
        return instance.getRowCounter() * heightPerHierarchyLevel + globalHeight;
    }
}
