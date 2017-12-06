package uk.ac.ebi.interpro.scan.io.gene3d;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.alignment.CigarAlignmentEncoder;


/**
 * Represents a record from the cath resolver hits output:
 * <pre>
 * Column 1: query_protein_id  e.g. 1 or UPI0001
 * Column 2: match_id HMM e.g.
 * Column 3: score
 * Column 4: starts_stops
 * Column 5: resolved_starts_stops
 * </pre>
 * For example:
 * <pre>
 * UPI00084CC181 cath|4_1_0|4hppA01/1-101 58.8 11-114 11-114
 * </pre>
 *
 * @author Gift Nuka
 * @version $Id$
 */

public final class CathResolverRecord {
    public static final String SEGMENT_BOUNDARY_SEPARATOR = ":";

    //#FIELDS query-id match-id score boundaries resolved aligned-regions cond-evalue indp-evalue
    private static final String COLUMN_SEP = "\\s+"; // ","; //"\\s+";

    private static final int MODEL_ID_POS = 0;
    private static final int CATH_FAMILY_ID_POS = 1;
    private static final int QUERY_PROTEIN_ID_POS = 2;
    private static final int MATCH_MODEL_NAME_POS = 3;
    private static final int SCORE_POS = 4;
    private static final int STARTS_STOPS_POS = 5;
    private static final int RESOLVED_STARTS_STOPS_POS = 6;
    private static final int ALIGNED_REGIONS = 7;
    private static final int COND_EVALUE_POS = 8;
    private static final int IND_EVALUE_POS = 9;
    private static final int REGION_COMMENT_POS = 10;
    private static final int LAST_POS = REGION_COMMENT_POS;


    private final String modelId;
    private final String cathFamilyId;
    private final String queryProteinId;
    private final String matchModelName;
    private final String matchId;
    private final Double score;
    private final String startsStopsPosition;
    private final String resolvedStartsStopsPosition;
    private final String alignedRegions;
    private final Double condEvalue;
    private final Double indpEvalue;
    private final String regionComment;


    private CathResolverRecord() {
        this.modelId = null;
        this.cathFamilyId = null;
        this.queryProteinId = null;
        this.matchModelName = null;
        this.matchId = null;
        this.score = null;
        this.startsStopsPosition = null;
        this.resolvedStartsStopsPosition = null;
        this.alignedRegions = null;
        this.condEvalue = null;
        this.indpEvalue = null;
        this.regionComment = null;
    }

    public CathResolverRecord(String modelId,String cathFamilyId,
                              String queryProteinId, String matchModelName,
                              Double score, String startsStopsPosition,
                              String resolvedStartsStopsPosition,
                              String alignedRegions,   Double condEvalue,
                              Double indpEvalue, String regionComment
                            ) {

        this.modelId = modelId;
        this.cathFamilyId = cathFamilyId;
        this.queryProteinId = queryProteinId;
        this.matchModelName = matchModelName;
        this.matchId = null;
        this.score = score;
        this.startsStopsPosition = startsStopsPosition;
        this.resolvedStartsStopsPosition = resolvedStartsStopsPosition;
        this.alignedRegions = alignedRegions;
        this.condEvalue = condEvalue;
        this.indpEvalue = indpEvalue;
        this.regionComment = regionComment;
    }

    public String getModelId() {
        return modelId;
    }

    public String getQueryProteinId() {
        return queryProteinId;
    }

    public String getMatchId() {
        return matchId;
    }

    public Double getScore() {
        return score;
    }

    public String getStartsStopsPosition() {
        return startsStopsPosition;
    }

    public String getResolvedStartsStopsPosition() {
        return resolvedStartsStopsPosition;
    }

    public static String getSegmentBoundarySeparator() {
        return SEGMENT_BOUNDARY_SEPARATOR;
    }

    public String getCathFamilyId() {
        return cathFamilyId;
    }

    public String getMatchModelName() {
        return matchModelName;
    }

    public Double getCondEvalue() {
        return condEvalue;
    }

    public Double getIndpEvalue() {
        return indpEvalue;
    }

    public String getAlignedRegions() {
        return alignedRegions;
    }

    public String getRegionComment() {
        return regionComment;
    }

    public static CathResolverRecord valueOf(Gene3dHmmer3RawMatch rawMatch) {
        if (rawMatch == null) {
            throw new NullPointerException("RawMatch object is null");
        }
        return new CathResolverRecord(rawMatch.getModelId(), rawMatch.getCathFamilyId(),
                rawMatch.getSequenceIdentifier(),
                rawMatch.getHitModelName(),
                rawMatch.getLocationScore(),
                rawMatch.getLocationStart() + "-" + rawMatch.getLocationEnd(),
                rawMatch.getLocationStart() + "-" + rawMatch.getLocationEnd(),
                rawMatch.getAlignedRegions(),
                rawMatch.getDomainCeValue(), rawMatch.getDomainIeValue(),
                rawMatch.getRegionComment()
        );

    }

    public static CathResolverRecord valueOf(String line) {
        String[] columns = line.split(COLUMN_SEP);

        String modelId = columns[MODEL_ID_POS];
        String cathFamilyId = columns[CATH_FAMILY_ID_POS];
        String queryProteinId = columns[QUERY_PROTEIN_ID_POS];
        String matchModelName = columns[MATCH_MODEL_NAME_POS];
        //String matchId = columns[MATCH_ID_POS];
        Double score = Double.parseDouble(columns[SCORE_POS]);

        String startsStopsPosition = stripString(columns[STARTS_STOPS_POS]);
        String resolvedStartsStopsPosition = stripString(columns[RESOLVED_STARTS_STOPS_POS]);
        String alignedRegions = stripString(columns[ALIGNED_REGIONS]);
        Double condEvalue = Double.parseDouble(columns[COND_EVALUE_POS]);
        Double indpEvalue = Double.parseDouble(columns[IND_EVALUE_POS]);
        String regionComment = null;
        if ((columns.length - 1) ==  LAST_POS) {
            regionComment = columns[REGION_COMMENT_POS];
        }

        return new CathResolverRecord(modelId,cathFamilyId, queryProteinId, matchModelName,
                score, startsStopsPosition,
                resolvedStartsStopsPosition, alignedRegions,
                condEvalue, indpEvalue, regionComment);
    }

    public static String toLine(CathResolverRecord record) {
        String[] columns = new String[LAST_POS + 1];
        columns[MODEL_ID_POS] = record.modelId;
        columns[CATH_FAMILY_ID_POS] =  record.cathFamilyId;
        columns[QUERY_PROTEIN_ID_POS] = record.queryProteinId;
        columns[MATCH_MODEL_NAME_POS] = record.matchModelName;
        columns[SCORE_POS] = String.valueOf(record.score);
        columns[STARTS_STOPS_POS] = record.startsStopsPosition;
        columns[RESOLVED_STARTS_STOPS_POS] = record.resolvedStartsStopsPosition;
        columns[ALIGNED_REGIONS] = record.alignedRegions;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            builder.append(columns[i]);
            if (i < LAST_POS) {
                builder.append(COLUMN_SEP);
            }
        }
        return builder.toString();
    }

    public static String stripString(String targetString){
        return targetString.replace("\"","");
    }

    public String getRecordKey() {
        String modelIdKey = modelId;
        if (matchModelName.startsWith("dc_")){
            modelIdKey = matchModelName.replace("_" + modelId, "");
//            modelIdKey = matchModelName.replace("_" + modelId, "_" + modelId);
        }
        return queryProteinId
                +  modelIdKey
                +  startsStopsPosition;
//                +  resolvedStartsStopsPosition;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CathResolverRecord))
            return false;
        final CathResolverRecord r = (CathResolverRecord) o;
        return new EqualsBuilder()
                .append(queryProteinId, r.queryProteinId)
                .append(matchId, r.matchId)
                .append(score, r.score)
                .append(startsStopsPosition, r.startsStopsPosition)
                .append(resolvedStartsStopsPosition, r.resolvedStartsStopsPosition)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 97)
                .append(queryProteinId)
                .append(matchId)
                .append(score)
                .append(startsStopsPosition)
                .append(resolvedStartsStopsPosition)
                .toHashCode();
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
