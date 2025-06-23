package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer3.PirsfPostProcessor;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.Hmmer3MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomTblDomainMatch;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Currently this step just takes PIRSF Hmmer3 raw matches and persists the relevant matches to the database. However
 * in the future some post processing may also be required. TODO Review this!
 */
public class PirsfPostProcessAndPersistStep extends Step {

    private String outputFileTemplate;
    private String domtblOutputFileTemplate;
    private Hmmer3MatchParser<PirsfHmmer3RawMatch> outputParser;

    private PirsfPostProcessor postProcessor;

    private FilteredMatchDAO<PirsfHmmer3RawMatch, Hmmer3Match> filteredMatchDAO;

    @Required
    public void setOutputFileTemplate(String outputFileTemplate) {
        this.outputFileTemplate = outputFileTemplate;
    }

    @Required
    public void setDomtblOutputFileTemplate(String domtblOutputFileTemplate) {
        this.domtblOutputFileTemplate = domtblOutputFileTemplate;
    }

    @Required
    public void setOutputParser(Hmmer3MatchParser<PirsfHmmer3RawMatch> outputParser) {
        this.outputParser = outputParser;
    }

    @Required
    public void setPostProcessor(PirsfPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }


    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(110, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        final String domOutputFilepath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, domtblOutputFileTemplate);
        Map<String, DomTblDomainMatch> domains = parseDomTabOutput(domOutputFilepath);

        Set<RawProtein<PirsfHmmer3RawMatch>> matches;
        String signatureLibraryRelease = null;
        final String outputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileTemplate);
        try (InputStream is = new FileInputStream(outputFilePath)) {
            matches = outputParser.parse(is);

            for (RawProtein<PirsfHmmer3RawMatch> protein : matches) {
                for (PirsfHmmer3RawMatch match: protein.getMatches()) {
                    String key = protein.getProteinIdentifier()
                            + match.getModelId()
                            + match.getEnvelopeStart()
                            + "-"
                            + match.getEnvelopeEnd();

                    DomTblDomainMatch domTblDomainMatch = domains.get(key);
                    match.setModelLength(domTblDomainMatch.getQueryLength());
                    match.setSequenceLength(domTblDomainMatch.getTargetLength());

                    if (signatureLibraryRelease == null) {
                        signatureLibraryRelease = match.getSignatureLibraryRelease();
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + outputFilePath, e);
        }

        // Post process
        try {
            Set<RawProtein<PirsfHmmer3RawMatch>> filteredMatches = postProcessor.process(matches);
            filteredMatchDAO.persist(filteredMatches);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to post process filtered PIRSF matches.", e);
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
