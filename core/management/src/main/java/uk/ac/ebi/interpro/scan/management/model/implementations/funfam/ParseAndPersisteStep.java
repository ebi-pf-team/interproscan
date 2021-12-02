package uk.ac.ebi.interpro.scan.management.model.implementations.funfam;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.io.funfam.CathResolveHit;
import uk.ac.ebi.interpro.scan.io.gene3d.CathResolveHitsOutputParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.Hmmer3DomTblParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomTblDomainMatch;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.FunFamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;


public class ParseAndPersisteStep extends Step  {
    private static final Logger LOGGER = LogManager.getLogger(ParseAndPersisteStep.class.getName());
    private String cathResolveHitsOutputFileNameTemplate;
    private String hmmsearchDomTblOutputFileNameTemplate;
    private Hmmer3DomTblParser hmmer3DomTblParser;
    private CathResolveHitsOutputParser cathResolveHitsOutputParser;
    private String signatureLibraryRelease;
    private RawMatchDAO<FunFamHmmer3RawMatch> rawMatchDAO;
    private FilteredMatchDAO<FunFamHmmer3RawMatch, Hmmer3Match> filteredMatchDAO;

    public String getCathResolveHitsOutputFileNameTemplate() {
        return cathResolveHitsOutputFileNameTemplate;
    }

    public void setCathResolveHitsOutputFileNameTemplate(String cathResolveHitsOutputFileNameTemplate) {
        this.cathResolveHitsOutputFileNameTemplate = cathResolveHitsOutputFileNameTemplate;
    }

    public String getHmmsearchDomTblOutputFileNameTemplate() {
        return hmmsearchDomTblOutputFileNameTemplate;
    }

    public void setHmmsearchDomTblOutputFileNameTemplate(String hmmsearchDomTblOutputFileNameTemplate) {
        this.hmmsearchDomTblOutputFileNameTemplate = hmmsearchDomTblOutputFileNameTemplate;
    }

    public Hmmer3DomTblParser getHmmer3DomTblParser() {
        return hmmer3DomTblParser;
    }

    public void setHmmer3DomTblParser(Hmmer3DomTblParser hmmer3DomTblParser) {
        this.hmmer3DomTblParser = hmmer3DomTblParser;
    }

    public CathResolveHitsOutputParser getCathResolveHitsOutputParser() {
        return cathResolveHitsOutputParser;
    }

    public void setCathResolveHitsOutputParser(CathResolveHitsOutputParser cathResolveHitsOutputParser) {
        this.cathResolveHitsOutputParser = cathResolveHitsOutputParser;
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
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(110, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        String cathResolveHitsOutputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getCathResolveHitsOutputFileNameTemplate());
        String domTblOutputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getHmmsearchDomTblOutputFileNameTemplate());

        Map<String, CathResolveHit> cathResolveHits;
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(cathResolveHitsOutputFileName))) {
            cathResolveHits = CathResolveHit.parse(reader);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + cathResolveHitsOutputFileName, e);
        }

        Map<String, DomTblDomainMatch> domains = new HashMap<>();
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(domTblOutputFileName))) {
            String line;
            String mode = "hmmsearch";
            while ((line = reader.readLine()) != null) {
                Matcher matcher = DomTblDomainMatch.getDomainDataLineMatcher(line, mode);
                if (matcher.matches()) {
                    DomTblDomainMatch match = new DomTblDomainMatch(matcher, mode);
                    domains.put(match.getDomTblDominLineKey(), match);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + domTblOutputFileName, e);
        }

        Map<String, RawProtein<FunFamHmmer3RawMatch>> matches = new HashMap<>();
        for (String key: cathResolveHits.keySet()) {
            CathResolveHit hit = cathResolveHits.get(key);
            String[] domainRegions = hit.getResolvedRegions().split(",");

            DomTblDomainMatch domain = domains.get(key);
            if (domain == null) {
                int minStartPosition = -1;
                int maxEndPosition = -1;

                for (String domainRegion: domainRegions) {
                    String[] positions = domainRegion.split("-");
                    int startPosition = Integer.parseInt(positions[0]);
                    int endPosition = Integer.parseInt(positions[1]);

                    if (minStartPosition == -1 || startPosition < minStartPosition) {
                        minStartPosition = startPosition;
                    }

                    if (maxEndPosition == -1 || endPosition > maxEndPosition)
                        maxEndPosition = endPosition;
                }

                domain = new DomTblDomainMatch(hit.getQueryIdentifier(), hit.getMatchIdentifier(), hit.getCondEvalue(),
                        hit.getScore(), 0.0, hit.getCondEvalue(), hit.getIndpEvalue(), hit.getScore(),
                        0.0, 0, 0, minStartPosition, maxEndPosition,
                        0, 0, 0.0);
            }

            UUID splitGroup = UUID.randomUUID();
            boolean isDiscontinuous = domainRegions.length > 1;
            for (String domainRegion: domainRegions) {
                String[] positions = domainRegion.split("-");
                int startPosition = Integer.parseInt(positions[0]);
                int endPosition = Integer.parseInt(positions[1]);

                FunFamHmmer3RawMatch match = new FunFamHmmer3RawMatch(
                        domain.getTargetIdentifier(),
                        hit.getMatchIdentifier(),
                        this.getSignatureLibraryRelease(),
                        startPosition,
                        endPosition, domain.getSequenceEValue(),
                        domain.getSequenceScore(),
                        domain.getDomainHmmfrom(),
                        domain.getDomainHmmto(),
                        HmmBounds.calculateHmmBounds(domain.getDomainEnvFrom(), domain.getDomainEnvTo(), startPosition, endPosition),
                        domain.getDomainScore(),
                        domain.getDomainEnvFrom(),
                        domain.getDomainEnvTo(),
                        domain.getDomainAccuracy(),
                        domain.getSequenceBias(),
                        hit.getCondEvalue(),
                        hit.getIndpEvalue(),
                        domain.getDomainBias()
                );

                if (isDiscontinuous) {
                    match.setSplitGroup(splitGroup);
                }

                matches.computeIfAbsent(match.getSequenceIdentifier(), k -> new RawProtein<>(k)).addMatch(match);
            }
        }

        this.getFilteredMatchDAO().persist(matches.values());
    }
}
