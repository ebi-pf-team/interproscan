package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import uk.ac.ebi.interpro.scan.util.Utilities;

public class SimpleLookupSite {
    //These indices go hand by hand with the 'LOOKUP_SITE' table

    public static final int COL_IDX_MD5 = 1;
    public static final int COL_IDX_SIG_LIB_NAME = 2;
    public static final int COL_IDX_SIG_LIB_RELEASE = 3;
    public static final int COL_IDX_SIG_ACCESSION = 4;
    public static final int COL_IDX_LOC_START = 5;
    public static final int COL_IDX_LOC_END = 6;
    public static final int COL_IDX_NUM_SITES = 7;
    public static final int COL_IDX_RESIDUE = 8;
    public static final int COL_IDX_RESIDUE_START = 9;
    public static final int COL_IDX_RESIDUE_END = 10;
    public static final int COL_IDX_DESCRIPTION = 11;


    String proteinMD5;
    String signatureLibraryName;
    String sigLibRelease;
    String signatureAccession;
    Integer locationStart;
    Integer locationEnd;
    Integer numSites;
    String residue;
    Integer residueStart;
    Integer residueEnd;
    String description;

    public SimpleLookupSite(String proteinMD5, String lookupMatch) {
        //SFLD,4,SFLDS00029,5,347,3,C,105,105
        String [] lookupMatchTokens =  lookupMatch.split(",");

        int columnOffSet = 2; // -1 as no proteinMD5 in sequence hit tokens
        this.proteinMD5 = proteinMD5;
        signatureLibraryName = lookupMatchTokens[COL_IDX_SIG_LIB_NAME - columnOffSet];
        sigLibRelease = lookupMatchTokens[COL_IDX_SIG_LIB_RELEASE - columnOffSet];
        signatureAccession = lookupMatchTokens[COL_IDX_SIG_ACCESSION - columnOffSet];
        locationStart = Integer.parseInt(lookupMatchTokens[COL_IDX_LOC_START - columnOffSet]);
        locationEnd = Integer.parseInt(lookupMatchTokens[COL_IDX_LOC_END - columnOffSet]);
        numSites = Integer.parseInt(lookupMatchTokens[COL_IDX_NUM_SITES - columnOffSet]);
        residue = lookupMatchTokens[COL_IDX_RESIDUE - columnOffSet];
        // if (residue.length() > 4000) {
        //     Utilities.verboseLog("Residue of length " + residue.length() + " found, will be truncated at 255 to fit in database.");
        //     residue = residue.substring(0, 4000);
        // }
        residueStart = Integer.parseInt(lookupMatchTokens[COL_IDX_RESIDUE_START - columnOffSet]);
        residueEnd = Integer.parseInt(lookupMatchTokens[COL_IDX_RESIDUE_END - columnOffSet]);

        if (lookupMatchTokens.length > 9) {
            Utilities.verboseLog(1100, "lookupMatchTokens size: " + lookupMatchTokens.length + " desc col : " + (COL_IDX_DESCRIPTION - columnOffSet));
            description = lookupMatchTokens[COL_IDX_DESCRIPTION - columnOffSet];
        }else{
            description = null;
        }
    }

    public static String kvValueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    public String getProteinMD5() {
        return proteinMD5;
    }

    public String getSignatureLibraryName() {
        return signatureLibraryName;
    }

    public String getSigLibRelease() {
        return sigLibRelease;
    }

    public String getSignatureAccession() {
        return signatureAccession;
    }

    public Integer getLocationStart() {
        return locationStart;
    }

    public Integer getLocationEnd() {
        return locationEnd;
    }

    public Integer getNumSites() {
        return numSites;
    }

    public String getResidue() {
        return residue;
    }

    public Integer getResidueStart() {
        return residueStart;
    }

    public Integer getResidueEnd() {
        return residueEnd;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "SimpleLookupSite{" +
                "proteinMD5='" + proteinMD5 + '\'' +
                ", signatureLibraryName='" + signatureLibraryName + '\'' +
                ", sigLibRelease='" + sigLibRelease + '\'' +
                ", signatureAccession='" + signatureAccession + '\'' +
                ", locationStart=" + locationStart +
                ", locationEnd=" + locationEnd +
                ", numSites=" + numSites +
                ", residue='" + residue + '\'' +
                ", residueStart=" + residueStart +
                ", residueEnd=" + residueEnd +
                ", description='" + description + '\'' +
                '}';
    }
}
