package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.Hmmer3MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomTblDomainMatch;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class PIRSFHmmer3ParseStep extends Step {
    private String outputFileTemplate;
    private Hmmer3MatchParser<PirsfHmmer3RawMatch> outputParser;
    private String domtblOutputFileTemplate;
    private RawMatchDAO<PirsfHmmer3RawMatch> rawMatchDAO;

    public void setRawMatchDAO(RawMatchDAO<PirsfHmmer3RawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    public void setOutputFileTemplate(String outputFileTemplate) {
        this.outputFileTemplate = outputFileTemplate;
    }

    public void setDomtblOutputFileTemplate(String domtblOutputFileTemplate) {
        this.domtblOutputFileTemplate = domtblOutputFileTemplate;
    }

    public void setOutputParser(Hmmer3MatchParser<PirsfHmmer3RawMatch> outputParser) {
        this.outputParser = outputParser;
    }

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        delayForNfs();

        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            return;
        }

        String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, domtblOutputFileTemplate);
        Map<String, DomTblDomainMatch> domains = parseDomTabOutput(fileName);

        fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileTemplate);
        try (InputStream is = new FileInputStream(fileName)) {
            final Set<RawProtein<PirsfHmmer3RawMatch>> results = outputParser.parse(is);

            for (RawProtein<PirsfHmmer3RawMatch> protein : results) {
                for (PirsfHmmer3RawMatch match: protein.getMatches()) {
                    String key = protein.getProteinIdentifier()
                            + match.getModelId()
                            + match.getEnvelopeStart()
                            + "-"
                            + match.getEnvelopeEnd();

                    DomTblDomainMatch domTblDomainMatch = domains.get(key);
                    match.setSequenceLength(domTblDomainMatch.getTargetLength());
                }
            }

            rawMatchDAO.insertProteinMatches(results);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + fileName, e);
        }
    }

    private Map<String, DomTblDomainMatch> parseDomTabOutput(String fileName) {
        String mode = outputParser.getUseHmmsearch() ? "hmmsearch" : "hmmscan";
        Map<String, DomTblDomainMatch> domains = new HashMap<>();

        try (InputStream is = new FileInputStream(fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher domainDataLineMatcher = DomTblDomainMatch.getDomainDataLineMatcher(line, mode);
                if (domainDataLineMatcher.matches()) {
                    DomTblDomainMatch domainMatch = new DomTblDomainMatch(domainDataLineMatcher, mode);
                    String key = domainMatch.getPirsfDomTblDomainLineKey();
                    domains.put(key, domainMatch);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + fileName, e);
        }
        return domains;
    }
}