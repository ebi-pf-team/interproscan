package uk.ac.ebi.interpro.scan.management.model.implementations.funfam;

import uk.ac.ebi.interpro.scan.io.funfam.CathResolveHit;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.Hmmer3SearchMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.FunFamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.alignment.CigarAlignmentEncoder;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.*;
import java.util.*;


public class ParseAndPersisteStep extends Step  {
    private String cathResolveHitsOutputFileNameTemplate;
    private String hmmsearchOutputFileNameTemplate;
    private Hmmer3SearchMatchParser<FunFamHmmer3RawMatch> hmmer3SearchMatchParser;
    private String signatureLibraryRelease;
    private RawMatchDAO<FunFamHmmer3RawMatch> rawMatchDAO;
    private FilteredMatchDAO<FunFamHmmer3RawMatch, Hmmer3Match> filteredMatchDAO;

    public String getCathResolveHitsOutputFileNameTemplate() {
        return cathResolveHitsOutputFileNameTemplate;
    }

    public void setCathResolveHitsOutputFileNameTemplate(String cathResolveHitsOutputFileNameTemplate) {
        this.cathResolveHitsOutputFileNameTemplate = cathResolveHitsOutputFileNameTemplate;
    }

    public String getHmmsearchOutputFileNameTemplate() {
        return hmmsearchOutputFileNameTemplate;
    }

    public void setHmmsearchOutputFileNameTemplate(String hmmsearchOutputFileNameTemplate) {
        this.hmmsearchOutputFileNameTemplate = hmmsearchOutputFileNameTemplate;
    }

    public Hmmer3SearchMatchParser<FunFamHmmer3RawMatch> getHmmer3SearchMatchParser() {
        return hmmer3SearchMatchParser;
    }

    public void setHmmer3SearchMatchParser(Hmmer3SearchMatchParser<FunFamHmmer3RawMatch> hmmer3SearchMatchParser) {
        this.hmmer3SearchMatchParser = hmmer3SearchMatchParser;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public RawMatchDAO<FunFamHmmer3RawMatch> getRawMatchDAO() {
        return rawMatchDAO;
    }

    public void setRawMatchDAO(RawMatchDAO<FunFamHmmer3RawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    public FilteredMatchDAO<FunFamHmmer3RawMatch, Hmmer3Match> getFilteredMatchDAO() {
        return filteredMatchDAO;
    }

    public void setFilteredMatchDAO(FilteredMatchDAO<FunFamHmmer3RawMatch, Hmmer3Match> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            return;
        }

        String hmmsearchOutputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getHmmsearchOutputFileNameTemplate());
        String cathResolveHitsOutputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getCathResolveHitsOutputFileNameTemplate());

        Set<RawProtein<FunFamHmmer3RawMatch>> rawProteins;
        try (InputStream is = new FileInputStream(hmmsearchOutputFileName)) {
            rawProteins = this.getHmmer3SearchMatchParser().parse(is);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + hmmsearchOutputFileName, e);
        }

        Map<String, CathResolveHit> cathResolveHits;
        try (BufferedReader reader = new BufferedReader(new FileReader(cathResolveHitsOutputFileName))) {
            cathResolveHits = CathResolveHit.parse(reader);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + cathResolveHitsOutputFileName, e);
        }

        final CigarAlignmentEncoder cigarEncoder = new CigarAlignmentEncoder();
        Map<String, RawProtein<FunFamHmmer3RawMatch>> matches = new HashMap<>();
        for (RawProtein<FunFamHmmer3RawMatch> protein: rawProteins) {
            for (FunFamHmmer3RawMatch match: protein.getMatches()) {
                String key = match.getSequenceIdentifier() + '-' + match.getModelId() + '-' + match.getEnvelopeStart() + '-' + match.getEnvelopeEnd();
                CathResolveHit hit = cathResolveHits.get(key);

                if (hit != null) {
                    UUID splitGroup = UUID.randomUUID();
                    String[] domainRegions = hit.getResolvedRegions().split(",");
                    boolean isDiscontinuous = domainRegions.length > 1;
                    for (String domainRegion: domainRegions) {
                        String[] positions = domainRegion.split("-");
                        int startPosition = Integer.parseInt(positions[0]);
                        int endPosition = Integer.parseInt(positions[1]);

                        FunFamHmmer3RawMatch newMatch = new FunFamHmmer3RawMatch(
                                match.getSequenceIdentifier(),
                                match.getModelId(),
                                this.getSignatureLibraryRelease(),
                                match.getLocationStart(),
                                match.getLocationEnd(),
                                match.getEvalue(),
                                match.getScore(),
                                match.getHmmStart(),
                                match.getHmmEnd(),
                                match.getHmmBounds(),
                                match.getLocationScore(),
                                match.getEnvelopeStart(),
                                match.getEnvelopeEnd(),
                                match.getExpectedAccuracy(),
                                match.getFullSequenceBias(),
                                match.getDomainCeValue(),
                                match.getDomainIeValue(),
                                match.getDomainBias(),
                                cigarEncoder.encode(match.getAlignment()),
                                startPosition,
                                endPosition
                        );

                        if (isDiscontinuous) {
                            newMatch.setSplitGroup(splitGroup);
                        }

                        matches.computeIfAbsent(match.getSequenceIdentifier(), k -> new RawProtein<>(k)).addMatch(newMatch);
                    }
                }
            }
        }

        this.getFilteredMatchDAO().persist(matches.values());
    }
}
