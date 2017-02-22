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

    private static final String COLUMN_SEP = "\\s+";

    private static final int QUERY_PROTEIN_ID_POS = 0;
    private static final int MATCHL_ID_POS = 1;
    private static final int SCORE_POS = 2;
    private static final int STARTS_STOPS_POS = 3;
    private static final int RESOLVED_STARTS_STOPS_POS = 4;

    private static final int LAST_POS = RESOLVED_STARTS_STOPS_POS;

    private final String queryProteinId;
    private final String matchId;
    private final Double score;
    private final String startsStopsPosition;
    private final String resolvedStartsStopsPosition;


    private CathResolverRecord() {
        this.queryProteinId = null;
        this.matchId = null;
        this.score = null;
        this.startsStopsPosition = null;
        this.resolvedStartsStopsPosition = null;
    }

    public CathResolverRecord(String queryProteinId, String matchId,
                              Double score, String startsStopsPosition,
                              String resolvedStartsStopsPosition) {

        this.queryProteinId = queryProteinId;
        this.matchId = matchId;
        this.score = score;
        this.startsStopsPosition = startsStopsPosition;
        this.resolvedStartsStopsPosition = resolvedStartsStopsPosition;
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

    public static CathResolverRecord valueOf(Gene3dHmmer3RawMatch rawMatch) {
        if (rawMatch == null) {
            throw new NullPointerException("RawMatch object is null");
        }
        return new CathResolverRecord(rawMatch.getSequenceIdentifier(), rawMatch.getModelId(),
                rawMatch.getLocationScore(),
                rawMatch.getLocationStart() + "-" + rawMatch.getLocationEnd(),
                rawMatch.getLocationStart() + "-" + rawMatch.getLocationEnd()
        );

    }

    public static CathResolverRecord valueOf(String line) {
        String[] columns = line.split(COLUMN_SEP);


        String queryProteinId = columns[QUERY_PROTEIN_ID_POS];
        String matchId = columns[MATCHL_ID_POS];
        Double score = Double.parseDouble(columns[SCORE_POS]);

        String startsStopsPosition = columns[STARTS_STOPS_POS];
        String resolvedStartsStopsPosition = columns[RESOLVED_STARTS_STOPS_POS];

        return new CathResolverRecord(queryProteinId, matchId,
                score, startsStopsPosition,
                resolvedStartsStopsPosition);
    }

    public static String toLine(CathResolverRecord record) {
        String[] columns = new String[LAST_POS + 1];
        columns[QUERY_PROTEIN_ID_POS] = record.queryProteinId;
        columns[MATCHL_ID_POS] = record.matchId;
        columns[SCORE_POS] = String.valueOf(record.score);
        columns[STARTS_STOPS_POS] = record.startsStopsPosition;
        columns[RESOLVED_STARTS_STOPS_POS] = record.resolvedStartsStopsPosition;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            builder.append(columns[i]);
            if (i < LAST_POS) {
                builder.append(COLUMN_SEP);
            }
        }
        return builder.toString();
    }


    public String getRecordKey() {
        return queryProteinId
                +  matchId
                +  startsStopsPosition;
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
