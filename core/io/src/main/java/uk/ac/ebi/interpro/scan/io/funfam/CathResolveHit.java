package uk.ac.ebi.interpro.scan.io.funfam;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CathResolveHit {
    private final String queryIdentifier;
    private final String matchIdentifier;
    private final Double score;
    private final String regions;
    private final String resolvedRegions;
    private final String alignedRegions;
    private final Double condEvalue;
    private final Double indpEvalue;

    public String getQueryIdentifier() {
        return queryIdentifier;
    }

    public String getMatchIdentifier() {
        return matchIdentifier;
    }

    public Double getScore() {
        return score;
    }

    public String getRegions() {
        return regions;
    }

    public String getResolvedRegions() {
        return resolvedRegions;
    }

    public String getAlignedRegions() {
        return alignedRegions;
    }

    public Double getCondEvalue() {
        return condEvalue;
    }

    public Double getIndpEvalue() {
        return indpEvalue;
    }

    public String getKey() {
        return this.getQueryIdentifier() + this.getMatchIdentifier() + this.getRegions();
    }

    private CathResolveHit (String queryId, String matchId, Double score, String regions, String resolvedRegions,
                            String alignedRegions, Double condEvalue, Double indpEvalue) {
        this.queryIdentifier = queryId;
        this.matchIdentifier = matchId;
        this.score = score;
        this.regions = regions;
        this.resolvedRegions = resolvedRegions;
        this.alignedRegions = alignedRegions;
        this.condEvalue = condEvalue;
        this.indpEvalue = indpEvalue;
    }

    public static CathResolveHit parseLine (String line) {
        String[] fields = line.split("\\s+");

        String queryId = fields[0];
        String matchId = fields[1];
        Double score = Double.parseDouble(fields[2]);
        String regions = fields[3];
        String resolvedRegions = fields[4];
        String alignedRegions = fields[5];
        Double condEvalue = Double.parseDouble(fields[6]);
        Double indpEvalue = Double.parseDouble(fields[7]);

        return new CathResolveHit(queryId, matchId, score, regions, resolvedRegions, alignedRegions, condEvalue,
                indpEvalue);
    }

    public static Map<String, CathResolveHit> parse (BufferedReader reader) throws IOException {
        Map<String, CathResolveHit> hits = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty() || line.startsWith("#")){
                continue;
            }

            CathResolveHit hit = CathResolveHit.parseLine(line);
            hits.put(hit.getKey(), hit);
        }

        return hits;
    }
}
