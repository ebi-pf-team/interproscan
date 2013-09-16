package uk.ac.ebi.interpro.scan.web.model;

import java.util.regex.Pattern;

/**
 * Signature databases.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public enum MatchDataSource {

    // Signature databases (descriptions from user manual appendices: http://www.ebi.ac.uk/interpro/user_manual.html)

    GENE3D(0,
            "Gene3D HMMs extended predictions of CATH protein structures.",
            "http://gene3d.biochem.ucl.ac.uk/Gene3D/",
            "http://www.cathdb.info/superfamily/$0"),

    HAMAP(0,
            "Members of HAMAP families are identified using PROSITE profile collections. " +
                    "HAMAP profiles are manually created by expert curators and they identify proteins " +
                    "that are part of well-conserved bacterial, archaeal and plastid-encoded proteins families " +
                    "or subfamilies. The aim of HAMAP is to propagate manually generated annotation to all " +
                    "members of a given protein family in an automated and controlled way using " +
                    "very strict criteria.",
            "http://www.expasy.org/sprot/hamap/",
            "http://www.expasy.org/unirule/$0"),

    PANTHER(0,
            "PANTHER HMMs define protein families, and subfamilies modelled on the divergence of function.",
            "http://www.pantherdb.org/",
            "http://www.pantherdb.org/panther/family.do?clsAccession=$0"),

    PFAM("Pfam",
            "Pfam is a collection of protein family alignments which were constructed semi-automatically " +
                    "using hidden Markov models (HMMs). Sequences that were not covered by Pfam were clustered and aligned " +
                    "automatically, and are released as Pfam-B. Pfam families have permanent accession numbers and contain " +
                    "functional annotation and cross-references to other databases, while Pfam-B families are re-generated " +
                    "at each release and are unannotated.",
            "http://pfam.sanger.ac.uk/",
            "http://pfam.sanger.ac.uk/family/$0"),

    PFAM_B("PfamB",
            PFAM.description,
            "http://pfam.sanger.ac.uk/",
            "http://pfam.sanger.ac.uk/pfamb/$0"),

    PIRSF(0,
            "PIRSF is a hierarchical classification system based on the 'homeomorphic family' principle. Members are " +
                    "both homologous (evolved from a common ancestor) and homeomorphic (sharing full-length sequence similarity " +
                    "and common domain architecture). PIRSF subfamilies are clusters representing functional specialization " +
                    "and/or domain architecture variation within the family.",
            "http://pir.georgetown.edu/pirwww/dbinfo/pirsf.shtml",
            "http://pir.georgetown.edu/cgi-bin/ipcSF?id=$0"),

    PRINTS(0,
            "PRINTS is a compendium of protein fingerprints. " +
                    "A fingerprint is a group of conserved motifs used to characterise a protein family; " +
                    "its diagnostic power is refined by iterative scanning of OWL. Usually the motifs do not overlap, " +
                    "but are separated along a sequence, though they may be contiguous in 3D-space. " +
                    "Fingerprints can encode protein folds and functionalities more flexibly and powerfully than can " +
                    "single motifs: the database thus provides a useful adjunct to PROSITE.",
            "http://www.bioinf.manchester.ac.uk/dbbrowser/PRINTS/",
            "http://www.bioinf.manchester.ac.uk/cgi-bin/dbbrowser/sprint/searchprintss.cgi?prints_accn=$0&amp;display_opts=Prints&amp;category=None&amp;queryform=false&amp;regexpr=off"),

    PRODOM("ProDom",
            "ProDom protein domain database consists of an automatic compilation of homologous domains.",
            "http://prodom.prabi.fr/",
            "http://prodom.prabi.fr/prodom/current/cgi-bin/request.pl?question=DBEN&amp;query=$0"),

    // db == 'PROSITE patterns' or db == 'PROSITE profiles'
    PROSITE(0,
            "PROSITE consists of documentation entries describing protein domains, families and functional sites, " +
                    "as well as associated patterns and profiles to identify them. Profiles and patterns are constructed " +
                    "from manually edited seed alignments. PROSITE is complemented by ProRule, a collection of rules based " +
                    "on profiles and patterns, which increases the discriminatory power of profiles and patterns by providing " +
                    "additional information about functionally and/or structurally critical amino acids.",
            "http://www.expasy.ch/prosite/",
            "http://www.expasy.org/prosite/$0"),

    PROSITE_PATTERNS("PROSITE patterns", PROSITE.description, PROSITE.homeUrl, PROSITE.linkUrl),

    PROSITE_PROFILES("PROSITE profiles", PROSITE.description, PROSITE.homeUrl, PROSITE.linkUrl),

    SMART(0,
            "SMART (a Simple Modular Architecture Research Tool) uses hidden Markov models (HMMs) and allows the " +
                    "identification of genetically mobile domains and the analysis of domain architectures. These domains are " +
                    "extensively annotated with respect to phyletic distributions, functional class, tertiary structures and " +
                    "functionally important residues.",
            "http://smart.embl-heidelberg.de/",
            "http://smart.embl-heidelberg.de/smart/do_annotation.pl?ACC=$0&amp;BLAST=DUMMY"),

    SUPERFAMILY(0,
            "SUPERFAMILY is a library of profile hidden Markov models that represent all proteins of known structure. " +
                    "The library is based on the SCOP classification of proteins. Models correspond to SCOP domains at the " +
                    "superfamily level and a hybrid method subsequently sub-classifies domains at the family level. SUPERFAMILY " +
                    "provides its structural assignments at both levels to all completely sequenced genomes.",
            "http://supfam.cs.bris.ac.uk/SUPERFAMILY/",
            "http://supfam.cs.bris.ac.uk/SUPERFAMILY/cgi-bin/scop.cgi?ipid=$0"),

    TIGRFAMS("TIGRFAMs",
            "TIGRFAMs is a collection of protein families.",
            "http://www.jcvi.org/cgi-bin/tigrfams/index.cgi",
            "http://www.jcvi.org/cgi-bin/tigrfams/HmmReportPage.cgi?acc=$0"),

    // Structural features
    CATH(0,
            "CATH is a manually curated classification of protein domain structures.",
            "http://www.cathdb.info/",
            "http://www.cathdb.info/superfamily/$0"),

    SCOP(0,
            "The Structural Classification of Proteins (SCOP) database is a largely manual classification of protein " +
                    "structural domains based on similarities of their amino acid sequences and three-dimensional structures",
            "http://scop.mrc-lmb.cam.ac.uk/scop",
            "http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?key=$0"),

    PDB(0,
            "The Protein Data Bank (PDB) is a repository for the 3-D structural data of large biological molecules, " +
                    "such as proteins and nucleic acids.",
            "http://www.ebi.ac.uk/pdbe/",
            "http://www.ebi.ac.uk/pdbe-srv/view/entry/$0/summary"),

    // Structural predictions
    MODBASE(0,
            "MODBASE models are generated by the fully automated homology-modelling pipeline MODPIPE.",
            "http://modbase.compbio.ucsf.edu/modbase-cgi/index.cgi",
            "http://modbase.compbio.ucsf.edu/modbase-cgi-new/model_search.cgi?searchvalue=$0&amp;searchproperties=database_id&amp;displaymode=moddetail&amp;searchmode=default"),

    SWISSMODEL("SWISS-MODEL",
            "SWISS-MODEL is a fully automated protein structure homology-modelling server. The purpose of this server " +
                    "is to make Protein Modelling accessible to all biochemists and molecular biologists World Wide.",
            "http://swissmodel.expasy.org/",
            "http://swissmodel.expasy.org/repository/?pid=smr03&amp;query_1_input=$0"),

    // Others

    COILS(0,
            "COILS compares a sequence to a database of known parallel two-stranded coiled-coils and derives a " +
                    "similarity score and then calculates the probability that the sequence will adopt a coiled-coil " +
                    "conformation.",
            "http://embnet.vital-it.ch/software/COILS_form.html",
            "http://embnet.vital-it.ch/software/COILS_form.html"),

    PHOBIUS(0,
            "Phobius is a combined transmembrane topology and signal peptide predictor.",
            "http://phobius.sbc.su.se/",
            "http://phobius.sbc.su.se/"),

    TMHMM(0,
            "TMHMM provides the prediction of transmembrane helices in proteins.",
            "http://www.cbs.dtu.dk/services/TMHMM/",
            "http://www.cbs.dtu.dk/services/TMHMM/"),

    // db == 'SignalP_EUK', db == 'SignalP_GRAM+' or db == 'SignalP_GRAM-'
    SIGNALP(0,
            "SignalP predicts the presence and location of signal peptide cleavage sites in amino acid sequences " +
                    "from different organisms: Gram-positive prokaryotes, Gram-negative prokaryotes, and eukaryotes.",
            "http://www.cbs.dtu.dk/services/SignalP/",
            "http://www.cbs.dtu.dk/services/SignalP/"),

    SIGNALP_EUK("SignalP euk", SIGNALP.description, SIGNALP.homeUrl, SIGNALP.linkUrl),

    SIGNALP_GRAM_POSITIVE("SignalP Gram+ prok", SIGNALP.description, SIGNALP.homeUrl, SIGNALP.linkUrl),

    SIGNALP_GRAM_NEGATIVE("SignalP Gram- prok", SIGNALP.description, SIGNALP.homeUrl, SIGNALP.linkUrl),

    // Other
    UNKNOWN;

    private static final Pattern ACCESSION_PATTERN = Pattern.compile("\\$0");

    private final String name;
    private final String description;
    private final String homeUrl;
    private final String linkUrl;

    private MatchDataSource() {
        this.name = name(); // Default name (see java.lang.Enum)
        this.description = "";
        this.homeUrl = "";
        this.linkUrl = "";
    }

    private MatchDataSource(String name, String description, String homeUrl, String linkUrl) {
        this.name = name;
        this.description = description;
        this.homeUrl = homeUrl;
        this.linkUrl = linkUrl;
    }

    // Dummy int just allows us to specify description without having to give name
    private MatchDataSource(int dummy, String description, String homeUrl, String linkUrl) {
        this.name = name(); // Default name (see java.lang.Enum)
        this.description = description;
        this.homeUrl = homeUrl;
        this.linkUrl = linkUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getLinkUrl(String accession) {
        if (accession == null || linkUrl == null) {
            return null;
        }
        final boolean longerThanThree = accession.length() > 3;
        if (this == GENE3D && accession.length() > 6 && accession.startsWith("G3DSA:")) {
            accession = accession.substring(6);
        } else if ((this == CATH || this == SCOP || this == MODBASE) && longerThanThree && accession.startsWith("MB_")) {
            accession = accession.substring(3);
        } else if (this == SWISSMODEL && longerThanThree && accession.startsWith("SW_")) {
            accession = accession.substring(3);
        }
        return ACCESSION_PATTERN.matcher(linkUrl).replaceAll(accession);
    }

    public String getSourceName() {
        return name;
    }

    /**
     * Lookup the member database enum from a text string that represents it
     *
     * @param name The text to parse
     * @return The member database enum value, or UNKNOWN if not found
     */
    public static MatchDataSource parseName(String name) {
        if (name != null) {

            // First get the supplied SignatureLibraryRelease "name" in the same format as it would be for the
            // MatchDataSource name that we will later iterate over
            name = name.toLowerCase().replaceAll("\\s+", ""); // Supplied name to check
            if (name.contains("_")) {
                // SignalP is the only SignatureLibraryRelease name to contain an underscore
                name = name.replaceAll("_", "");
                if (name.contains("gram")) {
                    name = name.replaceAll("positive", "+prok");
                    name = name.replaceAll("negative", "-prok");
                }
            }

            // Now iterate over the MatchDataSource names to see if we have a match
            for (MatchDataSource m : MatchDataSource.values()) {
                String mName = m.toString().toLowerCase(); // Possible name match
                if (name.equals(mName)) {
                    // E.g. "SMART"
                    return m;
                } else {
                    // E.g. Match "PROSITE profiles" from InterPro database with "ProSiteProfiles" from I5
                    // SignatureLibraryRelease name
                    mName = mName.replaceAll("\\s+", "");
                    if (name.equals(mName)) {
                        return m;
                    }
                }
            }
        }
        return UNKNOWN;
    }

    public static boolean isStructuralFeature(MatchDataSource matchDataSource) {
        return (matchDataSource.equals(CATH) || matchDataSource.equals(SCOP) || matchDataSource.equals(PDB));
    }

    public static boolean isStructuralPrediction(MatchDataSource matchDataSource) {
        return (matchDataSource.equals(MODBASE) || matchDataSource.equals(SWISSMODEL));
    }

    @Override
    public String toString() {
        return name;
    }


}
