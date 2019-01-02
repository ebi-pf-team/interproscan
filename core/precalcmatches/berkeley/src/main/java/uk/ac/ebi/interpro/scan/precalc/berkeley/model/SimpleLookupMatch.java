package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

public class SimpleLookupMatch {
    //These indices go hand by hand with the 'lookup_tmp_tab' table

    public static final int COL_IDX_MD5 = 1;
    public static final int COL_IDX_SIG_LIB_NAME = 2;
    public static final int COL_IDX_SIG_LIB_RELEASE = 3;
    public static final int COL_IDX_SIG_ACCESSION = 4;
    public static final int COL_IDX_MODEL_ACCESSION = 5;
    public static final int COL_IDX_SEQ_START = 6;
    public static final int COL_IDX_SEQ_END = 7;
    public static final int COL_IDX_FRAGMENTS = 8;
    public static final int COL_IDX_SEQ_SCORE = 9;
    public static final int COL_IDX_SEQ_EVALUE = 10;
    public static final int COL_IDX_HMM_BOUNDS = 11;
    public static final int COL_IDX_HMM_START = 12;
    public static final int COL_IDX_HMM_END = 13;
    public static final int COL_IDX_HMM_LENGTH = 14;
    public static final int COL_IDX_ENV_START = 15;
    public static final int COL_IDX_ENV_END = 16;
    public static final int COL_IDX_LOC_SCORE = 17;
    public static final int COL_IDX_LOC_EVALUE = 18;
    public static final int COL_IDX_SEQ_FEATURE = 19;


    String proteinMD5;
    String signatureLibraryName;
    String sigLibRelease;
    String signatureAccession;
    String modelAccession;
    Integer sequenceStart;
    Integer sequenceEnd;
    String  fragments;
    Double sequenceScore;
    Double sequenceEValue;
    String hmmBounds;
    Integer hmmStart;
    Integer hmmEnd;
    Integer hmmLength;
    Integer envelopeStart;
    Integer envelopeEnd;
    Double locationScore;
    Double locationEValue;
    String seqFeature;

    public SimpleLookupMatch(String proteinMD5, String lookupMatch) {
        String [] lookupMatchTokens =  lookupMatch.split(",");


//        System.out.println("lookupMatchTokens: " + lookupMatchTokens);
//        for (String token: lookupMatchTokens){
//            System.out.println(token);
//        }

//        String proteinMD5 = "00006F313F29B29DA473B6DDF28AF744";
//        String csvmatch = "SMART,7.1,SM00929,SM00929,92,132,92-132-S,85.9,4.9E-21,[],1,43,43,0,0,4.9E-21,85.9,";
//        System.out.println("csvmatch length: " + lookupMatchTokens.length) ;

//        System.out.println("hit: (" + lookupMatchTokens.length + ") " + Arrays.toString(lookupMatchTokens));
        int columnOffSet = 2; // -1 as zero indexed and -1 as no proteinMD5 in sequence hit tokens
        this.proteinMD5 = proteinMD5;
        signatureLibraryName = lookupMatchTokens[COL_IDX_SIG_LIB_NAME - columnOffSet];
        sigLibRelease = lookupMatchTokens[COL_IDX_SIG_LIB_RELEASE - columnOffSet];
        signatureAccession = lookupMatchTokens[COL_IDX_SIG_ACCESSION - columnOffSet];
        modelAccession = lookupMatchTokens[COL_IDX_MODEL_ACCESSION - columnOffSet];
        sequenceStart = Integer.parseInt(lookupMatchTokens[COL_IDX_SEQ_START - columnOffSet]);
        sequenceEnd = Integer.parseInt(lookupMatchTokens[COL_IDX_SEQ_END - columnOffSet]);
//        System.out.println("toString(): (after sequenceEnd)" + toString());
        fragments = lookupMatchTokens[COL_IDX_FRAGMENTS - columnOffSet];
//        fragments.replace(";", ",");  // we change fragments back to comma separated string, or maybe not

//        System.out.println("toString(): (after frag) " + toString());
        sequenceScore = Double.parseDouble(lookupMatchTokens[COL_IDX_SEQ_SCORE - columnOffSet]);
//        System.out.println("toString(): (after sequenceScore )" + toString());
//        System.out.println("sequenceEValue b4: (pos " + COL_IDX_SEQ_EVALUE + ") " + lookupMatchTokens[COL_IDX_SEQ_EVALUE - columnOffSet]);

        sequenceEValue = Double.parseDouble(lookupMatchTokens[COL_IDX_SEQ_EVALUE - columnOffSet]);
        hmmBounds = lookupMatchTokens[COL_IDX_HMM_BOUNDS - columnOffSet];
        hmmStart = Integer.parseInt(lookupMatchTokens[COL_IDX_HMM_START - columnOffSet]);
        hmmEnd = Integer.parseInt(lookupMatchTokens[COL_IDX_HMM_END - columnOffSet]);
        hmmLength = Integer.parseInt(lookupMatchTokens[COL_IDX_HMM_LENGTH - columnOffSet]);
        envelopeStart = Integer.parseInt(lookupMatchTokens[COL_IDX_ENV_START - columnOffSet]);
        envelopeEnd = Integer.parseInt(lookupMatchTokens[COL_IDX_ENV_END - columnOffSet]);
        locationScore = Double.parseDouble(lookupMatchTokens[COL_IDX_LOC_SCORE - columnOffSet]);
        locationEValue = Double.parseDouble(lookupMatchTokens[COL_IDX_LOC_EVALUE - columnOffSet]);
        if(lookupMatchTokens.length == 18) {
            seqFeature = lookupMatchTokens[COL_IDX_SEQ_FEATURE - columnOffSet];
        }else{
            seqFeature = "";
        }
//        System.out.println("fragments: " + fragments);
//        System.out.println(toString());
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

    public String getModelAccession() {
        return modelAccession;
    }

    public Integer getSequenceStart() {
        return sequenceStart;
    }

    public Integer getSequenceEnd() {
        return sequenceEnd;
    }

    public String getFragments() {
        return fragments;
    }

    public Double getSequenceScore() {
        return sequenceScore;
    }

    public Double getSequenceEValue() {
        return sequenceEValue;
    }

    public String getHmmBounds() {
        return hmmBounds;
    }

    public Integer getHmmStart() {
        return hmmStart;
    }

    public Integer getHmmEnd() {
        return hmmEnd;
    }

    public Integer getHmmLength() {
        return hmmLength;
    }

    public Integer getEnvelopeStart() {
        return envelopeStart;
    }

    public Integer getEnvelopeEnd() {
        return envelopeEnd;
    }

    public Double getLocationScore() {
        return locationScore;
    }

    public Double getLocationEValue() {
        return locationEValue;
    }

    public String getSeqFeature() {
        return seqFeature;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "SimpleLookupMatch{" +
                "proteinMD5='" + proteinMD5 + '\'' +
                ", signatureLibraryName='" + signatureLibraryName + '\'' +
                ", sigLibRelease='" + sigLibRelease + '\'' +
                ", signatureAccession='" + signatureAccession + '\'' +
                ", modelAccession='" + modelAccession + '\'' +
                ", sequenceStart=" + sequenceStart +
                ", sequenceEnd=" + sequenceEnd +
                ", fragments='" + fragments + '\'' +
                ", sequenceScore=" + sequenceScore +
                ", sequenceEValue=" + sequenceEValue +
                ", hmmBounds='" + hmmBounds + '\'' +
                ", hmmStart=" + hmmStart +
                ", hmmEnd=" + hmmEnd +
                ", hmmLength=" + hmmLength +
                ", envelopeStart=" + envelopeStart +
                ", envelopeEnd=" + envelopeEnd +
                ", locationScore=" + locationScore +
                ", locationEValue=" + locationEValue +
                ", seqFeature='" + seqFeature + '\'' +
                '}';
    }
}
